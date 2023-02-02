package jsoneditor.controller.impl;

import jsoneditor.controller.Controller;
import jsoneditor.model.WritableModel;

public class ControllerImpl implements Controller {
    private WritableModel model;
    
    public ControllerImpl(WritableModel model) {
        this.model = model;
    }
}
