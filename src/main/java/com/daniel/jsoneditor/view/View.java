package com.daniel.jsoneditor.view;

import com.daniel.jsoneditor.model.observe.Observer;

public interface View extends Observer
{
    void cantValidateJson();
    
    void selectJsonAndSchema();
    
}
