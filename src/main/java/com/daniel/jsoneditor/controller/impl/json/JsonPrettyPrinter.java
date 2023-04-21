package com.daniel.jsoneditor.controller.impl.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;


/**
 * this class tries to be as close to the default vs code formatting for JSON
 */
public class JsonPrettyPrinter implements PrettyPrinter
{
    private static final String INDENTATION = "  "; // Two spaces per level of indentation
    
    private int indentLevel = 0;
    
    @Override
    public void writeRootValueSeparator(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw('\n');
    }
    
    @Override
    public void writeStartObject(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw("{\n");
        indentLevel++;
    }
    
    @Override
    public void writeEndObject(JsonGenerator jsonGenerator, int i) throws IOException
    {
        indentLevel--;
        jsonGenerator.writeRaw('\n');
        indent(jsonGenerator);
        jsonGenerator.writeRaw("}");
    }
    
    @Override
    public void writeObjectEntrySeparator(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw(",");
        jsonGenerator.writeRaw("\n");
        indent(jsonGenerator);
    }
    
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw(": ");
    }
    
    @Override
    public void writeStartArray(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw("[");
        jsonGenerator.writeRaw("\n");
        indentLevel++;
        indent(jsonGenerator);
    }
    
    @Override
    public void writeEndArray(JsonGenerator jsonGenerator, int i) throws IOException
    {
        indentLevel--;
        jsonGenerator.writeRaw("\n");
        indent(jsonGenerator);
        jsonGenerator.writeRaw("]");
    }
    
    @Override
    public void writeArrayValueSeparator(JsonGenerator jsonGenerator) throws IOException
    {
        jsonGenerator.writeRaw(",");
        jsonGenerator.writeRaw("\n");
        indent(jsonGenerator);
    }
    
    @Override
    public void beforeArrayValues(JsonGenerator jsonGenerator)
    {
    }
    
    @Override
    public void beforeObjectEntries(JsonGenerator jsonGenerator) throws IOException
    {
        indent(jsonGenerator);
    }
    
    private void indent(JsonGenerator jsonGenerator) throws IOException
    {
        for (int i = 0; i < indentLevel; i++)
        {
            jsonGenerator.writeRaw(INDENTATION);
        }
    }
    
}
