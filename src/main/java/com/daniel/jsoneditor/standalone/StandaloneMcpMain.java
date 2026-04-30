package com.daniel.jsoneditor.standalone;

import com.daniel.jsoneditor.model.mcp.JsonEditorMcpServer;
import com.daniel.jsoneditor.model.sessions.FileSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Standalone entry point for the MCP server without JavaFX GUI.
 * Files are opened/closed via MCP tools (open_file, close_file, list_files).
 */
public class StandaloneMcpMain
{
    private static final Logger logger = LoggerFactory.getLogger(StandaloneMcpMain.class);
    
    public static void main(final String[] args)
    {
        final int port = parsePort(args);
        
        final FileSessionManager sessionManager = new FileSessionManager();
        final JsonEditorMcpServer server = new JsonEditorMcpServer(sessionManager);
        
        try
        {
            server.start(port);
            logger.info("Standalone MCP server running on port {}. Use open_file tool to load JSON files.", port);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down standalone MCP server");
                server.stop();
            }));
            
            // Block main thread
            Thread.currentThread().join();
        }
        catch (IOException e)
        {
            logger.error("Failed to start MCP server on port {}: {}", port, e.getMessage());
            System.exit(1);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            server.stop();
        }
    }
    
    private static int parsePort(final String[] args)
    {
        for (int i = 0; i < args.length - 1; i++)
        {
            if ("--port".equals(args[i]))
            {
                try
                {
                    return Integer.parseInt(args[i + 1]);
                }
                catch (NumberFormatException e)
                {
                    logger.error("Invalid port: {}", args[i + 1]);
                    System.exit(1);
                }
            }
        }
        return JsonEditorMcpServer.DEFAULT_PORT;
    }
}

