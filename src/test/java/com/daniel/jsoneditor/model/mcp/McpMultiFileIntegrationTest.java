package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class McpMultiFileIntegrationTest extends McpTestBase
{
    private static final String JSON_COMPANY =
            "{\"company\":\"Acme\",\"employees\":[{\"name\":\"Alice\",\"role\":\"dev\"},"
            + "{\"name\":\"Bob\",\"role\":\"qa\"}],\"config\":{\"version\":2,\"darkMode\":true}}";

    private static final String JSON_ITEMS =
            "{\"items\":[{\"id\":1,\"label\":\"first\"},{\"id\":2,\"label\":\"second\"}]}";

    private static final String JSON_FLAGS =
            "{\"active\":true,\"count\":7}";

    private static final String SCHEMA_COMPANY =
            "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\","
            + "\"properties\":{"
            + "\"company\":{\"type\":\"string\"},"
            + "\"employees\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"name\":{\"type\":\"string\"},\"role\":{\"type\":\"string\"}}}},"
            + "\"config\":{\"type\":\"object\",\"properties\":{"
            + "\"version\":{\"type\":\"integer\"},\"darkMode\":{\"type\":\"boolean\"}}}"
            + "}}";

    private static final String SCHEMA_ITEMS =
            "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\","
            + "\"properties\":{"
            + "\"items\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"id\":{\"type\":\"integer\"},\"label\":{\"type\":\"string\"}}}}"
            + "}}";

    private static final String SCHEMA_FLAGS =
            "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\","
            + "\"properties\":{"
            + "\"active\":{\"type\":\"boolean\"},"
            + "\"count\":{\"type\":\"integer\"}"
            + "}}";

    @Test
    void testMultipleFilesOpenSimultaneously() throws Exception
    {
        final String id0 = openFile(JSON_COMPANY, SCHEMA_COMPANY);
        final String id1 = openFile(JSON_ITEMS, SCHEMA_ITEMS);
        final String id2 = openFile(JSON_FLAGS, SCHEMA_FLAGS);

        // All IDs must be unique
        assertNotEquals(id0, id1, "File IDs must be unique");
        assertNotEquals(id1, id2, "File IDs must be unique");
        assertNotEquals(id0, id2, "File IDs must be unique");

        // list_files must show all 3
        final JsonNode listPayload = parseToolResultPayload(
                callTool("list_files", OBJECT_MAPPER.createObjectNode()));
        assertTrue(listPayload.isArray(), "list_files must return an array");
        final Set<String> listedIds = new HashSet<>();
        for (final JsonNode entry : listPayload)
        {
            listedIds.add(entry.path("file_id").asText());
        }
        assertTrue(listedIds.contains(id0), "list_files must include file 0");
        assertTrue(listedIds.contains(id1), "list_files must include file 1");
        assertTrue(listedIds.contains(id2), "list_files must include file 2");

        // get_node returns correct content per file
        final JsonNode root0 = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", id0).put("path", "")));
        assertEquals("Acme", root0.path("value").path("company").asText(),
                "File 0 must contain company=Acme");

        final JsonNode root1 = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", id1).put("path", "")));
        assertTrue(root1.path("value").path("items").isArray(),
                "File 1 must contain an items array");

        final JsonNode root2 = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", id2).put("path", "")));
        assertEquals(7, root2.path("value").path("count").asInt(),
                "File 2 must contain count=7");
    }

    @Test
    void testCloseOneOfMultipleFiles() throws Exception
    {
        final String id0 = openFile(JSON_COMPANY, SCHEMA_COMPANY);
        final String id1 = openFile(JSON_ITEMS, SCHEMA_ITEMS);
        final String id2 = openFile(JSON_FLAGS, SCHEMA_FLAGS);

        // Close the middle file
        final JsonNode closePayload = parseToolResultPayload(
                callTool("close_file", OBJECT_MAPPER.createObjectNode().put("file_id", id1)));
        assertTrue(closePayload.path("success").asBoolean(), "Expected success=true from close_file");

        // list_files must now show exactly id0 and id2
        final JsonNode listPayload = parseToolResultPayload(
                callTool("list_files", OBJECT_MAPPER.createObjectNode()));
        final Set<String> remaining = new HashSet<>();
        for (final JsonNode entry : listPayload)
        {
            remaining.add(entry.path("file_id").asText());
        }
        assertTrue(remaining.contains(id0), "id0 must still be listed after closing id1");
        assertFalse(remaining.contains(id1), "id1 must be gone after close");
        assertTrue(remaining.contains(id2), "id2 must still be listed after closing id1");

        // Remaining files must still respond correctly
        final JsonNode node0 = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", id0).put("path", "")));
        assertEquals("Acme", node0.path("value").path("company").asText(),
                "id0 must still return correct content after id1 was closed");

        final JsonNode node2 = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", id2).put("path", "")));
        assertEquals(7, node2.path("value").path("count").asInt(),
                "id2 must still return correct content after id1 was closed");
    }

    @Test
    void testSessionsAreIsolated() throws Exception
    {
        final String idCompany = openFile(JSON_COMPANY, SCHEMA_COMPANY);
        final String idItems = openFile(JSON_ITEMS, SCHEMA_ITEMS);

        final JsonNode companyRoot = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", idCompany).put("path", "")));
        final JsonNode itemsRoot = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", idItems).put("path", "")));

        // No cross-contamination: company session has "company" key, items session does not
        assertTrue(companyRoot.path("value").has("company"),
                "company session must have 'company' key");
        assertFalse(itemsRoot.path("value").has("company"),
                "items session must NOT have 'company' key");

        // Items session has "items" key, company session does not
        assertTrue(itemsRoot.path("value").has("items"),
                "items session must have 'items' key");
        assertFalse(companyRoot.path("value").has("items"),
                "company session must NOT have 'items' key");

        assertEquals("Acme", companyRoot.path("value").path("company").asText());
        assertEquals(2, itemsRoot.path("value").path("items").size());

        // validate_node: same content is valid for company schema but invalid for items schema — proves schema isolation
        final JsonNode mixedJson = OBJECT_MAPPER.readTree("{\"items\":\"not-an-array\"}");

        final ObjectNode companyValidateParams = OBJECT_MAPPER.createObjectNode()
                .put("file_id", idCompany).put("path", "");
        companyValidateParams.set("content", mixedJson);
        assertTrue(parseToolResultPayload(callTool("validate_node", companyValidateParams)).path("valid").asBoolean(),
                "Company schema must accept {items:string} — it has no 'items' constraint");

        final ObjectNode itemsValidateParams = OBJECT_MAPPER.createObjectNode()
                .put("file_id", idItems).put("path", "");
        itemsValidateParams.set("content", mixedJson);
        assertFalse(parseToolResultPayload(callTool("validate_node", itemsValidateParams)).path("valid").asBoolean(),
                "Items schema must reject {items:string} because 'items' must be an array");
    }

    @Test
    void testComplexNestedJson() throws Exception
    {
        final String fileId = openFile(JSON_COMPANY, SCHEMA_COMPANY);

        // Query employees sub-path
        final JsonNode employeesNode = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", fileId).put("path", "/employees")));
        assertTrue(employeesNode.path("value").isArray(), "Expected employees to be an array");
        assertEquals(2, employeesNode.path("value").size(), "Expected 2 employees");

        // Query config sub-path
        final JsonNode configNode = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", fileId).put("path", "/config")));
        assertTrue(configNode.path("value").isObject(), "Expected config to be an object");
        assertEquals(2, configNode.path("value").path("version").asInt(), "Expected version=2");
        assertTrue(configNode.path("value").path("darkMode").asBoolean(), "Expected darkMode=true");

        // Query first employee by index
        final JsonNode firstEmployee = parseToolResultPayload(
                callTool("get_node", OBJECT_MAPPER.createObjectNode().put("file_id", fileId).put("path", "/employees/0")));
        assertEquals("Alice", firstEmployee.path("value").path("name").asText());
        assertEquals("dev", firstEmployee.path("value").path("role").asText());
    }

    @Test
    void testGetNodeInvalidPath() throws Exception
    {
        final String fileId = openFile(JSON_COMPANY, SCHEMA_COMPANY);

        final JsonNode result = callTool("get_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", "/nonexistent/deeply/nested"));

        // The server handles non-existent paths gracefully.
        // It either returns a JSON-RPC error, or a success response where the
        // value field is absent (Jackson excludes MissingNode from serialization).
        if (result.get("error") == null)
        {
            final JsonNode payload = parseToolResultPayload(result);
            // value field is either absent or null for a non-existent path
            assertTrue(!payload.has("value") || payload.path("value").isNull(),
                    "Expected absent or null value for non-existent path, got: " + payload);
        }
    }

    @Test
    void testToolCallWithInvalidFileId() throws Exception
    {
        final JsonNode result = callTool("get_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", "bogus-file-id-does-not-exist")
                .put("path", ""));

        assertNotNull(result.get("error"),
                "Expected JSON-RPC error when using a bogus file_id");
    }

    @Test
    void testOpenFileValidationRejection() throws Exception
    {
        final java.nio.file.Path jsonFile = createTempFile("mcp-invalid-", ".json",
                "{\"active\":\"not-a-boolean\",\"count\":\"not-a-number\"}");
        final java.nio.file.Path schemaFile = createTempFile("mcp-invalid-schema-", ".json", SCHEMA_FLAGS);

        final JsonNode result = callTool("open_file", OBJECT_MAPPER.createObjectNode()
                .put("json_path", jsonFile.toString())
                .put("schema_path", schemaFile.toString()));

        assertNotNull(result.get("error"), "Expected JSON-RPC error when JSON fails schema validation");
        final String errorMessage = result.path("error").path("message").asText("");
        assertFalse(errorMessage.isBlank(), "Error message must not be blank");
        assertTrue(
                errorMessage.toLowerCase().contains("schema") || errorMessage.toLowerCase().contains("validat"),
                "Error message must mention schema or validation failure, got: " + errorMessage);
    }

    @Test
    void testCloseWhileReading() throws Exception
    {
        final String fileId = openFile(JSON_FLAGS, SCHEMA_FLAGS);

        // Close the session
        final JsonNode closeResult = parseToolResultPayload(
                callTool("close_file", OBJECT_MAPPER.createObjectNode().put("file_id", fileId)));
        assertTrue(closeResult.path("success").asBoolean(), "close_file must succeed");

        // Now try to read from the closed session - must return an error, not crash
        final JsonNode getNodeResult = callTool("get_node", OBJECT_MAPPER.createObjectNode()
                .put("file_id", fileId)
                .put("path", ""));
        assertNotNull(getNodeResult.get("error"),
                "Expected JSON-RPC error when accessing a closed session");
    }
}
