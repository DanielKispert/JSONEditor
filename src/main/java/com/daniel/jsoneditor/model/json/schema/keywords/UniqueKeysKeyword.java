package com.daniel.jsoneditor.model.json.schema.keywords;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class UniqueKeysKeyword extends AbstractKeyword
{
    public UniqueKeysKeyword()
    {
        super("uniqueKeys");
    }
    
    @Override
    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception
    {
        // you can read validator config here
        String config = schemaNode.asText();
        return new AbstractJsonValidator()
        {
            @Override
            public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at)
            {
                // you can do validate here
            
                return Collections.emptySet();
            }
        };
    }
}
