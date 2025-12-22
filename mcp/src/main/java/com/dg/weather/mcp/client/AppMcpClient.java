package com.dg.weather.mcp.client;

import org.springframework.stereotype.Component;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class AppMcpClient {
    private McpSyncClient client;

    @PostConstruct
    void start() {
        System.out.println("Bean created at startup");
        var stdioParams = ServerParameters.builder("java")
			.args("-jar",
					"/Users/george/Documents/github/spring-boot-demos/mcp/target/weather.mcp-0.0.1-SNAPSHOT.jar")
			.build();

		var transport = new StdioClientTransport(stdioParams, McpJsonMapper.createDefault());
		var client = McpClient.sync(transport).build();

		client.initialize();
    }

    @PreDestroy
    void stop() {
        System.out.println("Bean destroyed on shutdown");
        client.closeGracefully();
    }

    public McpSyncClient get() {
        return this.client;
    }
}
