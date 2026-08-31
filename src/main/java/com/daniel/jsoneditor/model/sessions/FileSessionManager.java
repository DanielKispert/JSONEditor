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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages multiple open file sessions. Used by both GUI and MCP server.
 * GUI sessions are protected from being closed via MCP.
 */
public class FileSessionManager
{
    private static final Logger logger = LoggerFactory.getLogger(FileSessionManager.class);

    private static final String GUI_SESSION_PREFIX = "gui-";

    private static final int MAX_SESSIONS = 64;

    private final Map<String, EditorSession> sessions = new ConcurrentHashMap<>();

    /**
     * Tracks a file that may be shared across multiple sessions.
     * {@code canonicalSchemaPath} is stored so schema-mismatch checks never need I/O inside a lock.
     */
    private record SharedFile(
            String canonicalPath,
            String canonicalSchemaPath,
            ModelImpl model,
            File jsonFile,
            File schemaFile,
            AtomicInteger refCount) {}

    /**
     * Typed result of a single attach attempt — replaces the raw {@code String[] errorHolder} side-channel.
     */
    private record AttachAttempt(SharedFile shared, String error)
    {
        static AttachAttempt ofShared(final SharedFile shared)
        {
            return new AttachAttempt(shared, null);
        }

        static AttachAttempt ofError(final String error)
        {
            return new AttachAttempt(null, error);
        }

