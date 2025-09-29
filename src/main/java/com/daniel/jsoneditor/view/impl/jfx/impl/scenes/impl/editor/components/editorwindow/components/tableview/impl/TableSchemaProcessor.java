package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.editorwindow.components.tableview.impl;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.NodeSearcher;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Responsible for processing JSON schemas and creating table data structures.
 * Extracted from EditorTableViewImpl to follow Single Responsibility Principle.
 */
public class TableSchemaProcessor
{
    private final ReadableModel model;
    
    public TableSchemaProcessor(ReadableModel model)
    {
        this.model = model;
    }
    
    /**
     * Processes a JSON node and schema to create displayable table data.
     *
     * @param nodeWithPath the JSON node to process
     * @return processed table data
     */
    public TableData processNode(JsonNodeWithPath nodeWithPath)
    {
        final JsonNode node = nodeWithPath.getNode();
        final JsonNode schema = model.getSubschemaForPath(nodeWithPath.getPath()).getSchemaNode();
        
        final ObservableList<JsonNodeWithPath> nodesToDisplay = createNodesList(nodeWithPath, node);
        final List<Pair<Pair<String, Boolean>, JsonNode>> properties = extractProperties(schema);
        
        return new TableData(nodesToDisplay, properties, isArray(schema));
    }
    
    private ObservableList<JsonNodeWithPath> createNodesList(JsonNodeWithPath nodeWithPath, JsonNode node)
    {
        final ObservableList<JsonNodeWithPath> nodesToDisplay = FXCollections.observableArrayList();
        
        if (nodeWithPath.isArray())
        {
            int arrayItemIndex = 0;
            for (JsonNode arrayItem : node)
            {
                nodesToDisplay.add(new JsonNodeWithPath(arrayItem, nodeWithPath.getPath() + "/" + arrayItemIndex++));
            }
        }
        else if (nodeWithPath.isObject())
        {
            nodesToDisplay.add(nodeWithPath);
        }
        
        return nodesToDisplay;
    }
    
    private List<Pair<Pair<String, Boolean>, JsonNode>> extractProperties(JsonNode schema)
    {
        final List<Pair<Pair<String, Boolean>, JsonNode>> properties = new ArrayList<>();
        final boolean isArray = isArray(schema);
        
        JsonNode childSchema = schema;
        if (isArray)
        {
            childSchema = childSchema.get("items");
        }
        
        final List<String> requiredProperties = SchemaHelper.getRequiredProperties(childSchema);
        final Iterator<Map.Entry<String, JsonNode>> iterator = childSchema.get("properties").fields();
        
        while (iterator.hasNext())
        {
            final Map.Entry<String, JsonNode> entry = iterator.next();
            final String propertyName = entry.getKey();
            final boolean isRequired = requiredProperties.contains(propertyName);
            
            if (isArray)
            {
                // Only permit non-object and non-array properties in array view
                final String propertyType = NodeSearcher.getTypeFromNode(entry.getValue());
                if (propertyType != null && !"object".equals(propertyType) && !"array".equals(propertyType))
                {
                    properties.add(new Pair<>(new Pair<>(propertyName, isRequired), entry.getValue()));
                }
            }
            else
            {
                properties.add(new Pair<>(new Pair<>(propertyName, isRequired), entry.getValue()));
            }
        }
        
        return properties;
    }
    
    private boolean isArray(JsonNode schema)
    {
        final JsonNode type = schema.get("type");
        return type != null && "array".equals(type.asText());
    }
    
    /**
     * Data transfer object for processed table data.
     */
    public static class TableData
    {
        private final ObservableList<JsonNodeWithPath> nodes;
        private final List<Pair<Pair<String, Boolean>, JsonNode>> properties;
        private final boolean isArray;
        
        public TableData(ObservableList<JsonNodeWithPath> nodes,
                        List<Pair<Pair<String, Boolean>, JsonNode>> properties,
                        boolean isArray)
        {
            this.nodes = nodes;
            this.properties = properties;
            this.isArray = isArray;
        }
        
        public ObservableList<JsonNodeWithPath> getNodes() { return nodes; }
        public List<Pair<Pair<String, Boolean>, JsonNode>> getProperties() { return properties; }
        public boolean isArray() { return isArray; }
    }
}
