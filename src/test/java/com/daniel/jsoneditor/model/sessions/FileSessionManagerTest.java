package com.daniel.jsoneditor.model.sessions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.WritableModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class FileSessionManagerTest
{
    @TempDir
    Path tempDir;

    private static final String SIMPLE_JSON = "{\"name\":\"test\",\"value\":42}";

    private static final String SIMPLE_SCHEMA =
            "{\"$schema\":\"http://json-schema.org/draft-07/schema#\","
            + "\"type\":\"object\","
            + "\"properties\":{"
            + "\"name\":{\"type\":\"string\"},"
            + "\"value\":{\"type\":\"number\"}"
            + "}}";

    private FileSessionManager sessionManager;
    private Path jsonFile;
    private Path schemaFile;

    @BeforeEach
    void setUp() throws Exception
    {
        sessionManager = new FileSessionManager();
        jsonFile = tempDir.resolve("test.json");
        schemaFile = tempDir.resolve("schema.json");
        Files.writeString(jsonFile, SIMPLE_JSON);
        Files.writeString(schemaFile, SIMPLE_SCHEMA);
    }

    // ─── Gap 2+4: MCP-GUI shared lifecycle, mutation visibility, and refcount correctness ───

    @Test
    void testOpenFileCreatesSession()
    {
        final AttachResult openResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(openResult.success(), "attachSession must succeed");
        assertNull(openResult.error(), "error must be null on success");
        final String id = openResult.sessionId();
        assertFalse(id.isEmpty(), "Session ID must not be empty");

        final EditorSession session = sessionManager.getSession(id);
        assertNotNull(session, "getSession must return the opened session");
        assertEquals(id, session.id(), "Session ID must match");
        assertFalse(session.guiOwned(), "Headless session must not be GUI-owned");
        assertEquals(jsonFile.toFile(), session.jsonFile(), "JSON file must match");
        assertEquals(schemaFile.toFile(), session.schemaFile(), "Schema file must match");
        assertNotNull(session.model(), "Session model must not be null");
    }

    @Test
    void testOpenInvalidFileReturnsNull()
    {
        final String nonExistentJson = tempDir.resolve("no-such.json").toString();
        final String nonExistentSchema = tempDir.resolve("no-such-schema.json").toString();

        final AttachResult r1 = sessionManager.attachSession(nonExistentJson, nonExistentSchema, false);
        assertFalse(r1.success(), "attachSession with non-existent JSON and schema must fail");
        assertNotNull(r1.error(), "error message must be present");
        assertTrue(r1.error().contains("does not exist"), "error must mention missing file");

        final AttachResult r2 = sessionManager.attachSession(jsonFile.toString(), nonExistentSchema, false);
        assertFalse(r2.success(), "attachSession with existing JSON but missing schema must fail");
        assertNotNull(r2.error(), "error message must be present");
        assertTrue(r2.error().contains("does not exist"), "error must mention missing schema");
    }

    @Test
    void testCloseFileRemovesSession()
    {
        final AttachResult openResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(openResult.success(), "Precondition: session must open successfully");
        final String id = openResult.sessionId();

        final CloseFileResult closeResult = sessionManager.closeFile(id);
        assertEquals(CloseFileResult.CLOSED, closeResult, "closeFile must return CLOSED for a valid headless session");

        assertNull(sessionManager.getSession(id), "getSession must return null after close");
        assertTrue(sessionManager.listSessions().isEmpty(), "listSessions must be empty after close");
    }

    @Test
    void testCannotCloseGuiSession()
    {
        // Open a headless session to get a valid ReadableModel instance
        final AttachResult headlessResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(headlessResult.success(), "Precondition: headless session must open");
        final String headlessId = headlessResult.sessionId();
        final EditorSession headlessSession = sessionManager.getSession(headlessId);

        // Register as GUI session (guiOwned=true)
        final String guiId = sessionManager.registerGuiSession(
                headlessSession.model(), jsonFile.toFile(), schemaFile.toFile());
        assertNotNull(guiId, "registerGuiSession must return a session ID");
        assertTrue(sessionManager.getSession(guiId).guiOwned(), "GUI session must be marked guiOwned");

        // Attempting to close the GUI session via closeFile must fail
        final CloseFileResult closeResult = sessionManager.closeFile(guiId);
        assertEquals(CloseFileResult.GUI_OWNED, closeResult, "closeFile must return GUI_OWNED for a GUI-owned session");
        assertNotNull(sessionManager.getSession(guiId), "GUI session must still exist after failed close");
    }

    @Test
    void testUnregisterGuiSession()
    {
        final AttachResult headlessResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(headlessResult.success(), "Precondition: headless session must open");
        final String headlessId = headlessResult.sessionId();
        final EditorSession headlessSession = sessionManager.getSession(headlessId);

        final String guiId = sessionManager.registerGuiSession(
                headlessSession.model(), jsonFile.toFile(), schemaFile.toFile());
        assertNotNull(sessionManager.getSession(guiId), "GUI session must exist before unregister");

        sessionManager.unregisterGuiSession(guiId);
        assertNull(sessionManager.getSession(guiId),
                "GUI session must be gone after unregisterGuiSession");
    }

    @Test
    void testListSessionsReturnsAll() throws Exception
    {
        assertTrue(sessionManager.listSessions().isEmpty(), "Manager must start with no sessions");

        final Path jsonFile2 = tempDir.resolve("test2.json");
        final Path schemaFile2 = tempDir.resolve("schema2.json");
        Files.writeString(jsonFile2, "{\"a\":1}");
        Files.writeString(schemaFile2,
                "{\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{\"a\":{\"type\":\"integer\"}}}");

        final AttachResult result1 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        final AttachResult result2 = sessionManager.attachSession(jsonFile2.toString(), schemaFile2.toString(), false);
        assertTrue(result1.success(), "First session must open");
        assertTrue(result2.success(), "Second session must open");
        final String id1 = result1.sessionId();
        final String id2 = result2.sessionId();

        final List<EditorSession> sessions = sessionManager.listSessions();
        assertEquals(2, sessions.size(), "listSessions must return exactly 2 sessions");
        final List<String> ids = sessions.stream().map(EditorSession::id).toList();
        assertTrue(ids.contains(id1), "list must contain id1");
        assertTrue(ids.contains(id2), "list must contain id2");
    }

    @Test
    void testConcurrentAccess() throws Exception
    {
        final int threadCount = 10;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final AtomicInteger errorCount = new AtomicInteger(0);
        final List<String> openedIds = new ArrayList<>();

        // Pre-create temp files for each thread before concurrent execution
        final List<Path> jsonFiles = new ArrayList<>();
        final List<Path> schemaFiles = new ArrayList<>();
        for (int i = 0; i < threadCount; i++)
        {
            final Path jf = tempDir.resolve("concurrent-" + i + ".json");
            final Path sf = tempDir.resolve("concurrent-schema-" + i + ".json");
            Files.writeString(jf, "{\"n\":" + i + "}");
            Files.writeString(sf,
                    "{\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                    + "\"type\":\"object\","
                    + "\"properties\":{\"n\":{\"type\":\"integer\"}}}");
            jsonFiles.add(jf);
            schemaFiles.add(sf);
        }

        // Each thread attaches a file then immediately closes it
        for (int i = 0; i < threadCount; i++)
        {
            final int index = i;
            executor.submit(() ->
            {
                try
                {
                    startLatch.await();
                    final AttachResult openResult = sessionManager.attachSession(
                            jsonFiles.get(index).toString(), schemaFiles.get(index).toString(), false);
                    if (!openResult.success())
                    {
                        errorCount.incrementAndGet();
                    }
                    else
                    {
                        synchronized (openedIds)
                        {
                            openedIds.add(openResult.sessionId());
                        }
                        sessionManager.closeFile(openResult.sessionId());
                    }
                }
                catch (final Exception e)
                {
                    errorCount.incrementAndGet();
                }
                finally
                {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All threads must finish within 30 seconds");
        executor.shutdown();

        assertEquals(0, errorCount.get(), "No threads must encounter errors during concurrent access");
        assertEquals(threadCount, openedIds.size(), "All threads must have opened a session");
        assertTrue(sessionManager.listSessions().isEmpty(), "All sessions must be closed after concurrent test");
    }

    @Test
    void testOpenFileWithInvalidJson() throws Exception
    {
        // JSON has a string where the schema expects a number - validation must reject it
        final Path invalidJson = tempDir.resolve("invalid.json");
        Files.writeString(invalidJson, "{\"name\":\"test\",\"value\":\"not-a-number\"}");

        final AttachResult result = sessionManager.attachSession(invalidJson.toString(), schemaFile.toString(), false);

        assertFalse(result.success(), "attachSession must fail when JSON does not validate against schema");
        assertNotNull(result.error(), "error message must be present");
        assertFalse(result.error().isBlank(), "error message must not be blank");
        assertTrue(result.error().toLowerCase().contains("schema") || result.error().toLowerCase().contains("validat"),
                "error must mention schema or validation, got: " + result.error());
    }

    @Test
    void testCloseSessionThenAccess()
    {
        final AttachResult openResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(openResult.success(), "Precondition: session must open");
        final String id = openResult.sessionId();

        sessionManager.closeFile(id);

        assertNull(sessionManager.getSession(id), "getSession must return null after session is closed");
    }

    // ─── Phase A: deduplication by file path ───────────────────────────────────

    @Test
    void attachSession_dedupLifecycle()
    {
        // Step 1: first attach creates a new session and model
        final AttachResult result1 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(result1.success(), "first attach must succeed");
        final String id1 = result1.sessionId();
        assertFalse(id1.isEmpty(), "sessionId must not be empty");
        final ReadableModel model1 = sessionManager.getSession(id1).model();
        assertNotNull(model1, "first attach must produce a non-null model");

        // Step 2: second attach to same path reuses the same model
        final AttachResult result2 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(result2.success(), "second attach must succeed");
        final String id2 = result2.sessionId();
        assertNotEquals(id1, id2, "each attach must produce a unique sessionId");
        assertSame(model1, sessionManager.getSession(id2).model(),
                "both sessions on the same path must share one ModelImpl");

        // Step 3: detach first session — model stays alive because second session holds it
        sessionManager.detachSession(id1);
        assertNull(sessionManager.getSession(id1), "detached session must not be accessible");
        assertNotNull(sessionManager.getSession(id2), "second session must survive first detach");
        assertSame(model1, sessionManager.getSession(id2).model(),
                "surviving session must still hold the original model");

        // Step 4: close the last session via closeFile — model must be evicted
        final CloseFileResult closeResult = sessionManager.closeFile(id2);
        assertEquals(CloseFileResult.CLOSED, closeResult, "closeFile must return CLOSED for last session");
        assertNull(sessionManager.getSession(id2), "closed session must not be accessible");

        // Step 5: re-attach after eviction must produce a fresh model (not the old one)
        final AttachResult result3 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(result3.success(), "re-attach after full eviction must succeed");
        assertNotSame(model1, sessionManager.getSession(result3.sessionId()).model(),
                "re-attach after eviction must create a fresh ModelImpl, not reuse the old one");
    }

    @Test
    void attachSession_rejectsSchemaMismatch() throws Exception
    {
        // A different schema — same JSON is valid against it but it differs from schemaFile
        final Path schema2 = tempDir.resolve("schema2.json");
        Files.writeString(schema2,
                "{\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{"
                + "\"name\":{\"type\":\"string\"}"
                + "}}");

        final AttachResult result1 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(result1.success(), "first attach must succeed");

        // Second attach with a different schema must be rejected
        final AttachResult result2 = sessionManager.attachSession(jsonFile.toString(), schema2.toString(), false);
        assertFalse(result2.success(), "attach with mismatched schema must fail");
        assertNotNull(result2.error(), "error must be present on schema mismatch");
        assertTrue(result2.error().toLowerCase().contains("schema"),
                "error must mention 'schema', got: " + result2.error());

        // Original session must remain accessible and unaffected
        assertNotNull(sessionManager.getSession(result1.sessionId()),
                "original session must remain accessible after rejected attach");
    }

    @Test
    void attachSession_concurrentSamePathSharesOneModel() throws Exception
    {
        final int threadCount = 10;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final List<Future<AttachResult>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++)
        {
            futures.add(executor.submit(() ->
            {
                startLatch.await();
                return sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
            }));
        }

        startLatch.countDown();

        final List<AttachResult> results = new ArrayList<>();
        for (final Future<AttachResult> f : futures)
        {
            results.add(f.get(5, TimeUnit.SECONDS));
        }
        executor.shutdown();

        // All attaches must succeed with unique session IDs
        final Set<String> sessionIds = new HashSet<>();
        for (final AttachResult r : results)
        {
            assertTrue(r.success(), "all concurrent attaches must succeed: " + r.error());
            sessionIds.add(r.sessionId());
        }
        assertEquals(threadCount, sessionIds.size(), "each concurrent attach must produce a unique sessionId");

        // All sessions must share one ModelImpl instance
        final ReadableModel referenceModel = sessionManager.getSession(results.get(0).sessionId()).model();
        for (final AttachResult r : results)
        {
            assertSame(referenceModel, sessionManager.getSession(r.sessionId()).model(),
                    "all concurrent attaches to same path must share one ModelImpl");
        }
    }

    @Test
    void attachSession_mutationVisibleAcrossSessions()
    {
        // Attach two sessions to the same path — they share one ModelImpl
        final AttachResult result1 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        final AttachResult result2 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(result1.success(), "first attach must succeed");
        assertTrue(result2.success(), "second attach must succeed");
        assertSame(sessionManager.getSession(result1.sessionId()).model(),
                sessionManager.getSession(result2.sessionId()).model(),
                "precondition: both sessions must share one ModelImpl");

        // Mutate via session1's model (cast to WritableModel — ModelImpl implements both interfaces)
        final WritableModel writable = (WritableModel) sessionManager.getSession(result1.sessionId()).model();
        final ObjectNode newRoot = new ObjectMapper().createObjectNode();
        newRoot.put("mutated", true);
        writable.resetRootNode(newRoot);

        // Mutation must be immediately visible via session2's model (proves shared state: GUI sees MCP edits)
        assertSame(newRoot, sessionManager.getSession(result2.sessionId()).model().getRootJson(),
                "write via session1 must be visible via session2 — GUI sees MCP edits at the model layer");
    }

    @Test
    void attachSession_sharedModelLifecycleAcrossMcpAndGui()
    {
        // ── Phase A: MCP-first attach, then GUI ───────────────────────────────────────

        // A1. Attach MCP session
        final AttachResult mcpResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(mcpResult.success(), "Phase A: MCP attach must succeed");
        final String idMcp = mcpResult.sessionId();

        // A2. Attach GUI session — must reuse the already-loaded model
        final AttachResult guiResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), true);
        assertTrue(guiResult.success(), "Phase A: GUI attach must succeed");
        final String idGui = guiResult.sessionId();

        // A3. Both sessions present in registry
        assertEquals(2, sessionManager.listSessions().size(), "Phase A: both sessions must be listed");
        assertNotNull(sessionManager.getSession(idMcp), "Phase A: MCP session must be accessible");
        assertNotNull(sessionManager.getSession(idGui), "Phase A: GUI session must be accessible");

        // A4. Same ModelImpl instance shared
        assertSame(sessionManager.getSession(idMcp).model(), sessionManager.getSession(idGui).model(),
                "Phase A: MCP and GUI sessions must share one ModelImpl");

        // A5. Mutate via MCP session — observe via GUI session (proves shared in-memory state)
        final WritableModel writable = (WritableModel) sessionManager.getSession(idMcp).model();
        final ObjectNode mutatedRoot = new ObjectMapper().createObjectNode();
        mutatedRoot.put("phase", "A");
        writable.resetRootNode(mutatedRoot);
        assertSame(mutatedRoot, sessionManager.getSession(idGui).model().getRootJson(),
                "Phase A: mutation via MCP session must be visible via GUI session (shared model)");

        final ReadableModel sharedModelA = sessionManager.getSession(idMcp).model();

        // A6. closeFile(idMcp): MCP gone, GUI session and model survive (refcount 2→1)
        final CloseFileResult closeResultA = sessionManager.closeFile(idMcp);
        assertEquals(CloseFileResult.CLOSED, closeResultA, "Phase A: closeFile must return CLOSED for headless session");
        assertNull(sessionManager.getSession(idMcp), "Phase A: MCP session must be gone after closeFile");
        assertNotNull(sessionManager.getSession(idGui), "Phase A: GUI session must survive MCP closeFile");
        assertNotNull(sessionManager.getSession(idGui).model().getRootJson(),
                "Phase A: GUI model must still be functional after MCP closeFile");

        // A7. unregisterGuiSession(idGui): refcount 1→0, model evicted; fresh attach confirms eviction
        sessionManager.unregisterGuiSession(idGui);
        assertNull(sessionManager.getSession(idGui), "Phase A: GUI session must be gone after unregisterGuiSession");

        final AttachResult phaseAProbe = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(phaseAProbe.success(), "Phase A: re-attach after eviction must succeed");
        final ReadableModel freshModelA = sessionManager.getSession(phaseAProbe.sessionId()).model();
        assertNotSame(sharedModelA, freshModelA,
                "Phase A: re-attach must produce a fresh ModelImpl (old model was evicted)");
        assertNull(freshModelA.getRootJson().get("phase"),
                "Phase A: fresh model reads from disk — in-memory mutation is gone (eviction confirmed)");

        // Close eviction probe before Phase B
        sessionManager.closeFile(phaseAProbe.sessionId());

        // ── Phase B: GUI-first attach, then MCP (reverse close order) ────────────────

        // B1. Attach GUI session first — fresh model (Phase A fully evicted)
        final AttachResult guiResult2 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), true);
        assertTrue(guiResult2.success(), "Phase B: GUI attach must succeed");
        final String idGui2 = guiResult2.sessionId();
        final ReadableModel sharedModelB = sessionManager.getSession(idGui2).model();
        assertNotSame(sharedModelA, sharedModelB,
                "Phase B: must start with a new model instance (Phase A model was evicted)");

        // B2. Attach MCP session — must reuse the already-loaded model
        final AttachResult mcpResult2 = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(mcpResult2.success(), "Phase B: MCP attach must succeed");
        final String idMcp2 = mcpResult2.sessionId();

        // B3. Same ModelImpl shared
        assertSame(sharedModelB, sessionManager.getSession(idMcp2).model(),
                "Phase B: GUI and MCP sessions must share one ModelImpl");

        // B4. unregisterGuiSession first — MCP session survives with same model (refcount 2→1)
        sessionManager.unregisterGuiSession(idGui2);
        assertNull(sessionManager.getSession(idGui2), "Phase B: GUI session must be gone after unregisterGuiSession");
        assertNotNull(sessionManager.getSession(idMcp2), "Phase B: MCP session must survive GUI unregister");
        assertSame(sharedModelB, sessionManager.getSession(idMcp2).model(),
                "Phase B: MCP session must still hold the shared model after GUI unregistered");

        // B5. closeFile(idMcp2): refcount 1→0, model evicted
        final CloseFileResult closeResultB = sessionManager.closeFile(idMcp2);
        assertEquals(CloseFileResult.CLOSED, closeResultB, "Phase B: closeFile must return CLOSED for last headless session");
        assertNull(sessionManager.getSession(idMcp2), "Phase B: MCP session must be gone after closeFile");

        // B6. Re-attach proves order-independence of the refcount lifecycle
        final AttachResult phaseBFresh = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(phaseBFresh.success(), "Phase B: re-attach after eviction must succeed");
        assertNotSame(sharedModelB, sessionManager.getSession(phaseBFresh.sessionId()).model(),
                "Phase B: re-attach must produce a fresh ModelImpl (order-independence of eviction proven)");
    }

    // ─── Gap 3: Refcount underflow protection ──────────────────────────────────

    @Test
    void detachSession_doubleDetach_isNoOpWithoutException()
    {
        // Attach one session (refcount=1)
        final AttachResult result = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(result.success(), "attach must succeed");
        final String id = result.sessionId();

        // First detach removes the session and evicts the file (refcount reaches 0)
        sessionManager.detachSession(id);
        assertNull(sessionManager.getSession(id), "session must be gone after first detach");

        // Second detach on the same id must be a no-op — no exception, no negative refcount
        assertDoesNotThrow(() -> sessionManager.detachSession(id),
                "second detach on the same sessionId must not throw");

        // Subsequent attach on the same path must produce a clean working session
        final AttachResult freshResult = sessionManager.attachSession(jsonFile.toString(), schemaFile.toString(), false);
        assertTrue(freshResult.success(), "fresh attach after double detach must succeed");
        assertNotNull(sessionManager.getSession(freshResult.sessionId()), "fresh session must be accessible");
    }
}
