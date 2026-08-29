package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.controller.AppService;

import com.daniel.jsoneditor.model.sessions.AttachResult;
import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class ShowGuiToolTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AppService appService;
    private FileSessionManager sessionManager;
    private ShowGuiTool tool;

    @BeforeEach
    void setUp()
    {
        appService = Mockito.mock(AppService.class);
        sessionManager = Mockito.mock(FileSessionManager.class);
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

        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            final JsonNode payload = callAndParsePayload("{\"session_id\":\"session-1\"}");

            assertEquals("session-1", payload.path("session_id").asText());
            assertFalse(payload.path("gui_state").asText().isEmpty(), "Expected gui_state in response");
            assertFalse(payload.path("note").asText().isEmpty(), "Expected note in response");
        }
    }

    @Test
    void opensWindowForNewFile() throws Exception
    {
        Mockito.when(appService.isShuttingDown()).thenReturn(false);
        Mockito.when(sessionManager.attachSession(Mockito.anyString(), Mockito.anyString(), Mockito.eq(false)))
                .thenReturn(AttachResult.ofSuccess("new-session-id"));

        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            // without settings — verify gui_state and session_id are returned
            JsonNode payload = callAndParsePayload(
                    "{\"json_path\":\"/data/file.json\",\"schema_path\":\"/data/schema.json\"}");
            assertFalse(payload.path("gui_state").asText().isEmpty(), "Expected gui_state in response");
            assertEquals("new-session-id", payload.path("session_id").asText(), "Expected session_id in response");

            // with settings (file does not exist on disk so settings application is skipped)
            payload = callAndParsePayload(
                    "{\"json_path\":\"/data/file.json\",\"schema_path\":\"/data/schema.json\",\"settings_path\":\"/data/settings.json\"}");
            assertFalse(payload.path("gui_state").asText().isEmpty(), "Expected gui_state with settings");
            assertEquals("new-session-id", payload.path("session_id").asText(), "Expected session_id with settings");
        }
    }

    @Test
    void guiState_derivedFromLiveSessions_notWindowRegistry() throws Exception
    {
        Mockito.when(appService.isShuttingDown()).thenReturn(false);

        // Scenario A: a guiOwned session exists for /data/file.json — window is currently open.
        // The implementation must derive gui_state from the session list, not from WindowRegistry.
        final EditorSession guiSession = new EditorSession(
                "gui-open", null, new File("/data/file.json"), new File("/data/schema.json"), true);
        Mockito.when(sessionManager.listSessions()).thenReturn(List.of(guiSession));
        Mockito.when(sessionManager.getSession("gui-open")).thenReturn(guiSession);

        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            final JsonNode payload = callAndParsePayload("{\"session_id\":\"gui-open\"}");
            assertEquals("focused", payload.path("gui_state").asText(),
                    "gui_state must be 'focused' when a guiOwned session exists for the same file path");
        }

        // Scenario B: no guiOwned sessions exist — window was closed since last show_gui call.
        Mockito.when(sessionManager.listSessions()).thenReturn(List.of());
        Mockito.when(sessionManager.attachSession(
                Mockito.anyString(), Mockito.anyString(), Mockito.eq(false)))
                .thenReturn(AttachResult.ofSuccess("new-headless"));

        try (final MockedStatic<Platform> platformMock = mockStatic(Platform.class))
        {
            final JsonNode payload = callAndParsePayload(
                    "{\"json_path\":\"/data/file.json\",\"schema_path\":\"/data/schema.json\"}");
            assertEquals("opened", payload.path("gui_state").asText(),
                    "gui_state must be 'opened' when no guiOwned session exists for the file (window was closed)");
        }
    }

    // ── helpers ───────────────────────────────────

    private void assertError(final JsonNode response, final String expectedFragment)
    {
        assertNotNull(response.get("error"),
                "Expected error response but got: " + response);
        assertTrue(response.path("error").path("message").asText().contains(expectedFragment),
                "Expected ''" + expectedFragment + "' in error message, got: "
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
