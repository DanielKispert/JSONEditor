package jsoneditor.view;

import jsoneditor.model.observe.Observer;

public interface View extends Observer
{
    void cantValidateJson();
    
    void selectJsonAndSchema();
    
}
