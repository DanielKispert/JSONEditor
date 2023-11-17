package com.daniel.jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.tooltips;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;


public class TooltipHelper
{
    public static Tooltip makeTooltipFromJsonNode(JsonNode jsonNode)
    {
        StringBuilder tooltipText = new StringBuilder();
    
        if (jsonNode.isObject())
        {
            Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext())
            {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode childNode = field.getValue();
                Tooltip childTooltip;
                if (childNode.isContainerNode())
                {
                    childTooltip = new Tooltip(jsonNode.asText());
                }
                else
                {
                    childTooltip = makeTooltipFromJsonNode(field.getValue());
                }
                tooltipText.append(String.format("%s : %s\n", field.getKey(), childTooltip.getText()));
            }
        }
        else if (jsonNode.isArray())
        {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++)
            {
                Tooltip childTooltip = makeTooltipFromJsonNode(arrayNode.get(i));
                tooltipText.append(String.format("Item %d: %s\n", i + 1, childTooltip.getText()));
            }
        }
        else
        {
            tooltipText.append(jsonNode);
        }
    
        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setShowDuration(Duration.INDEFINITE);
        return tooltip;
    }
}
