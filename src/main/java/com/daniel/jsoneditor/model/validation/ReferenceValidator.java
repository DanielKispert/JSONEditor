package com.daniel.jsoneditor.model.validation;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;

import java.util.ArrayList;
import java.util.List;


public final class ReferenceValidator
{
    private ReferenceValidator()
    {
    }
    
    /**
     * Validates all references in the JSON to ensure they point to valid referenceable objects.
     *
     * @param model The model containing the JSON and schema
     * @return A validation result containing all invalid references
     */
    public static ValidationResult validateReferences(ReadableModel model)
    {
        final List<ValidationError> errors = new ArrayList<>();
        
        collectInvalidReferences(model, "", model.getNodeForPath(""), errors);
        
        return new ValidationResult(errors);
    }
    
    private static void collectInvalidReferences(ReadableModel model, String currentPath, JsonNodeWithPath node, List<ValidationError> errors)
    {
        if (node == null || node.getNode() == null)
        {
            return;
        }
        
        final ReferenceToObject referenceToObject = model.getReferenceToObject(currentPath);
        
        if (referenceToObject != null)
        {
            final String resolvedPath = ReferenceHelper.resolveReference(node, model);
            
            if (resolvedPath == null)
            {
                final String objectReferencingKey = node.getNode().at(referenceToObject.getObjectReferencingKey()).asText();
                final String objectKey = node.getNode().at(referenceToObject.getObjectKey()).asText();
                
                final String errorMessage = String.format(
                    "Invalid reference at '%s': Cannot resolve reference with type='%s' and key='%s'",
                    currentPath.isEmpty() ? "/" : currentPath,
                    objectReferencingKey,
                    objectKey
                );
                
                errors.add(new ValidationError(currentPath, errorMessage));
            }
        }
        
        if (node.getNode().isObject())
        {
            node.getNode().fields().forEachRemaining(entry -> {
                final String childPath = currentPath.isEmpty() ? "/" + entry.getKey() : currentPath + "/" + entry.getKey();
                final JsonNodeWithPath childNode = model.getNodeForPath(childPath);
                collectInvalidReferences(model, childPath, childNode, errors);
            });
        }
        else if (node.getNode().isArray())
        {
            for (int i = 0; i < node.getNode().size(); i++)
            {
                final String childPath = currentPath + "/" + i;
                final JsonNodeWithPath childNode = model.getNodeForPath(childPath);
                collectInvalidReferences(model, childPath, childNode, errors);
            }
        }
    }
}

