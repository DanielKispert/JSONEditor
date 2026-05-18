package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class McpServerIntegrationTest extends McpTestBase
{
    @Test
    void testHealthEndpoint() throws Exception
    {
        final java.net.http.HttpResponse<String> response = httpClient.send(
                java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(baseUrl + "/health"))
                        .GET()
                        .timeout(java.time.Duration.ofSeconds(5))
                        .build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        final JsonNode body = OBJECT_MAPPER.readTree(response.body());
        assertEquals("ok", body.path("status").asText());
    }

    @Test
    void testInitialize() throws Exception
    {
        final JsonNode response = sendJsonRpc("initialize");
        assertNull(response.get("error"), "Expected no error in initialize response");
        final JsonNode result = response.path("result");
        assertEquals("json-editor", result.path("serverInfo").path("name").asText());
        assertEquals("2024-11-05", result.path("protocolVersion").asText());
    }

    @Test
    void testToolsList() throws Exception
    {
        final JsonNode response = sendJsonRpc("tools/list");
        assertNull(response.get("error"), "Expected no error in tools/list response");
        final JsonNode tools = response.path("result").path("tools");
        assertTrue(tools.isArray());
        assertTrue(tools.size() > 0);

        final List<String> toolNames = new ArrayList<>();
        for (final JsonNode tool : tools)
        {
            toolNames.add(tool.path("name").asText());
        }
        assertTrue(toolNames.contains("open_file"), "Expected open_file tool");
        assertTrue(toolNames.contains("list_files"), "Expected list_files tool");
        assertTrue(toolNames.contains("close_file"), "Expected close_file tool");
        assertTrue(toolNames.contains("get_file_info"), "Expected get_file_info tool");
        assertTrue(toolNames.contains("get_node"), "Expected get_node tool");
    }

    @Test
    void testOpenAndListFiles() throws Exception
    {
        final Path jsonFile = createTempJsonFile();
        final Path schemaFile = createTempSchemaFile();

        final JsonNode openResult = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", jsonFile.toString())
                .put("schema_path", schemaFile.toString()));
        assertNull(openResult.get("error"), "Expected no error from open_file");

        final JsonNode openPayload = parseToolResultPayload(openResult);
        final String fileId = openPayload.path("file_id").asText();
        assertFalse(fileId.isEmpty(), "Expected non-empty file_id");

        final JsonNode listResult = callTool("list_files", OBJECT_MAPPER.createObjectNode());
        assertNull(listResult.get("error"), "Expected no error from list_files");

        final JsonNode listPayload = parseToolResultPayload(listResult);
        assertTrue(listPayload.isArray(), "Expected array result from list_files");

        boolean found = false;
        for (final JsonNode entry : listPayload)
        {
            if (fileId.equals(entry.path("file_id").asText()))
            {
                found = true;
                break;
            }
        }
        assertTrue(found, "Expected opened file to appear in list_files result");
    }

    @Test
    void testGetNodeOnOpenedFile() throws Exception
    {
        final Path jsonFile = createTempJsonFile();
        final Path schemaFile = createTempSchemaFile();

        final JsonNode openResult = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", jsonFile.toString())
                .put("schema_path", schemaFile.toString()));
        final String fileId = parseToolResultPayload(openResult).path("file_id").asText();

        final JsonNode nodeResult = callTool("get_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", ""));
        assertNull(nodeResult.get("error"), "Expected no error from get_node");

        final JsonNode nodePayload = parseToolResultPayload(nodeResult);
        assertEquals("", nodePayload.path("path").asText());
        assertEquals("Root Element", nodePayload.path("display_name").asText());
        assertTrue(nodePayload.path("value").isObject(), "Expected root to be an object");
        assertEquals("test", nodePayload.path("value").path("name").asText());
        assertEquals(42, nodePayload.path("value").path("value").asInt());
    }

    @Test
    void testGetFileInfo() throws Exception
    {
        final Path jsonFile = createTempJsonFile();
        final Path schemaFile = createTempSchemaFile();

        final JsonNode openResult = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", jsonFile.toString())
                .put("schema_path", schemaFile.toString()));
        final String fileId = parseToolResultPayload(openResult).path("file_id").asText();

        final JsonNode infoResult = callTool("get_file_info", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId));
        assertNull(infoResult.get("error"), "Expected no error from get_file_info");

        final JsonNode infoPayload = parseToolResultPayload(infoResult);
        assertEquals(jsonFile.getFileName().toString(), infoPayload.path("file_name").asText());
        assertEquals(schemaFile.toString(), infoPayload.path("schema_path").asText());
        assertTrue(infoPayload.path("has_content").asBoolean(), "Expected has_content to be true");
    }

    @Test
    void testCloseFile() throws Exception
    {
        final Path jsonFile = createTempJsonFile();
        final Path schemaFile = createTempSchemaFile();

        final JsonNode openResult = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", jsonFile.toString())
                .put("schema_path", schemaFile.toString()));
        final String fileId = parseToolResultPayload(openResult).path("file_id").asText();

        final JsonNode closeResult = callTool("close_file", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId));
        assertNull(closeResult.get("error"), "Expected no error from close_file");

        final JsonNode closePayload = parseToolResultPayload(closeResult);
        assertTrue(closePayload.path("success").asBoolean(), "Expected success=true from close_file");

        final JsonNode listResult = callTool("list_files", OBJECT_MAPPER.createObjectNode());
        final JsonNode listPayload = parseToolResultPayload(listResult);
        for (final JsonNode entry : listPayload)
        {
            assertNotEquals(fileId, entry.path("file_id").asText(), "Closed file should not appear in list_files");
        }
    }

    @Test
    void testCloseNonExistentFile() throws Exception
    {
        final JsonNode result = callTool("close_file", OBJECT_MAPPER.createObjectNode()
                .put("file_id", "nonexistent-id-12345"));
        assertNotNull(result.get("error"), "Expected error when closing non-existent file_id");
    }

    @Test
    void testOpenInvalidFile() throws Exception
    {
        final JsonNode result = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", "/nonexistent/path/file.json")
                .put("schema_path", "/nonexistent/path/schema.json"));
        assertNotNull(result.get("error"), "Expected error when opening non-existent files");
    }

    @Test
    void testGetNodeWithEmptyFileIdArgument() throws Exception
    {
        final JsonNode result = callTool("get_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", "")
                .put("path", ""));
        assertNotNull(result.get("error"), "Expected error when file_id is empty string");
        final String message = result.path("error").path("message").asText();
        assertTrue(message.contains("file_id argument is required"),
                "Expected 'file_id argument is required' for empty file_id, got: " + message);
    }

    @Test
    void testGetNodeWithNonExistentFileId() throws Exception
    {
        final JsonNode result = callTool("get_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", "nonexistent123")
                .put("path", ""));
        assertNotNull(result.get("error"), "Expected error when file_id is unknown");
        final String message = result.path("error").path("message").asText();
        assertTrue(message.contains("nonexistent123"),
                "Expected file_id to be included in error message, got: " + message);
    }

    @Test
    void testValidateNode() throws Exception
    {
        final String schema = "{"
                + "\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{"
                + "\"name\":{\"type\":\"string\"},"
                + "\"value\":{\"type\":\"number\"}"
                + "}"
                + "}";
        final String fileId = openFile("{\"name\":\"test\",\"value\":42}", schema);

        // valid content → valid=true, no errors
        final JsonNode validResult = callTool("validate_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", "")
                .set("content", OBJECT_MAPPER.readTree("{\"name\":\"hello\",\"value\":10}")));
        assertNull(validResult.get("error"), "Expected no error from validate_node with valid content");
        final JsonNode validPayload = parseToolResultPayload(validResult);
        assertTrue(validPayload.path("valid").asBoolean(), "Expected valid=true for matching content");
        assertTrue(validPayload.path("errors").isEmpty(), "Expected empty errors for valid content");

        // invalid content (wrong types) → valid=false, errors non-empty mentioning bad fields
        final JsonNode invalidResult = callTool("validate_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", "")
                .set("content", OBJECT_MAPPER.readTree("{\"name\":123,\"value\":\"wrong\"}")));
        assertNull(invalidResult.get("error"), "Expected no RPC error from validate_node with invalid content");
        final JsonNode invalidPayload = parseToolResultPayload(invalidResult);
        assertFalse(invalidPayload.path("valid").asBoolean(), "Expected valid=false for mismatched content");
        assertFalse(invalidPayload.path("errors").isEmpty(), "Expected non-empty errors for invalid content");
        final String errorsText = invalidPayload.path("errors").toString();
        assertTrue(errorsText.contains("name") || errorsText.contains("value"),
                "Expected errors to mention 'name' or 'value', got: " + errorsText);
    }

    @Test
    void testValidateNodeErrors() throws Exception
    {
        final String schema = "{"
                + "\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{"
                + "\"name\":{\"type\":\"string\"},"
                + "\"value\":{\"type\":\"number\"}"
                + "}"
                + "}";
        final String fileId = openFile("{\"name\":\"test\",\"value\":42}", schema);

        // missing content argument → error mentioning "content"
        final JsonNode missingContent = callTool("validate_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", ""));
        assertNotNull(missingContent.get("error"), "Expected error when content argument is missing");
        final String missingContentMessage = missingContent.path("error").path("message").asText();
        assertTrue(missingContentMessage.contains("content"), "Expected 'content' in error message, got: " + missingContentMessage);

        // path with no schema → error response
        final JsonNode noSchema = callTool("validate_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", "/unknown_field_xyz")
                .set("content", OBJECT_MAPPER.readTree("\"anything\"")));
        assertNotNull(noSchema.get("error"), "Expected error when path has no schema");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Path createTempJsonFile() throws Exception
    {
        return createTempFile("mcp-test-", ".json", "{\"name\":\"test\",\"value\":42}");
    }

    private Path createTempSchemaFile() throws Exception
    {
        final String schema = "{"
                + "\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{"
                + "\"name\":{\"type\":\"string\"},"
                + "\"value\":{\"type\":\"number\"}"
                + "}"
                + "}";
        return createTempFile("mcp-schema-", ".json", schema);
    }
}
