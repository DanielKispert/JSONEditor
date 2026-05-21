package com.daniel.jsoneditor.model.sessions;

import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.ModelFactory;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.daniel.jsoneditor.util.CanonicalPaths;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages multiple open file sessions. Used by both GUI and MCP server.
 * GUI sessions are protected from being closed via MCP.
 */
public class FileSessionManager
{
    private static final Logger logger = LoggerFactory.getLogger(FileSessionManager.class);

    private final Map<String, EditorSession> sessions = new ConcurrentHashMap<>();

    private record SharedFile(
            String canonicalPath,
            String schemaCanonicalPath,
            ModelImpl model,
            File jsonFile,
            File schemaFile,
            AtomicInteger refCount) {}

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
        // BUG 3 fix: decrement refcount for sessions created via attachSession(guiOwned=true).
        // For sessions created by registerGuiSession (not in sessionToCanonicalPath), this is a safe no-op.
        decrementRefCount(sessionId);
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

        // BUG 4 fix: use CanonicalPaths.canonicalize instead of raw getCanonicalPath() to match FileOpenCoordinator behavior.
        final String canonical = CanonicalPaths.canonicalize(jsonFile);
        // Pre-compute schema canonical path outside any lock — cheap and idempotent.
        final String requestedSchemaCanonical = CanonicalPaths.canonicalize(schemaFile);

        // BUG 2 fix: two-step approach so heavy IO (JSON read, schema read, validation) is NEVER done inside compute().
        // Fast path: file already loaded, just atomically increment its refcount.
        final SharedFile preExisting = filesByPath.get(canonical);
        if (preExisting != null)
        {
            final String[] errorHolder = {null};
            final SharedFile[] resultHolder = {null};
            filesByPath.compute(canonical, (final String key, final SharedFile sf) ->
            {
                if (sf == null)
                {
                    // Eviction race: entry removed between get() and compute(). Signal to fall through to slow path.
                    errorHolder[0] = "__RETRY__";
                    return null;
                }
                if (!sf.schemaCanonicalPath().equals(requestedSchemaCanonical))
                {
                    errorHolder[0] = "Schema mismatch: path " + key + " is already open with schema "
                            + sf.schemaCanonicalPath() + " but requested " + requestedSchemaCanonical;
                    return sf;
                }
                sf.refCount().incrementAndGet();
                resultHolder[0] = sf;
                return sf;
            });
            if (!"__RETRY__".equals(errorHolder[0]))
            {
                if (errorHolder[0] != null)
                {
                    return AttachResult.ofError(errorHolder[0]);
                }
                return finishAttach(resultHolder[0], canonical, guiOwned);
            }
            // Fall through to slow path: the entry was evicted between get() and compute().
        }

        // Slow path: file not currently loaded — perform heavy IO outside any lock.
        // Two threads racing on a fresh path each load IO independently; only one SharedFile wins the compute() insert.
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
            return AttachResult.ofError("Failed to parse files: " + e.getMessage());
        }
        if (json == null || schema == null)
        {
            return AttachResult.ofError("Failed to parse JSON or schema files: " + jsonPath + " / " + schemaPath);
        }
        final List<String> validationErrors = SchemaHelper.validateJsonWithSchema(json, schema);
        if (!validationErrors.isEmpty())
        {
            return AttachResult.ofError("JSON does not validate against schema: " + String.join(", ", validationErrors));
        }
        final ModelImpl model = ModelFactory.createEmpty();
        model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
        final SharedFile candidate = new SharedFile(canonical, requestedSchemaCanonical, model, jsonFile, schemaFile, new AtomicInteger(1));

        // Atomically insert our freshly loaded file, or adopt the one a concurrent thread already inserted.
        final String[] errorHolder = {null};
        final SharedFile[] winnerHolder = {null};
        filesByPath.compute(canonical, (final String key, final SharedFile sf) ->
        {
            if (sf == null)
            {
                // We win the race — insert our candidate.
                winnerHolder[0] = candidate;
                return candidate;
            }
            // Another thread already inserted its SharedFile; discard our candidate (it will be GC'd).
            if (!sf.schemaCanonicalPath().equals(requestedSchemaCanonical))
            {
                errorHolder[0] = "Schema mismatch: path " + key + " is already open with schema "
                        + sf.schemaCanonicalPath() + " but requested " + requestedSchemaCanonical;
                return sf;
            }
            sf.refCount().incrementAndGet();
            winnerHolder[0] = sf;
            return sf;
        });

        if (errorHolder[0] != null)
        {
            return AttachResult.ofError(errorHolder[0]);
        }
        if (winnerHolder[0] == null)
        {
            return AttachResult.ofError("Failed to load file: " + jsonPath);
        }
        return finishAttach(winnerHolder[0], canonical, guiOwned);
    }

    /** Registers the session entry and canonical-path mapping, then returns an AttachResult. */
    private AttachResult finishAttach(final SharedFile sharedFile, final String canonical, final boolean guiOwned)
    {
        final String prefix = guiOwned ? "gui-" : "";
        String sessionId;
        EditorSession session;
        // BUG 7 fix: insert canonical mapping BEFORE session is visible in `sessions` to close the
        // TOCTOU gap. On the rare event of a sessionId collision, clean up the orphan mapping and retry.
        do
        {
            sessionId = generateUniqueId(prefix);
            session = new EditorSession(sessionId, sharedFile.model(), sharedFile.jsonFile(), sharedFile.schemaFile(), guiOwned);
            sessionToCanonicalPath.put(sessionId, canonical);
            if (sessions.putIfAbsent(sessionId, session) == null)
            {
                break;
            }
            sessionToCanonicalPath.remove(sessionId); // clean up orphan mapping on the rare ID collision
        }
        while (true);

        logger.info("Attached session {} to path {} (refCount={})", sessionId, canonical, sharedFile.refCount().get());
        return AttachResult.ofSuccess(sessionId);
    }

    /**
     * Returns an existing {@link EditorSession} for the given canonical JSON file path, if any
     * session is currently attached to it. Useful for "focus existing window" UX.
     *
     * @param canonicalPath the canonical path of the JSON file (as returned by {@link File#getCanonicalPath()})
     * @return the first session found for the path, or {@link Optional#empty()} if none
     */
    public Optional<EditorSession> getSessionByCanonicalPath(final String canonicalPath)
    {
        if (!filesByPath.containsKey(canonicalPath))
        {
            return Optional.empty();
        }
        return sessionToCanonicalPath.entrySet().stream()
                .filter(e -> canonicalPath.equals(e.getValue()))
                .findFirst()
                .map(e -> sessions.get(e.getKey()));
    }

    /**
     * Detaches a session created via {@link #attachSession}. Decrements the shared-file ref count;
     * when the last session for a path is detached the shared model is removed from the registry.
     *
     * @param sessionId the session ID returned by {@link #attachSession}
     */
    public void detachSession(final String sessionId)
    {
        // detachSession is the internal teardown call from window-close / MCP-client-disconnect; closeFile is the MCP-protected variant.
        sessions.remove(sessionId);
        decrementRefCount(sessionId);
        logger.info("Detached session {}", sessionId);
    }

    // Decrements filesByPath ref count for this session; evicts model when count reaches zero.
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
                    // TODO: ModelImpl currently has no dispose() — listener/thread cleanup relies on GC.
                    // Revisit when adding closable resources to the model.
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