        boolean isSuccess()
        {
            return error == null;
        }
    }

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
            sessionId = generateUniqueId(GUI_SESSION_PREFIX);
            session = new EditorSession(sessionId, model, jsonFile, schemaFile, true);
        }
        while (sessions.putIfAbsent(sessionId, session) != null);
        logger.info("Registered GUI session {} for {}", sessionId, jsonFile != null ? jsonFile.getAbsolutePath() : "null");
        return sessionId;
    }

    /**
     * Unregisters a GUI session (called when GUI closes a file).
     * Delegates to {@link #detachSession(String)} to ensure the shared-file ref count is
     * decremented and the session is removed from {@code sessionToCanonicalPath}.
     *
     * @param sessionId the session to unregister
     */
    public void unregisterGuiSession(final String sessionId)
    {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        final EditorSession session = sessions.get(sessionId);
        if (session != null && session.guiOwned())
        {
            logger.info("Unregistered GUI session {}", sessionId);
            detachSession(sessionId);
        }
        else if (session != null)
        {
            logger.warn("unregisterGuiSession called with non-GUI session id {}", sessionId);
        }
    }

    /**
     * Closes a headless session. Refuses to close GUI-owned sessions.
     * Also decrements the shared-file ref count via {@link #decrementRefCount(String)}.
     *
     * @param sessionId the session to close
     * @return {@link CloseFileResult#CLOSED} if closed, {@link CloseFileResult#NOT_FOUND} if not found,
     *         {@link CloseFileResult#GUI_OWNED} if the session is GUI-owned
     */
    public CloseFileResult closeSession(final String sessionId)
    {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
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
        Objects.requireNonNull(sessionId, "sessionId must not be null");
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

        // Pre-canonicalize both paths before any map operations — no I/O inside locks
        final String canonicalJson;
        final String canonicalSchema;
        try
        {
            canonicalJson = jsonFile.getCanonicalPath();
            canonicalSchema = schemaFile.getCanonicalPath();
        }
        catch (final IOException e)
        {
            return AttachResult.ofError("Cannot resolve canonical path: " + e.getMessage());
        }

        if (sessions.size() >= MAX_SESSIONS)
        {
            return AttachResult.ofError("Maximum number of concurrent sessions (" + MAX_SESSIONS + ") reached");
        }

        final AttachAttempt attempt = getOrCreateSharedFile(
                jsonFile, schemaFile, canonicalJson, canonicalSchema, jsonPath, schemaPath);
        if (!attempt.isSuccess())
        {
            return AttachResult.ofError(attempt.error());
        }
        final SharedFile sharedFile = attempt.shared();

        final String prefix = guiOwned ? GUI_SESSION_PREFIX : "";
        EditorSession session;
        String sessionId;
        // Insert canonical mapping BEFORE session is visible in `sessions` to close the
        // TOCTOU gap. On the rare event of a sessionId collision, clean up the orphan mapping and retry.
        do
        {
            sessionId = generateUniqueId(prefix);
            session = new EditorSession(sessionId, sharedFile.model(), sharedFile.jsonFile(), sharedFile.schemaFile(), guiOwned);
            sessionToCanonicalPath.put(sessionId, canonicalJson);
            if (sessions.putIfAbsent(sessionId, session) == null)
            {
                break;
            }
            sessionToCanonicalPath.remove(sessionId); // clean up orphan mapping on the rare ID collision
        }
        while (true);

        if (guiOwned)
        {
            removeOrphanedHeadlessSessionsForPath(canonicalJson, sessionId);
        }
        logger.info("Attached session {} to path {} (refCount={})", sessionId, canonicalJson, sharedFile.refCount().get());
        return AttachResult.ofSuccess(sessionId);
    }

    /**
     * Retrieves the existing {@link SharedFile} for the given canonical JSON path (incrementing its
     * ref-count), or builds a brand-new one. All blocking I/O happens BEFORE any {@code compute()}
     * call so no {@link ConcurrentHashMap} bucket lock is held during disk access.
     *
     * <p>Dedup correctness: concurrent slow-path races are resolved inside the final {@code compute()}
     * call — the thread that loses the race discards its freshly-built model and increments the
     * winner's ref-count instead.</p>
     */
    private AttachAttempt getOrCreateSharedFile(
            final File jsonFile,
            final File schemaFile,
            final String canonicalJson,
            final String canonicalSchema,
            final String jsonPath,
            final String schemaPath)
    {
        // ── Fast path: peek without holding a lock ────────────────────────────────
        final SharedFile peeked = filesByPath.get(canonicalJson);
        if (peeked != null)
        {
            afterFastPathPeek(canonicalJson);
            // Increment refCount inside compute() so it is atomic with concurrent decrements
            final AttachAttempt[] fastResult = {null};
            filesByPath.compute(canonicalJson, (final String key, final SharedFile cur) ->
            {
                if (cur == null)
                {
                    return null; // evicted between get() and compute(); caller falls through to slow path
                }
                // Re-verify schema: another thread may have evicted and reinstalled with a different schema
                if (!cur.canonicalSchemaPath().equals(canonicalSchema))
                {
                    fastResult[0] = AttachAttempt.ofError("Schema mismatch: path " + canonicalJson
                            + " is already open with schema " + cur.canonicalSchemaPath()
                            + " but requested " + canonicalSchema);
                    return cur; // leave the entry unchanged; do not increment
                }
                cur.refCount().incrementAndGet();
                fastResult[0] = AttachAttempt.ofShared(cur);
                return cur;
            });
            if (fastResult[0] != null)
            {
                return fastResult[0];
            }
            // Entry was evicted between the peek and the compute; fall through to slow path
        }

        // ── Slow path: build model entirely outside any lock ─────────────────────
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
            return AttachAttempt.ofError("Failed to parse files: " + e.getMessage());
        }
        if (json == null || schema == null)
        {
            return AttachAttempt.ofError("Failed to parse JSON or schema files: " + jsonPath + " / " + schemaPath);
        }
        final List<String> validationErrors = SchemaHelper.validateJsonWithSchema(json, schema);
        if (!validationErrors.isEmpty())
        {
            return AttachAttempt.ofError("JSON does not validate against schema: " + String.join(", ", validationErrors));
        }
        final ModelImpl newModel;
        final SharedFile newShared;
        try
        {
            newModel = ModelFactory.createEmpty();
            newModel.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
            newShared = new SharedFile(
                    canonicalJson, canonicalSchema, newModel, jsonFile, schemaFile, new AtomicInteger(1));
        }
        catch (final RuntimeException e)
        {
            logger.error("Failed to initialize model for {}: {}", jsonPath, e.getMessage(), e);
            return AttachAttempt.ofError("Internal error: " + e.getMessage());
        }

        // Atomically install: if another thread won the race, use theirs and increment its refCount
        final AttachAttempt[] resultHolder = {null};
        filesByPath.compute(canonicalJson, (final String key, final SharedFile cur) ->
        {
            if (cur != null)
            {
                // Another thread installed first — verify schema compatibility then increment
                if (!cur.canonicalSchemaPath().equals(canonicalSchema))
                {
                    resultHolder[0] = AttachAttempt.ofError("Schema mismatch: path " + canonicalJson
                            + " is already open with schema " + cur.canonicalSchemaPath()
                            + " but requested " + canonicalSchema);
                    return cur;
                }
                cur.refCount().incrementAndGet();
                resultHolder[0] = AttachAttempt.ofShared(cur);
                return cur;
            }
            resultHolder[0] = AttachAttempt.ofShared(newShared);
            return newShared;
        });
        return resultHolder[0];
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
     * Closes all headless (non-GUI-owned) sessions.
     * Called during application shutdown to drain in-flight MCP sessions before the MCP server stops.
     * Collect IDs before iterating to avoid {@link java.util.ConcurrentModificationException}.
     */
    public void closeAllHeadlessSessions()
    {
        final List<String> headlessIds = sessions.entrySet().stream()
                .filter(e -> !e.getValue().guiOwned())
                .map(Map.Entry::getKey)
                .toList();
        for (final String id : headlessIds)
        {
            detachSession(id);
        }
    }

    /**
     * Removes this session from {@code sessionToCanonicalPath} and decrements the ref count in
     * {@code filesByPath}. When the count reaches zero the shared model is evicted from the map.
     * Called by both {@link #closeSession(String)} and {@link #detachSession(String)}.
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

    /**
     * Removes any headless (non-GUI-owned) sessions that refer to {@code canonicalPath},
     * excluding the newly attached GUI session identified by {@code guiSessionId}.
     * Called immediately after a GUI session is committed to {@code sessions} to prevent
     * orphaned headless sessions (created by repeated show_gui calls) from accumulating.
     */
    private void removeOrphanedHeadlessSessionsForPath(final String canonicalPath, final String guiSessionId)
    {
        final List<String> orphanIds = sessions.entrySet().stream()
                .filter(e -> !e.getValue().guiOwned()
                        && !e.getKey().equals(guiSessionId)
                        && canonicalPath.equals(sessionToCanonicalPath.get(e.getKey())))
                .map(Map.Entry::getKey)
                .toList();
        for (final String orphanId : orphanIds)
        {
            logger.info("Removing orphaned headless session {} for path {} after GUI session attached",
                    orphanId, canonicalPath);
            detachSession(orphanId);
        }
    }

    /**
     * Hook for testing concurrent fast-path/slow-path races. Override in test subclasses to inject
     * a delay between the fast-path peek and the subsequent {@code compute()} call.
     * Empty in production.
     */
    protected void afterFastPathPeek(final String canonicalJson)
    {
    }

    private String generateUniqueId(final String prefix)
    {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}
