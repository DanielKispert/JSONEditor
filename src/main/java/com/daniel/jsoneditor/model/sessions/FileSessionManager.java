package com.daniel.jsoneditor.model.sessions;

import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.statemachine.impl.EventSenderImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Manages multiple open file sessions. Used by both GUI and MCP server.
 * GUI sessions are protected from being closed via MCP.
 */
public class FileSessionManager
{
    private static final Logger logger = LoggerFactory.getLogger(FileSessionManager.class);
    
    private final Map<String, EditorSession> sessions = new ConcurrentHashMap<>();
    
    /**
     * Opens a JSON file with its schema and creates a new headless session.
     *
     * @param jsonPath absolute path to the JSON file
     * @param schemaPath absolute path to the schema file
     * @return session ID, or null if loading failed
     */
    public String openFile(final String jsonPath, final String schemaPath)
    {
        final File jsonFile = new File(jsonPath);
        final File schemaFile = new File(schemaPath);
        
        if (!jsonFile.exists())
        {
            logger.error("JSON file does not exist: {}", jsonPath);
            return null;
        }
        if (!schemaFile.exists())
        {
            logger.error("Schema file does not exist: {}", schemaPath);
            return null;
        }
        
        final JsonFileReaderAndWriterImpl reader = new JsonFileReaderAndWriterImpl();
        final JsonNode json = reader.getJsonFromFile(jsonFile);
        final JsonSchema schema = reader.getSchemaFromFileResolvingRefs(schemaFile);
        
        if (json == null || schema == null)
        {
            logger.error("Failed to load JSON or schema from files: {} / {}", jsonPath, schemaPath);
            return null;
        }
        
        final ModelImpl model = new ModelImpl(new EventSenderImpl());
        model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
        
        final String sessionId = generateUniqueId("");
        final EditorSession session = new EditorSession(sessionId, model, jsonFile, schemaFile, false);
        sessions.putIfAbsent(sessionId, session);
        
        logger.info("Opened file session {} for {}", sessionId, jsonPath);
        return sessionId;
    }
    
    /**
     * Registers an existing GUI model as a session. Protected from MCP close.
     *
     * @param model the GUI's model
     * @param jsonFile the JSON file
     * @param schemaFile the schema file
     * @return session ID
     */
    public String registerGuiSession(final ReadableModel model, final File jsonFile, final File schemaFile)
    {
        final String sessionId = generateUniqueId("gui-");
        final EditorSession session = new EditorSession(sessionId, model, jsonFile, schemaFile, true);
        sessions.putIfAbsent(sessionId, session);
        logger.info("Registered GUI session {} for {}", sessionId, jsonFile != null ? jsonFile.getAbsolutePath() : "null");
        return sessionId;
    }
    
    /**
     * Unregisters a GUI session (called when GUI closes a file).
     *
     * @param sessionId the session to unregister
     */
    public void unregisterGuiSession(final String sessionId)
    {
        sessions.computeIfPresent(sessionId, (key, session) ->
        {
            if (session.guiOwned())
            {
                logger.info("Unregistered GUI session {}", sessionId);
                return null; // removes the entry
            }
            return session;
        });
    }
    
    /**
     * Closes a headless session. Refuses to close GUI-owned sessions.
     *
     * @param sessionId the session to close
     * @return true if closed, false if not found or GUI-owned
     */
    public boolean closeFile(final String sessionId)
    {
        final boolean[] closed = {false};
        sessions.computeIfPresent(sessionId, (key, session) ->
        {
            if (session.guiOwned())
            {
                logger.warn("Cannot close GUI-owned session {} via MCP", sessionId);
                return session; // keep it
            }
            logger.info("Closed file session {}", sessionId);
            closed[0] = true;
            return null; // removes the entry
        });
        return closed[0];
    }
    
    /**
     * @param sessionId the session ID
     * @return the session or null if not found
     */
    public EditorSession getSession(final String sessionId)
    {
        return sessions.get(sessionId);
    }
    
    /**
     * @return list of all active sessions
     */
    public List<EditorSession> listSessions()
    {
        return new ArrayList<>(sessions.values());
    }

    private String generateUniqueId(final String prefix)
    {
        for (int i = 0; i < 10; i++)
        {
            final String candidate = UUID.randomUUID().toString().substring(0, 8);
            final String fullKey = prefix + candidate;
            if (!sessions.containsKey(fullKey))
            {
                return fullKey;
            }
        }
        // Fallback to full UUID if collisions persist
        return prefix + UUID.randomUUID().toString();
    }
}
