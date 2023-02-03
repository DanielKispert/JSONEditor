package jsoneditor.model;

import jsoneditor.model.observe.Subject;
import jsoneditor.model.statemachine.impl.State;

import java.io.File;

public interface ReadableModel extends ReadableState
{
    File getCurrentJSONFile();
    
    File getCurrentSchemaFile();
    
}
