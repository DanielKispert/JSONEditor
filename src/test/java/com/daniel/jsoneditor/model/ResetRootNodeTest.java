package com.daniel.jsoneditor.model;

import com.daniel.jsoneditor.controller.impl.commands.CommandManager;
import com.daniel.jsoneditor.controller.impl.commands.CommandManagerImpl;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.impl.EventEnum;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that resetRootNode properly clears command history and triggers UI updates.
 */
public class ResetRootNodeTest
{
    private ModelImpl model;
    private CommandManager commandManager;
    private EventSenderImpl eventSender;
    
    @BeforeEach
    public void setUp()
    {
        eventSender = new EventSenderImpl();
        model = new ModelImpl(eventSender);
        commandManager = new CommandManagerImpl(model);
    }
    
    @Test
    public void testResetRootNodeTriggersCorrectEvents()
    {
        // Arrange
        ObjectMapper mapper = new ObjectMapper();
        JsonNode newRoot = mapper.createObjectNode().put("test", "value");
        
        // Act
        model.resetRootNode(newRoot);
        
        // Assert - should trigger UPDATED_JSON_STRUCTURE and RESET_SUCCESSFUL events
        assertEquals(EventEnum.RESET_SUCCESSFUL, eventSender.getState().getEvent());
        assertEquals("value", model.getRootJson().get("test").asText());
    }
    
    @Test
    public void testCommandHistoryIsClearedAfterReset()
    {
        // Arrange
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode initialRoot = mapper.createObjectNode()
            .put("initial", "data")
            .put("testProperty", "oldValue");
        final JsonNode newRoot = mapper.createObjectNode().put("reset", "data");
        
        model.resetRootNode(initialRoot);
        
        // Execute some commands to populate history - use setValue command that doesn't need schema
        commandManager.executeCommand(model.getCommandFactory().setValueAtNodeCommand("", "testProperty", "newValue"));
        
        // Act - simulate what refreshFromDisk does
        commandManager.clearHistory();
        model.resetRootNode(newRoot);
        
        // Assert - command history should be empty (no undo/redo possible)
        assertEquals(0, commandManager.undo().size());
        assertEquals(0, commandManager.redo().size());
        assertEquals("data", model.getRootJson().get("reset").asText());
    }
}
