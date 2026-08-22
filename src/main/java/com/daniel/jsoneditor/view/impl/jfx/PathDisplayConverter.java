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
     * Converts a raw JSON Pointer path to a readable display path using node name resolution.
     * Falls back to rawPath when rawPath is null or the node is missing.
     *
     * @param model   the model used to look up node display names
     * @param rawPath the JSON Pointer path to convert
     * @return a human-readable path, or rawPath if resolution is not possible
     */
    public static String convertToDisplay(final ReadableModel model, final String rawPath)
    {
        if (rawPath == null)
        {
            return null;
        }
        final JsonNodeWithPath node = model.getNodeForPath(rawPath);
        if (node == null || node.isMissing())
        {
            return rawPath;
        }
        return node.makeNameIncludingPath(model);
    }
}
