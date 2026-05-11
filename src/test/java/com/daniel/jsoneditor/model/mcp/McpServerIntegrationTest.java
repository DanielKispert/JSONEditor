package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


public class McpServerIntegrationTest
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    private JsonEditorMcpServer server;
    private HttpClient httpClient;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception
    {
        final int port;
        try (final ServerSocket socket = new ServerSocket(0))
        {
            port = socket.getLocalPort();
        }
        final FileSessionManager sessionManager = new FileSessionManager();
        server = new JsonEditorMcpServer(sessionManager, null);
        server.start(port);
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        baseUrl = "http://127.0.0.1:" + port;
    }

    @AfterEach
    void tearDown()
    {
        if (server != null)
        {
            server.stop();
        }
    }

    @Test
    void testHealthEndpoint() throws Exception
    {
        final HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/health"))
                        .GET()
                        .timeout(Duration.ofSeconds(5))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

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

    // ── helpers ──────────────────────────────────────────────────────────────

    private JsonNode sendJsonRpc(final String method) throws Exception
    {
        final ObjectNode request = OBJECT_MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestIdCounter.getAndIncrement());
        request.put("method", method);
        return sendRequest(request);
    }

    private JsonNode callTool(final String toolName, final ObjectNode arguments) throws Exception
    {
        final ObjectNode params = OBJECT_MAPPER.createObjectNode();
        params.put("name", toolName);
        params.set("arguments", arguments);

        final ObjectNode request = OBJECT_MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestIdCounter.getAndIncrement());
        request.put("method", "tools/call");
        request.set("params", params);
        return sendRequest(request);
    }

    private JsonNode sendRequest(final ObjectNode requestNode) throws Exception
    {
        final String body = OBJECT_MAPPER.writeValueAsString(requestNode);
        final HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        return OBJECT_MAPPER.readTree(response.body());
    }

    private JsonNode parseToolResultPayload(final JsonNode rpcResponse) throws Exception
    {
        final String text = rpcResponse.path("result").path("content").get(0).path("text").asText();
        return OBJECT_MAPPER.readTree(text);
    }

    private Path createTempJsonFile() throws Exception
    {
        final Path tempFile = Files.createTempFile("mcp-test-", ".json");
        Files.writeString(tempFile, "{\"name\":\"test\",\"value\":42}");
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    private Path createTempSchemaFile() throws Exception
    {
        final Path tempFile = Files.createTempFile("mcp-schema-", ".json");
        final String schema = "{"
                + "\"$schema\":\"http://json-schema.org/draft-07/schema#\","
                + "\"type\":\"object\","
                + "\"properties\":{"
                + "\"name\":{\"type\":\"string\"},"
                + "\"value\":{\"type\":\"number\"}"
                + "}"
                + "}";
        Files.writeString(tempFile, schema);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}
