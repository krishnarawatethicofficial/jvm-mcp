package dev.jvmmcp.server.tool;

import java.util.List;
import java.util.Map;

/** Interface for modular MCP tool domain handlers. */
public interface ToolHandler {

    /** Tool names supported by this handler. */
    List<String> toolNames();

    /** MCP tool definitions and input schemas. */
    List<Map<String, Object>> toolDefinitions();

    /**
     * Executes the requested tool with provided arguments.
     *
     * @param toolName name of tool to execute
     * @param arguments input arguments map
     * @return MCP tool result map with "content" and optional "isError"
     * @throws Exception if tool execution encounters an unhandled error
     */
    Map<String, Object> execute(String toolName, Map<String, Object> arguments) throws Exception;
}
