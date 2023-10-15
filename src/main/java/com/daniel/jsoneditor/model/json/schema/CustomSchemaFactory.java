package com.daniel.jsoneditor.model.json.schema;

import com.daniel.jsoneditor.model.json.schema.keywords.UniqueKeysKeyword;
import com.networknt.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CustomSchemaFactory
{
    public static JsonSchemaFactory makeCustomFactory()
    {
    
        List<Format> BUILTIN_FORMATS = new ArrayList<>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);
        JsonMetaSchema.Builder metaSchemaBuilder = new JsonMetaSchema.Builder(new Version202012().getInstance().getUri());
        
           JsonMetaSchema metaSchema = metaSchemaBuilder.idKeyword("$id")
                     .addFormats(BUILTIN_FORMATS)
                     .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V202012))
                     // keywords that may validly exist, but have no validation aspect to them
                     .addKeywords(Arrays.asList(
                             new NonValidationKeyword("$schema"),
                             new NonValidationKeyword("$id"),
                             new NonValidationKeyword("title"),
                             new NonValidationKeyword("description"),
                             new NonValidationKeyword("default"),
                             new NonValidationKeyword("definitions"),
                             new NonValidationKeyword("$defs"),
                             // custom keywords
                             new UniqueKeysKeyword()
                     ))
                     .build();
        JsonSchemaFactory.Builder schemaFactoryBuilder = JsonSchemaFactory.builder();
        return schemaFactoryBuilder.defaultMetaSchemaURI(metaSchema.getUri())
                                            .addMetaSchema(metaSchema).build();
    }
}
