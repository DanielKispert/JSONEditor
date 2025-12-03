package com.daniel.jsoneditor.model.validation;

import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceHelper;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObject;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceToObjectInstance;
import com.daniel.jsoneditor.model.json.schema.reference.ReferenceableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public final class ReferenceValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ReferenceValidator.class);
    
    private ReferenceValidator()
    {
    }
    
    /**
     * Validates all references in the JSON to ensure they point to valid referenceable objects.
     * Uses data already extracted by ReferenceToObjectInstance for optimal performance.
     *
     * @param model The model containing the JSON and schema
     * @return A validation result containing all invalid references
     */
    public static ValidationResult validateReferences(ReadableModel model)
    {
        final List<ValidationError> errors = new ArrayList<>();
        final List<ReferenceToObject> referenceDefinitions = ReferenceHelper.getReferenceToObjectNodes(model);
        
        int totalInstances = 0;
        
        for (ReferenceToObject referenceDefinition : referenceDefinitions)
        {
            final List<ReferenceToObjectInstance> instances = ReferenceHelper.getInstancesOfReferenceToObject(model, referenceDefinition);
            totalInstances += instances.size();
            
            for (ReferenceToObjectInstance instance : instances)
            {
                validateReferenceInstance(model, instance, errors);
            }
        }
        
        logger.debug("Validated {} reference instances, found {} errors", totalInstances, errors.size());
        
        return new ValidationResult(errors);
    }
    
    private static void validateReferenceInstance(ReadableModel model, ReferenceToObjectInstance instance, List<ValidationError> errors)
    {
        final String path = instance.getPath();
        final String objectKey = instance.getKey();
        
        if (objectKey == null || objectKey.isEmpty())
        {
            errors.add(new ValidationError(path, String.format(
                "Invalid reference at '%s': Empty or missing reference key",
                path
            )));
            return;
        }
        
        final String resolvedPath = ReferenceHelper.resolveReference(model, instance);
        
        if (resolvedPath == null)
        {
            final ReferenceableObject refObject = ReferenceHelper.getReferenceableObject(model,
                instance.getReference().getObjectReferencingKey());
            
            if (refObject == null)
            {
                return;
            }
            
            errors.add(new ValidationError(path, String.format(
                "Invalid reference at '%s': Cannot find %s with key '%s' in '%s'",
                path,
                refObject.getReferencingKey(),
                objectKey,
                refObject.getPath()
            )));
        }
    }
}

