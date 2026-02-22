# Authorization Server (`auth`)

## 1) What this project is
`auth` is the OAuth2 Authorization Server for this repository. It issues tokens for registered clients (such as `adoption`) and provides issuer metadata/JWKS used by resource servers (such as `mcp-scheduler`) to validate JWT access tokens.

This project also enables MCP-specific authorization server behavior through the Spring AI Community MCP security integration.

## 2) What it implements (feature inventory)

### Core security and web
- Spring Boot 4.0.2
- Spring Security OAuth2 Authorization Server starter
- Spring Web MVC
- In-memory user store
- Delegating password encoder

### OAuth2 authorization server configuration
- One configured client: `default-client`
- Supported client authentication methods:
  - `client_secret_basic`
  - `client_secret_post`
  - `none`
- Supported grant types:
  - `authorization_code`
  - `client_credentials`
- Access token TTL: `1h`
- Redirect URIs for local `adoption` app callbacks:
  - `http://127.0.0.1:8080/authorize/oauth2/code/authserver`
  - `http://localhost:8080/authorize/oauth2/code/authserver`

### MCP security integration
- Dependency: `org.springaicommunity:mcp-authorization-server:0.1.1`
- Security configurer:
  - `http.with(mcpAuthorizationServer(), Customizer.withDefaults())`
- This layer is intended to add MCP-aligned authorization server behavior for MCP clients/servers.

### Runtime settings
- server port: `9000`
- custom session cookie name:
  - `MCP_AUTHORIZATION_SERVER_SESSIONID`

## 3) Main code walkthrough

### Authorization server enablement
- A `Customizer<HttpSecurity>` bean applies `mcpAuthorizationServer()`.
- Effect: authorization-server endpoints and security filter chain are set up by configurer/autoconfiguration.

### Local users for interactive auth
- `InMemoryUserDetailsManager` defines two users:
  - `george / pass`
  - `josh / pass`
- Passwords are encoded with `PasswordEncoderFactories.createDelegatingPasswordEncoder()`.

### Registered OAuth2 client
From `application.yaml`:
- logical client key: `default-client`
- actual client id/secret:
  - `client-id=default-client`
  - `client-secret={noop}default-secret`
- grants include auth code + client credentials.

## 4) OAuth2 endpoints this service provides
As an OAuth2 authorization server, this service exposes standard endpoint groups (authorization, token, metadata, JWK set). Exact endpoint paths are controlled by Spring Authorization Server defaults/config.

Used by other modules:
- `adoption` uses this server as OAuth2 client provider (`issuer-uri=http://localhost:9000`).
- `mcp-scheduler` uses this server as JWT issuer for resource-server validation (`issuer-uri=http://localhost:9000`).

## 5) How to run

### Prerequisites
- Java 25

### Start
- `cd auth`
- `./mvnw spring-boot:run`

Server starts on `http://localhost:9000`.

## 6) Notes
- Credentials and secrets are demo-only and must be replaced for any non-demo environment.
- Users and clients are currently in config/in-memory, not backed by persistent storage.
