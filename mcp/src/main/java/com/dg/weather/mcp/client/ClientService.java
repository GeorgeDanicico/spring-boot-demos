package com.dg.weather.mcp.client;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

@Service
public class ClientService {
    private final AppMcpClient mcpClient;

    public ClientService(AppMcpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    public ListToolsResult getTools() {
        return mcpClient.get().listTools();
    }
    
    public CallToolResult callAlertTool(String state) {
        var alertResult = mcpClient.get().callTool(new CallToolRequest("getAlerts", Map.of("state", state)));

        return alertResult;
    }
}
