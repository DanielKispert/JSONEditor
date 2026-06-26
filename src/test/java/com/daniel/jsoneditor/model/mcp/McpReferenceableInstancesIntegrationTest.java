package com.daniel.jsoneditor.model.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class McpReferenceableInstancesIntegrationTest extends McpTestBase
{
    private static final String SCHEMA = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"type\":\"object\","
            + "\"properties\":{\"processes\":{\"type\":\"array\",\"items\":{\"type\":\"object\","
            + "\"properties\":{\"id\":{\"type\":\"string\"},\"sortOrder\":{\"type\":\"integer\",\"minimum\":0}},"
            + "\"required\":[\"id\"]}}},"
            + "\"required\":[\"processes\"],"
            + "\"referenceableObjects\":[{\"referencingKey\":\"item_ref\",\"path\":\"/processes\",\"key\":\"/id\"}]}";

    private static final String DATA = "{\"processes\":[{\"id\":\"proc_a\",\"sortOrder\":1},{\"id\":\"proc_b\",\"sortOrder\":2}]}";

    @Test
    void findReferenceableInstance_allPaths() throws Exception
    {
        final String fileId = openFile(DATA, SCHEMA);

        // verify find_referenceable_instance appears in the tools list
        final JsonNode toolsResponse = sendJsonRpc("tools/list");
        assertNull(toolsResponse.get("error"), "Expected no error from tools/list");
        final JsonNode tools = toolsResponse.path("result").path("tools");
        final List<String> toolNames = new ArrayList<>();
        for (final JsonNode tool : tools)
        {
            toolNames.add(tool.path("name").asText());
        }
        assertTrue(toolNames.contains("find_referenceable_instance"), "Expected find_referenceable_instance in tools list");

        // happy path: proc_a is at /processes/0
        final JsonNode resultA = callTool("find_referenceable_instance", OBJECT_MAPPER.createObjectNode()
                .put("session_id", fileId)
                .put("referencing_key", "item_ref")
                .put("instance_key", "proc_a"));
        assertNull(resultA.get("error"), "Expected no error for proc_a lookup");
        final JsonNode payloadA = parseToolResultPayload(resultA);
        assertEquals("/processes/0", payloadA.path("path").asText(), "Expected path /processes/0 for proc_a");
        assertEquals("proc_a", payloadA.path("key").asText(), "Expected key proc_a");
        assertFalse(payloadA.path("display_name").asText().isEmpty(), "Expected non-empty display_name for proc_a");

        // second instance: proc_b is at /processes/1
        final JsonNode resultB = callTool("find_referenceable_instance", OBJECT_MAPPER.createObjectNode()
                .put("session_id", fileId)
                .put("referencing_key", "item_ref")
                .put("instance_key", "proc_b"));
        assertNull(resultB.get("error"), "Expected no error for proc_b lookup");
        final JsonNode payloadB = parseToolResultPayload(resultB);
        assertEquals("/processes/1", payloadB.path("path").asText(), "Expected path /processes/1 for proc_b");

        // not found: nonexistent key returns JSON null payload
        final JsonNode resultMissing = callTool("find_referenceable_instance", OBJECT_MAPPER.createObjectNode()
                .put("session_id", fileId)
                .put("referencing_key", "item_ref")
                .put("instance_key", "nonexistent"));
        assertNull(resultMissing.get("error"), "Expected no RPC error for nonexistent instance key");
        final JsonNode payloadMissing = parseToolResultPayload(resultMissing);
        assertTrue(payloadMissing.isNull(), "Expected JSON null payload for nonexistent instance key");

        // unknown referencing_key → RPC error
        final JsonNode resultUnknown = callTool("find_referenceable_instance", OBJECT_MAPPER.createObjectNode()
                .put("session_id", fileId)
                .put("referencing_key", "unknown_key")
                .put("instance_key", "proc_a"));
        assertNotNull(resultUnknown.get("error"), "Expected RPC error for unknown referencing_key");
    }
}
