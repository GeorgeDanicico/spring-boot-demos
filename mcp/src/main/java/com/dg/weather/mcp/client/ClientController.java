package com.dg.weather.mcp.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

@RestController("/")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/tools")
    public ListToolsResult getMcpTools() {
        return clientService.getTools();
    }

    @GetMapping("/alerts/{state}")
    public CallToolResult getAlerts(@PathVariable("state") String state) {
        return clientService.callAlertTool(state);
    }
}
