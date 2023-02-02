package jsoneditor;

import com.google.gson.Gson;
import jsoneditor.controller.impl.ControllerImpl;
import jsoneditor.controller.Controller;
import jsoneditor.model.impl.ModelImpl;
import jsoneditor.model.statemachine.StateMachine;
import jsoneditor.model.statemachine.impl.StateMachineImpl;
import jsoneditor.view.View;
import jsoneditor.view.impl.ViewImpl;

public class Main {
    
    private static final Gson gson = new Gson();
    
    public static void main(String[] args)
    {
        StateMachine stateMachine = new StateMachineImpl();
        ModelImpl model = new ModelImpl(stateMachine);
        Controller controller = new ControllerImpl(model);
        View view = new ViewImpl(model, controller);
        view.observe(stateMachine);
    }
    
    public static Gson getGson()
    {
        return gson;
    }
    
}
