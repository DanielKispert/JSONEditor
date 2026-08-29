package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.EditorSession;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ListSessionsTool. Verifies gui_state is reported correctly for each session type.
 */
class ListSessionsToolTest
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void listSessions_guiState_reflectsSessionOwnership() throws Exception
    {
        // Arrange: one gui-owned session (window open) and one headless session (no window)
        final FileSessionManager sessionManager = Mockito.mock(FileSessionManager.class);
        final EditorSession guiSession = new EditorSession(
                "gui-1", null, new File("/a.json"), new File("/a-schema.json"), true);
        final EditorSession headlessSession = new EditorSession(
                "mcp-1", null, new File("/b.json"), new File("/b-schema.json"), false);
        Mockito.when(sessionManager.listSessions()).thenReturn(List.of(guiSession, headlessSession));

        final ListSessionsTool tool = new ListSessionsTool(sessionManager);
        final JsonNode response = MAPPER.readTree(
                tool.execute(MAPPER.createObjectNode(), MAPPER.readTree("1")));

        assertNull(response.get("error"), "list_sessions must not return an error");
        final String text = response.path("result").path("content").get(0).path("text").asText();
        final JsonNode sessions = MAPPER.readTree(text);
        assertTrue(sessions.isArray(), "list_sessions payload must be an array");
        assertEquals(2, sessions.size(), "Must return exactly 2 sessions");

        // Locate each entry by session_id
        JsonNode guiEntry = null;
        JsonNode headlessEntry = null;
        for (final JsonNode entry : sessions)
        {
            final String sid = entry.path("session_id").asText();
            if ("gui-1".equals(sid))
            {
                guiEntry = entry;
            }
            else if ("mcp-1".equals(sid))
            {
                headlessEntry = entry;
            }
        }

        assertNotNull(guiEntry, "GUI session entry must be present in list_sessions response");
        assertNotNull(headlessEntry, "Headless session entry must be present in list_sessions response");

        // gui_state must reflect current window status — not a stale snapshot
        assertEquals("opened", guiEntry.path("gui_state").asText(),
                "GUI-owned session must report gui_state='opened' (window is currently open)");
        assertEquals("none", headlessEntry.path("gui_state").asText(),
                "Headless session must report gui_state='none' (no associated GUI window)");
    }
}
