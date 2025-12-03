package com.daniel.jsoneditor.model.validation;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.JsonNodeWithPath;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import com.fasterxml.jackson.databind.JsonNode;

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
            final JsonNode referencingKeyNode = node.getNode().at(referenceToObject.getObjectReferencingKey());
            final JsonNode objectKeyNode = node.getNode().at(referenceToObject.getObjectKey());
            
            if (referencingKeyNode.isMissingNode() || referencingKeyNode.isNull())
            {
                errors.add(new ValidationError(currentPath, String.format(
                    "Invalid reference at '%s': Missing required field '%s'",
                    formatPath(currentPath),
                    referenceToObject.getObjectReferencingKey()
                )));
                return;
            }
            
            if (objectKeyNode.isMissingNode() || objectKeyNode.isNull())
            {
                errors.add(new ValidationError(currentPath, String.format(
                    "Invalid reference at '%s': Missing required field '%s'",
                    formatPath(currentPath),
                    referenceToObject.getObjectKey()
                )));
                return;
            }
            
            final String objectReferencingKey = referencingKeyNode.asText();
            final String objectKey = objectKeyNode.asText();
            
            if (objectReferencingKey.isEmpty())
            {
                errors.add(new ValidationError(currentPath, String.format(
                    "Invalid reference at '%s': Empty value for '%s'",
                    formatPath(currentPath),
                    referenceToObject.getObjectReferencingKey()
                )));
                return;
            }
            
            if (objectKey.isEmpty())
            {
                errors.add(new ValidationError(currentPath, String.format(
                    "Invalid reference at '%s': Empty value for '%s'",
                    formatPath(currentPath),
                    referenceToObject.getObjectKey()
                )));
                return;
            }
            
            final String resolvedPath = ReferenceHelper.resolveReference(node, model);
            
            if (resolvedPath == null)
            {
                final ReferenceableObject refObject = ReferenceHelper.getReferenceableObject(model, objectReferencingKey);
                
                if (refObject == null)
                {
                    errors.add(new ValidationError(currentPath, String.format(
                        "Invalid reference at '%s': Unknown reference type '%s' (expected one of the defined referenceableObjects)",
                        formatPath(currentPath),
                        objectReferencingKey
                    )));
                }
                else
                {
                    errors.add(new ValidationError(currentPath, String.format(
                        "Invalid reference at '%s': Cannot find %s with key '%s' in '%s'",
                        formatPath(currentPath),
                        objectReferencingKey,
                        objectKey,
                        refObject.getPath()
                    )));
                }
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
    
    private static String formatPath(String path)
    {
        return path.isEmpty() ? "/" : path;
    }
}

