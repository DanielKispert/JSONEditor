package com.daniel.jsoneditor.model.sessions;

import com.daniel.jsoneditor.controller.impl.json.impl.JsonFileReaderAndWriterImpl;
import com.daniel.jsoneditor.model.ReadableModel;
import com.daniel.jsoneditor.model.impl.ModelImpl;
import com.daniel.jsoneditor.model.json.schema.SchemaHelper;
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
import java.util.stream.Collectors;


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
     * @return result with sessionId on success, or error message on failure
     */
    public OpenFileResult openFile(final String jsonPath, final String schemaPath)
    {
        final File jsonFile = new File(jsonPath);
        final File schemaFile = new File(schemaPath);
        
        if (!jsonFile.exists())
        {
            logger.error("JSON file does not exist: {}", jsonPath);
            return new OpenFileResult(null, "JSON file does not exist: " + jsonPath);
        }
        if (!schemaFile.exists())
        {
            logger.error("Schema file does not exist: {}", schemaPath);
            return new OpenFileResult(null, "Schema file does not exist: " + schemaPath);
        }
        
        final JsonFileReaderAndWriterImpl reader = new JsonFileReaderAndWriterImpl();
        final JsonNode json;
        final JsonSchema schema;
        try
        {
            json = reader.getJsonFromFile(jsonFile);
            schema = reader.getSchemaFromFileResolvingRefs(schemaFile);
        }
        catch (Exception e)
        {
            logger.error("Failed to parse JSON or schema files: {} / {}", jsonPath, schemaPath, e);
            return new OpenFileResult(null, "Failed to parse files: " + e.getMessage());
        }
        
        if (json == null || schema == null)
        {
            logger.error("Failed to load JSON or schema from files: {} / {}", jsonPath, schemaPath);
            return new OpenFileResult(null, "Failed to parse JSON or schema files: " + jsonPath + " / " + schemaPath);
        }

        final List<String> validationErrors = SchemaHelper.validateJsonWithSchema(json, schema);
        if (!validationErrors.isEmpty())
        {
            final String errorDetails = String.join(", ", validationErrors);
            logger.error("JSON does not validate against schema: {} / {}", jsonPath, schemaPath);
            return new OpenFileResult(null, "JSON does not validate against schema: " + errorDetails);
        }
        
        final ModelImpl model = new ModelImpl(new EventSenderImpl());
        model.jsonAndSchemaSuccessfullyValidated(jsonFile, schemaFile, json, schema);
        
        EditorSession session;
        String sessionId;
        do
        {
            sessionId = generateUniqueId("");
            session = new EditorSession(sessionId, model, jsonFile, schemaFile, false);
        }
        while (sessions.putIfAbsent(sessionId, session) != null);
        
        logger.info("Opened file session {} for {}", sessionId, jsonPath);
        return new OpenFileResult(sessionId, null);
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
        EditorSession session;
        String sessionId;
        do
        {
            sessionId = generateUniqueId("gui-");
            session = new EditorSession(sessionId, model, jsonFile, schemaFile, true);
        }
        while (sessions.putIfAbsent(sessionId, session) != null);
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
        sessions.computeIfPresent(sessionId, (final String key, final EditorSession session) ->
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
     * @return {@link CloseFileResult#CLOSED} if closed, {@link CloseFileResult#NOT_FOUND} if not found,
     *         {@link CloseFileResult#GUI_OWNED} if the session is GUI-owned
     */
    public CloseFileResult closeFile(final String sessionId)
    {
        final CloseFileResult[] result = {CloseFileResult.NOT_FOUND};
        sessions.computeIfPresent(sessionId, (final String key, final EditorSession session) ->
        {
            if (session.guiOwned())
            {
                logger.warn("Cannot close GUI-owned session {} via MCP", sessionId);
                result[0] = CloseFileResult.GUI_OWNED;
                return session; // keep it
            }
            logger.info("Closed file session {}", sessionId);
            result[0] = CloseFileResult.CLOSED;
            return null; // removes the entry
        });
        return result[0];
    }
    
    /**
     * Returns the session for the given ID, or {@code null} if not found.
     *
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
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}
