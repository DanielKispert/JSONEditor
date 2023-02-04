package jsoneditor.controller;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;

public interface Controller
{

    void launchFinished();
    
    void jsonAndSchemaSelected(File json, File schema);
    
    void chooseNodeFromNavbar(String name, JsonNode node);


    

}
