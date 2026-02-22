# MCP Scheduler Service (`mcp-scheduler`)

## 1) What this project is
`mcp-scheduler` is a Spring AI MCP server that exposes a scheduling tool for dog adoption appointments. It is protected as an OAuth2 Resource Server and validates JWTs against the `auth` issuer.

In the end-to-end system, this service is called as an MCP tool provider by the `adoption` app.

## 2) What it implements (feature inventory)

### MCP server
- Spring AI MCP server starter for Spring MVC:
  - `spring-ai-starter-mcp-server-webmvc`
- MCP server protocol configured as `streamable`
- MCP tool declared with annotation:
  - `@McpTool`
  - parameters with `@McpToolParam`

### OAuth2 resource server
- `spring-boot-starter-security-oauth2-resource-server`
- JWT issuer validation configured via:
  - `spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9000`
- MCP security configurer applied:
  - `http.with(mcpServerOAuth2(), a -> a.authorizationServer(issuer))`

### Business capability (tool)
- Tool name: inferred from method `schedule`
- Description: schedules pickup/adoption appointment
- Inputs:
  - `dogId` (int)
  - `dogName` (String)
- Output record:
  - `DogAdoptionScheduleResponse(username, appointment, dogId)`
- Behavior:
  - user identity taken from `SecurityContextHolder`
  - appointment time set to now + 5 days

### Runtime settings
- server port: `8090`
- virtual threads enabled

## 3) Main code walkthrough

### Security setup
- A `Customizer<HttpSecurity>` bean reads configured issuer URI.
- `mcpServerOAuth2()` is applied to secure MCP endpoints using OAuth2/JWT semantics.
- This enables user context to be available in tool execution when token is valid.

### MCP tool registration
- `DogAdoptionScheduler` is a Spring `@Service`.
- `schedule(...)` is annotated with `@McpTool`, making it discoverable as an MCP tool.
- Parameters are explicitly marked with `@McpToolParam`.
- Returned record is serializable tool output for the MCP client.

### Identity propagation
- During tool execution, username is resolved from Spring Security context.
- This allows per-user appointment attribution in the response payload.

## 4) How to run

### Prerequisites
- Java 25
- `auth` service running on `http://localhost:9000`

### Start
- `cd mcp-scheduler`
- `./mvnw spring-boot:run`

Server starts on `http://localhost:8090`.

## 5) Notes
- Appointment scheduling is deterministic demo logic (`+5 days`), not persisted.
- Tool method currently ignores `dogName` beyond accepting it as input.
