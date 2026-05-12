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

import static org.junit.jupiter.api.Assertions.*;


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

    @Test
    void testOpenFileCreatesSession()
    {
        final OpenFileResult openResult = sessionManager.openFile(jsonFile.toString(), schemaFile.toString());
        assertTrue(openResult.success(), "openFile must succeed");
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

        final OpenFileResult r1 = sessionManager.openFile(nonExistentJson, nonExistentSchema);
        assertFalse(r1.success(), "openFile with non-existent JSON and schema must fail");
        assertNotNull(r1.error(), "error message must be present");
        assertTrue(r1.error().contains("does not exist"), "error must mention missing file");

        final OpenFileResult r2 = sessionManager.openFile(jsonFile.toString(), nonExistentSchema);
        assertFalse(r2.success(), "openFile with existing JSON but missing schema must fail");
        assertNotNull(r2.error(), "error message must be present");
        assertTrue(r2.error().contains("does not exist"), "error must mention missing schema");
    }

    @Test
    void testCloseFileRemovesSession()
    {
        final OpenFileResult openResult = sessionManager.openFile(jsonFile.toString(), schemaFile.toString());
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
        final OpenFileResult headlessResult = sessionManager.openFile(jsonFile.toString(), schemaFile.toString());
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
        final OpenFileResult headlessResult = sessionManager.openFile(jsonFile.toString(), schemaFile.toString());
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

        final OpenFileResult result1 = sessionManager.openFile(jsonFile.toString(), schemaFile.toString());
        final OpenFileResult result2 = sessionManager.openFile(jsonFile2.toString(), schemaFile2.toString());
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

        // Each thread opens a file then immediately closes it
        for (int i = 0; i < threadCount; i++)
        {
            final int index = i;
            executor.submit(() ->
            {
                try
                {
                    startLatch.await();
                    final OpenFileResult openResult = sessionManager.openFile(
                            jsonFiles.get(index).toString(), schemaFiles.get(index).toString());
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

        final OpenFileResult result = sessionManager.openFile(invalidJson.toString(), schemaFile.toString());

        assertFalse(result.success(), "openFile must fail when JSON does not validate against schema");
        assertNotNull(result.error(), "error message must be present");
        assertFalse(result.error().isBlank(), "error message must not be blank");
        assertTrue(result.error().toLowerCase().contains("schema") || result.error().toLowerCase().contains("validat"),
                "error must mention schema or validation, got: " + result.error());
    }

    @Test
    void testCloseSessionThenAccess()
    {
        final OpenFileResult openResult = sessionManager.openFile(jsonFile.toString(), schemaFile.toString());
        assertTrue(openResult.success(), "Precondition: session must open");
        final String id = openResult.sessionId();

        sessionManager.closeFile(id);

        assertNull(sessionManager.getSession(id), "getSession must return null after session is closed");
    }
}
