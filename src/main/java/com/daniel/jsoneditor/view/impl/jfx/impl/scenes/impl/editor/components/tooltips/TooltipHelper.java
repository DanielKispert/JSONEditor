package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.daniel.jsoneditor.model.ReadableModel;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;


public class TooltipHelper
{
    
    public static Tooltip makeTooltipFromPath(ReadableModel model, String path)
    {
        JsonNode jsonNode = model.getNodeForPath(path).getNode();
        return jsonNode == null ? null : makeTooltipFromJsonNode(jsonNode);
    }
    
    public static Tooltip makeTooltipFromJsonNode(JsonNode jsonNode)
    {
        String tooltipText = getTooltipTextFromJsonNode(jsonNode);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDuration(Duration.INDEFINITE);
        return tooltip;
    }
    
    private static boolean objectHasOnlyValueNodes(JsonNode jsonNode)
    {
        Iterable<Map.Entry<String, JsonNode>> fields = jsonNode::fields;
        return StreamSupport.stream(fields.spliterator(), false).noneMatch(field -> field.getValue().isContainerNode());
    }
    
    private static String getTooltipTextFromJsonNode(JsonNode jsonNode)
    {
        if (jsonNode.isValueNode())
        {
            return jsonNode.asText();
        }
        else if (jsonNode.isObject() && objectHasOnlyValueNodes(jsonNode))
        {
            return getTooltipFromObjectNodeWithOnlyValueNodes(jsonNode);
        }
        else if (jsonNode.isObject())
        {
            return getTooltipFromObjectNode(jsonNode);
        }
        else if (jsonNode.isArray())
        {
            return getTooltipFromArrayNode(jsonNode);
        }
        return "";
    }
    
    private static String getTooltipFromObjectNodeWithOnlyValueNodes(JsonNode jsonNode)
    {
        Iterable<Map.Entry<String, JsonNode>> fields = jsonNode::fields;
        return StreamSupport.stream(fields.spliterator(), false).map(field -> field.getValue().asText()).collect(Collectors.joining(" | "));
    }
    
    private static String getTooltipFromObjectNode(JsonNode jsonNode)
    {
        StringBuilder sb = new StringBuilder();
        jsonNode.fields().forEachRemaining(field -> sb.append("---").append(field.getKey()).append("---\n")
                .append(getTooltipTextFromJsonNode(field.getValue())).append("\n\n"));
        return sb.toString().trim();
    }
    
    private static String getTooltipFromArrayNode(JsonNode jsonNode)
    {
        StringBuilder sb = new StringBuilder();
        jsonNode.iterator().forEachRemaining(arrayItem -> sb.append(getTooltipTextFromJsonNode(arrayItem)).append("\n"));
        return sb.toString().trim();
    }
    
}
