# Adoption Service (`adoption`)

## 1) What this project is
`adoption` is the AI-facing Spring Boot application. It accepts user questions over HTTP, calls an OpenAI chat model through Spring AI, enriches answers with RAG over PostgreSQL/pgvector, keeps conversation memory in JDBC, and lets the model call MCP tools exposed by `mcp-scheduler`.

In this repository, this app is the orchestration layer between:
- user input
- LLM reasoning
- retrieval from local data (RAG)
- tool execution through MCP
- OAuth2-secured communication to MCP services

## 2) What it implements (feature inventory)

### API and app framework
- Spring Boot 4.0.2 (`adoption/pom.xml`)
- Spring Web MVC endpoint:
  - `GET /ask?question=...` (`adoption/src/main/java/com/dg/adoption/AdoptionApplication.java`)

### AI and LLM
- Spring AI ChatClient (`spring-ai-starter-model-openai`)
- OpenAI chat model via `spring.ai.openai.api-key`
- Prompt-level OpenAI option:
  - `promptCacheKey("system_cache_key")`

### RAG (retrieval-augmented generation)
- `QuestionAnswerAdvisor` configured with `VectorStore`
- pgvector-backed vector store (`spring-ai-starter-vector-store-pgvector`)
- PostgreSQL datastore via JDBC

### Chat memory
- JDBC chat memory repository (`spring-ai-starter-model-chat-memory-repository-jdbc`)
- `JdbcChatMemoryRepository`
- `MessageWindowChatMemory`
- `PromptChatMemoryAdvisor`
- conversation scoping with `ChatMemory.CONVERSATION_ID`

### MCP client and tool calling
- MCP client starter (`spring-ai-starter-mcp-client`)
- MCP security client integration (`mcp-client-security`)
- streamable HTTP MCP connection to scheduler service (`http://localhost:8090`)
- `ToolCallbackProvider` injected and registered as default tools in the `ChatClient`

### OAuth2 (client side)
- OAuth2 client starter (`spring-boot-starter-security-oauth2-client`)
- `oauth2Client(Customizer.withDefaults())` enabled in `SecurityFilterChain`
- local OAuth2 client registration named `authserver`
- MCP request customizer for auth code flow:
  - `OAuth2AuthorizationCodeSyncHttpRequestCustomizer`
- transport context propagation:
  - `AuthenticationMcpTransportContextProvider`

### Data access
- Spring Data JDBC (`spring-boot-starter-data-jdbc`)
- `DogRepository extends ListCrudRepository<Dog, Integer>`
- Postgres dialect bean (`JdbcPostgresDialect.INSTANCE`)

### Operational/runtime
- Actuator (`spring-boot-starter-actuator`)
- Devtools
- Virtual threads enabled (`spring.threads.virtual.enabled=true`)
- Docker compose for local PostgreSQL + pgvector (`adoption/compose.yaml`)

## 3) Main code walkthrough

### Security and OAuth2 client activation
- `SecurityFilterChain` allows all inbound HTTP requests and enables OAuth2 client support:
  - `authorizeHttpRequests(...permitAll())`
  - `.oauth2Client(Customizer.withDefaults())`
- Why this matters: outbound MCP calls can obtain/access OAuth2 tokens through Spring Security client infrastructure.

### MCP OAuth2 customization
- `OAuth2AuthorizationCodeSyncHttpRequestCustomizer` is created with registration id `authserver`.
- `McpSyncClientCustomizer` sets `AuthenticationMcpTransportContextProvider`.
- Combined effect:
  - MCP client requests can attach OAuth2 credentials using the configured auth flow.
  - User security context can be propagated into MCP transport metadata.

### RAG setup
- `QuestionAnswerAdvisor` bean uses the app's autoconfigured `VectorStore`.
- In constructor, `ChatClient.Builder.defaultAdvisors(questionAnswerAdvisor, promptChatMemoryAdvisor)` is used.
- Runtime behavior:
  - user query enters advisor chain
  - vector similarity retrieval runs
  - retrieved context is added before final model call

### Memory setup
- App builds a `JdbcChatMemoryRepository` from `DataSource`.
- It wraps that repository with `MessageWindowChatMemory`.
- `PromptChatMemoryAdvisor` is built over that memory.
- At request time, conversation id is set to current authenticated username:
  - `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))`

### Tool calling with MCP
- Constructor injects `ToolCallbackProvider`.
- `ChatClient` is built with `.defaultToolCallbacks(toolCallbackProvider)`.
- Result:
  - all tools discovered from configured MCP clients become callable by the model.
  - here, that includes scheduler tools from `mcp-scheduler` once connected.

### Endpoint
- `GET /ask`
- Reads `question` request param.
- Uses principal name from `SecurityContextHolder` as conversation id.
- Returns plain text model response.

## 4) Configuration reference (`application.yaml`)

### Data and schema
- `spring.datasource.url=jdbc:postgresql://localhost/mydatabase`
- `spring.datasource.username=myuser`
- `spring.datasource.password=secret`
- `spring.ai.vectorstore.pgvector.initialize-schema=true`
- `spring.ai.chat.memory.repository.jdbc.initialize-schema=always`

### OAuth2 client
- registration id: `authserver`
- client-id: `default-client`
- client-secret: `default-secret`
- grant type: `authorization_code`
- issuer: `http://localhost:9000`

### MCP client
- `spring.ai.mcp.client.type=SYNC`
- `spring.ai.mcp.client.initialized=false`
- streamable-http connection `scheduler.url=http://localhost:8090`

### OpenAI
- `spring.ai.openai.api-key=${OPENAI_API_KEY}`

## 5) How to run

### Prerequisites
- Java 25
- Maven (or wrapper)
- Docker (for pgvector)
- OpenAI API key in environment

### Startup order
1. Start auth server (`auth`) on `:9000`.
2. Start MCP scheduler (`mcp-scheduler`) on `:8090`.
3. Start PostgreSQL/pgvector:
   - `docker compose -f adoption/compose.yaml up -d`
4. Start adoption app on default `:8080`.

### Quick test
- `curl "http://localhost:8080/ask?question=When can I pick up dog 42?"`

## 6) Notes and current limitations
- Vector ingestion block is disabled (`if (false)`), so no dog documents are loaded into the vector store unless you change this.
- `permitAll()` means inbound endpoint authorization is intentionally open in this demo.
- MCP client initialization is set to `false`; behavior depends on how and when MCP clients are initialized in runtime wiring.
