package com.daniel.jsoneditor.model.mcp;

import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

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

/**
 * Base class for MCP server integration tests.
 * Starts a fresh server on a random port before each test and stops it after.
 */
abstract class McpTestBase
{
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final AtomicInteger requestIdCounter = new AtomicInteger(1);

    private final List<Path> tempFiles = new ArrayList<>();

    protected JsonEditorMcpServer server;
    protected HttpClient httpClient;
    protected String baseUrl;

    @BeforeEach
    void setUp() throws Exception
    {
        final FileSessionManager sessionManager = new FileSessionManager();
        server = new JsonEditorMcpServer(sessionManager, null);
        server.start(0);
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        baseUrl = "http://127.0.0.1:" + server.getPort();
    }

    @AfterEach
    void tearDown() throws Exception
    {
        if (server != null)
        {
            server.stop();
        }
        if (httpClient != null)
        {
            httpClient.close();
        }
        for (final Path tempFile : tempFiles)
        {
            Files.deleteIfExists(tempFile);
        }
        tempFiles.clear();
    }

    protected JsonNode sendJsonRpc(final String method) throws Exception
    {
        final ObjectNode request = OBJECT_MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", requestIdCounter.getAndIncrement());
        request.put("method", method);
        return sendRequest(request);
    }

    protected JsonNode callTool(final String toolName, final ObjectNode arguments) throws Exception
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

    protected JsonNode sendRequest(final ObjectNode requestNode) throws Exception
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

    protected JsonNode parseToolResultPayload(final JsonNode rpcResponse) throws Exception
    {
        final String text = rpcResponse.path("result").path("content").get(0).path("text").asText();
        return OBJECT_MAPPER.readTree(text);
    }

    protected Path createTempFile(final String prefix, final String suffix, final String content) throws Exception
    {
        final Path tempFile = Files.createTempFile(prefix, suffix);
        Files.writeString(tempFile, content);
        tempFiles.add(tempFile);
        return tempFile;
    }

    /**
     * Opens a file via MCP open_file tool and returns the assigned file_id.
     * Asserts that no error occurs and that the returned file_id is non-empty.
     */
    protected String openFile(final String jsonContent, final String schemaContent) throws Exception
    {
        final Path jsonFile = createTempFile("mcp-test-", ".json", jsonContent);
        final Path schemaFile = createTempFile("mcp-schema-", ".json", schemaContent);
        final JsonNode openResult = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", jsonFile.toString())
                .put("schema_path", schemaFile.toString()));
        assertNull(openResult.get("error"), "Expected no error from open_file");
        final String fileId = parseToolResultPayload(openResult).path("file_id").asText();
        assertFalse(fileId.isEmpty(), "Expected non-empty file_id");
        return fileId;
    }

}
