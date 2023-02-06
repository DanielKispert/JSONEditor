package jsoneditor.model.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import jsoneditor.model.json.JsonNodeWithPath;

public class SchemaHelper
{
    
    public static JsonSchema getSubschemaNodeForPath(JsonSchema root, JsonNodeWithPath node)
    {
        // this will be an array like ["", "addresses" "1" "street"]
        String[] pathParts = node.getPath().split("/");
        // this is the root schema, we want the schema that validates only the node that is given by the path
        JsonNode subNode = root.getSchemaNode();
        for (String part : pathParts)
        {
            if (!part.isEmpty())
            {
                JsonNode typeNode = subNode.get("type");
                if (typeNode.isTextual())
                {
                    String type = typeNode.textValue();
                    if ("object".equalsIgnoreCase(type))
                    {
                        // go into the "properties" node and then get the object that's referenced by the key
                        subNode = subNode.get("properties").get(part);
                    }
                    else if ("array".equalsIgnoreCase(type))
                    {
                        subNode = subNode.get("items");
                    }
                }
            }
        }
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(subNode);
    }
    
    public static Integer getMaxItems(JsonSchema schema) {
        JsonNode node = schema.getSchemaNode();
        JsonNode maxItemsNode = node.get("maxItems");
        if (maxItemsNode != null && maxItemsNode.isInt())
        {
            return maxItemsNode.intValue();
        }
        else
        {
            return null;
        }
    }
}
