package com.daniel.jsoneditor.model.sessions;

import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages multiple open file sessions. Used by both GUI and MCP server.
 * GUI sessions are protected from being closed via MCP.
 */
public class FileSessionManager
{
    private static final Logger logger = LoggerFactory.getLogger(FileSessionManager.class);

    private final Map<String, EditorSession> sessions = new ConcurrentHashMap<>();

    /**
     * Tracks a file that may be shared across multiple sessions.
     */
    private record SharedFile(String canonicalPath, ModelImpl model, File jsonFile, File schemaFile, AtomicInteger refCount) {}

    /** Map from canonical JSON file path to the shared file entry. */
    private final Map<String, SharedFile> filesByPath = new ConcurrentHashMap<>();

    /** Map from session ID to canonical JSON file path (for reverse lookup). */
    private final Map<String, String> sessionToCanonicalPath = new ConcurrentHashMap<>();

    /**
     * Registers an existing GUI model as a session. Protected from MCP close.
     *
     * @param model the GUI's model
     * @param jsonFile the JSON file
     * @param schemaFile the schema file
     * @return session ID
     */
    public String registerGuiSession(final ReadableModel model, final File jsonFile, final File schemaFile)
    {
        EditorSession session;
        String sessionId;
        do
        {
            sessionId = generateUniqueId("gui-");
            session = new EditorSession(sessionId, model, jsonFile, schemaFile, true);
        }
        while (sessions.putIfAbsent(sessionId, session) != null);
        logger.info("Registered GUI session {} for {}", sessionId, jsonFile != null ? jsonFile.getAbsolutePath() : "null");
        return sessionId;
    }

    /**
     * Unregisters a GUI session (called when GUI closes a file).
     *
     * @param sessionId the session to unregister
     */
    public void unregisterGuiSession(final String sessionId)
    {
        sessions.computeIfPresent(sessionId, (final String key, final EditorSession session) ->
        {
            if (session.guiOwned())
            {
                logger.info("Unregistered GUI session {}", sessionId);
                return null; // removes the entry
            }
            return session;
        });
    }

    /**
     * Closes a headless session. Refuses to close GUI-owned sessions.
     * Also decrements the shared-file ref count via {@link #decrementRefCount(String)}.
     *
     * @param sessionId the session to close
     * @return {@link CloseFileResult#CLOSED} if closed, {@link CloseFileResult#NOT_FOUND} if not found,
     *         {@link CloseFileResult#GUI_OWNED} if the session is GUI-owned
     */
    public CloseFileResult closeFile(final String sessionId)
    {
        final CloseFileResult[] result = {CloseFileResult.NOT_FOUND};
        sessions.computeIfPresent(sessionId, (final String key, final EditorSession session) ->
        {
            if (session.guiOwned())
            {
                logger.warn("Cannot close GUI-owned session {} via MCP", sessionId);
                result[0] = CloseFileResult.GUI_OWNED;
                return session; // keep it
            }
            logger.info("Closed file session {}", sessionId);
            result[0] = CloseFileResult.CLOSED;
            return null; // removes the entry
        });
        if (result[0] == CloseFileResult.CLOSED)
        {
            decrementRefCount(sessionId);
        }
        return result[0];
    }

    /**
     * Returns the session for the given ID, or {@code null} if not found.
     *
     * @param sessionId the session ID
     * @return the session or null if not found
     */
    public EditorSession getSession(final String sessionId)
    {
        return sessions.get(sessionId);
    }

    /**
     * @return list of all active sessions
     */
    public List<EditorSession> listSessions()
    {
        return new ArrayList<>(sessions.values());
    }

