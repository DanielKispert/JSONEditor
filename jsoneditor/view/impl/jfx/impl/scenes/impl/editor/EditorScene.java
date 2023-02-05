package jsoneditor.view.impl.jfx.impl.scenes.impl.editor;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jsoneditor.controller.Controller;
import jsoneditor.model.ReadableModel;
import jsoneditor.view.impl.jfx.impl.scenes.impl.SceneHandlerImpl;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.JsonEditorEditorWindow;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.JsonEditorNavbar;
import jsoneditor.view.impl.jfx.impl.scenes.impl.editor.components.JsonEditorToolbar;

public class EditorScene extends SceneHandlerImpl
{
    private JsonEditorEditorWindow editor;
    
    private JsonEditorNavbar navbar;
    
    private JsonEditorToolbar toolbar;
    
    
    public EditorScene(Controller controller, ReadableModel model)
    {
        super(controller, model);
    }
    
    @Override
    public Scene getScene(Stage stage)
    {
        double startingSceneWidth = 800;
        double startingSceneHeight = 600;
        
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, startingSceneWidth, startingSceneHeight);
        
        toolbar = new JsonEditorToolbar(model, controller);
        navbar = new JsonEditorNavbar(model, controller);
        editor = new JsonEditorEditorWindow(model, controller);
        root.setTop(toolbar);
        root.setLeft(makeSplitPane(scene));
        return scene;
    }
    
    private SplitPane makeSplitPane(Scene scene)
    {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(navbar, editor);
        splitPane.setDividerPositions(0.2);
        splitPane.prefWidthProperty().bind(scene.widthProperty());
        splitPane.prefHeightProperty().bind(scene.heightProperty());
        return splitPane;
    }
    
    public void updateSelectedJson()
    {
        editor.updateSelectedJson(model);
    }
    
    

    
}
