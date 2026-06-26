package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.controller.AppService;
import com.daniel.jsoneditor.controller.WindowRegistry;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShowGuiToolTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AppService appService;
    private FileSessionManager sessionManager;
    private ShowGuiTool tool;
    private WindowRegistry windowRegistry;

    @BeforeEach
    void setUp()
    {
        appService = Mockito.mock(AppService.class);
        sessionManager = Mockito.mock(FileSessionManager.class);
        windowRegistry = Mockito.mock(WindowRegistry.class);
        Mockito.when(appService.getWindowRegistry()).thenReturn(windowRegistry);
        Mockito.when(windowRegistry.findByPath(Mockito.anyString())).thenReturn(Optional.empty());
        tool = new ShowGuiTool(appService, sessionManager);
    }

    @Test
    void rejectsInvalidArguments() throws Exception
    {
        Mockito.when(appService.isShuttingDown()).thenReturn(false);

        // both params — mutually exclusive
        assertError(callTool("{\"session_id\":\"x\",\"json_path\":\"/f.json\"}"), "mutually exclusive");

        // neither param
        assertError(callTool("{}"), "Either session_id or json_path");

        // unknown session_id
        Mockito.when(sessionManager.getSession("bad-id")).thenReturn(null);
        assertError(callTool("{\"session_id\":\"bad-id\"}"), "bad-id");

        // json_path without schema_path
        assertError(callTool("{\"json_path\":\"/data/file.json\"}"), "schema_path is required");

        // shutting down — checked regardless of other params
        Mockito.when(appService.isShuttingDown()).thenReturn(true);
        assertError(callTool("{\"session_id\":\"any\"}"), "shutting down");
    }

    @Test
    void opensWindowForExistingSession() throws Exception
    {
        Mockito.when(appService.isShuttingDown()).thenReturn(false);
        final EditorSession session = new EditorSession(
                "session-1", null, new File("/data/file.json"), new File("/data/schema.json"), false);
        Mockito.when(sessionManager.getSession("session-1")).thenReturn(session);

        final JsonNode payload = callAndParsePayload("{\"session_id\":\"session-1\"}");

        assertEquals("session-1", payload.path("session_id").asText());
        assertFalse(payload.path("gui_state").asText().isEmpty(), "Expected gui_state in response");
        assertFalse(payload.path("note").asText().isEmpty(), "Expected note in response");
    }

    @Test
    void opensWindowForNewFile() throws Exception
    {
        Mockito.when(appService.isShuttingDown()).thenReturn(false);

        // without settings
        JsonNode payload = callAndParsePayload(
                "{\"json_path\":\"/data/file.json\",\"schema_path\":\"/data/schema.json\"}");
        assertFalse(payload.path("gui_state").asText().isEmpty(), "Expected gui_state in response");

        // with settings
        payload = callAndParsePayload(
                "{\"json_path\":\"/data/file.json\",\"schema_path\":\"/data/schema.json\",\"settings_path\":\"/data/settings.json\"}");
        assertFalse(payload.path("gui_state").asText().isEmpty(), "Expected gui_state with settings");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void assertError(final JsonNode response, final String expectedFragment)
    {
        assertNotNull(response.get("error"),
                "Expected error response but got: " + response);
        assertTrue(response.path("error").path("message").asText().contains(expectedFragment),
                "Expected '" + expectedFragment + "' in error message, got: "
                        + response.path("error").path("message").asText());
    }

    private JsonNode callTool(final String argsJson) throws Exception
    {
        final JsonNode args = MAPPER.readTree(argsJson);
        final JsonNode id = MAPPER.readTree("1");
        return MAPPER.readTree(tool.execute(args, id));
    }

    private JsonNode callAndParsePayload(final String argsJson) throws Exception
    {
        final JsonNode response = callTool(argsJson);
        assertNull(response.get("error"), "Expected no error but got: " + response);
        final String text = response.path("result").path("content").get(0).path("text").asText();
        return MAPPER.readTree(text);
    }
}