    /**
     * Attaches a new session to the given file path. Deduplication-aware: if the path is already
     * open, the existing {@link ModelImpl} is reused. Concurrent attaches to the same new path are
     * atomic — only one model is created.
     *
     * @param jsonPath   absolute path to the JSON file
     * @param schemaPath absolute path to the schema file
     * @param guiOwned   true if this session is owned by a GUI window
     * @return {@link AttachResult} with sessionId on success, or error on failure
     */
    public AttachResult attachSession(final String jsonPath, final String schemaPath, final boolean guiOwned)
    {
        final File jsonFile = new File(jsonPath);
        final File schemaFile = new File(schemaPath);

        if (!jsonFile.exists())
        {
            return AttachResult.ofError("JSON file does not exist: " + jsonPath);
        }
        if (!schemaFile.exists())
        {
            return AttachResult.ofError("Schema file does not exist: " + schemaPath);
        }

        final String canonical;
        try
        {
            canonical = jsonFile.getCanonicalPath();
        }
        catch (final IOException e)
        {
            return AttachResult.ofError("Cannot resolve canonical path: " + e.getMessage());
        }

        final String[] errorHolder = {null};
        final SharedFile sharedFile = filesByPath.compute(canonical, (final String key, final SharedFile existing) ->
        {
            if (existing != null)
            {
                // Same path already open — check schema compatibility before reusing
                try
                {
                    final String existingSchemaCanonical = existing.schemaFile().getCanonicalPath();
                    final String requestedSchemaCanonical = schemaFile.getCanonicalPath();
                    if (!existingSchemaCanonical.equals(requestedSchemaCanonical))
                    {
                        errorHolder[0] = "Schema mismatch: path " + key + " is already open with schema "
                                + existingSchemaCanonical + " but requested " + requestedSchemaCanonical;
                        return existing; // keep existing entry unchanged
                    }
                }
                catch (final IOException e)
                {
                    errorHolder[0] = "Cannot resolve schema canonical path: " + e.getMessage();
                    return existing;
                }
                existing.refCount().incrementAndGet();
                return existing;
            }
            // New path — load and validate
            final JsonFileReaderAndWriterImpl reader = new JsonFileReaderAndWriterImpl();
            final JsonNode json;
            final JsonSchema schema;
            try
            {
                json = reader.getJsonFromFile(jsonFile);
                schema = reader.getSchemaFromFileResolvingRefs(schemaFile);
            }
            catch (final Exception e)
            {
                errorHolder[0] = "Failed to parse files: " + e.getMessage();
                return null;
            }
            if (json == null || schema == null)
            {
                errorHolder[0] = "Failed to parse JSON or schema files: " + jsonPath + " / " + schemaPath;
                return null;
            }
            final List<String> validationErrors = SchemaHelper.validateJsonWithSchema(json, schema);
            if (!validationErrors.isEmpty())
            {
                errorHolder[0] = "JSON does not validate against schema: " + String.join(", ", validationErrors);
                return null;
            }
            final ModelImpl model = ModelFactory.createEmpty();
            model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
            return new SharedFile(canonical, model, jsonFile, schemaFile, new AtomicInteger(1));
        });

        if (errorHolder[0] != null)
        {
            return AttachResult.ofError(errorHolder[0]);
        }
        if (sharedFile == null)
        {
            return AttachResult.ofError("Failed to load file: " + jsonPath);
        }

        final String prefix = guiOwned ? "gui-" : "";
        EditorSession session;
        String sessionId;
        do
        {
            sessionId = generateUniqueId(prefix);
            session = new EditorSession(sessionId, sharedFile.model(), sharedFile.jsonFile(), sharedFile.schemaFile(), guiOwned);
        }
        while (sessions.putIfAbsent(sessionId, session) != null);

        sessionToCanonicalPath.put(sessionId, canonical);

        logger.info("Attached session {} to path {} (refCount={})", sessionId, canonical, sharedFile.refCount().get());
        return AttachResult.ofSuccess(sessionId);
    }

    /**
     * Detaches a session created via {@link #attachSession}. Decrements the shared-file ref count;
     * when the last session for a path is detached the shared model is removed from the registry.
     *
     * @param sessionId the session ID returned by {@link #attachSession}
     */
    public void detachSession(final String sessionId)
    {
        sessions.remove(sessionId);
        decrementRefCount(sessionId);
        logger.info("Detached session {}", sessionId);
    }

    /**
     * Returns an existing {@link EditorSession} for the given canonical JSON file path, if any
     * session is currently attached to it. Useful for "focus existing window" UX.
     *
     * @param canonicalPath the canonical path of the JSON file (as returned by {@link File#getCanonicalPath()})
     * @return the first session found for the path, or empty if none
     */
    

    /**
     * Removes this session from {@code sessionToCanonicalPath} and decrements the ref count in
     * {@code filesByPath}. When the count reaches zero the shared model is evicted from the map.
     * Called by both {@link #closeFile(String)} and {@link #detachSession(String)}.
     */
    private void decrementRefCount(final String sessionId)
    {
        final String canonical = sessionToCanonicalPath.remove(sessionId);
        if (canonical != null)
        {
            filesByPath.computeIfPresent(canonical, (final String key, final SharedFile sharedFile) ->
            {
                final int remaining = sharedFile.refCount().decrementAndGet();
                if (remaining <= 0)
                {
                    logger.info("Removed shared file for path {} (last session detached)", key);
                    return null; // removes the entry from the map
                }
                return sharedFile;
            });
        }
    }

    private String generateUniqueId(final String prefix)
    {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}
