package com.daniel.jsoneditor.view.impl.jfx;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;


/**
 * View-layer utility for converting raw JSON Pointer paths to human-readable display path.
 * Single source of truth for the null-safe resolve: getNodeForPath -> isMissing-guard -> makeNameIncludingPath.
 */
public final class PathDisplayConverter
{
    private PathDisplayConverter()
    {
    }

    /**
     * Returns null when rawPath is null. Returns rawPath unchanged when the model is null or
     * the node cannot be resolved (null or missing). Otherwise returns the readable display
     * path via node.makeNameIncludingPath(model).
     */
    public static String convertToDisplay(final ReadableModel model, final String rawPath)
    {
        if (rawPath == null)
        {
            return null;
        }
        if (model == null)
        {
            return rawPath;
        }
        final JsonNodeWithPath node = model.getNodeForPath(rawPath);
        if (node == null || node.isMissing())
        {
            return rawPath;
        }
        return node.makeNameIncludingPath(model);
    }
}
