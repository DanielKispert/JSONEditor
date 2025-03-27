package com.daniel.jsoneditor.model.json.schema.reference;

import com.fasterxml.jackson.databind.JsonNode;


public interface Instanceable
{
    String getKeyOfInstance(JsonNode node);
    
    String getInstanceIdentifier(JsonNode node);
}
