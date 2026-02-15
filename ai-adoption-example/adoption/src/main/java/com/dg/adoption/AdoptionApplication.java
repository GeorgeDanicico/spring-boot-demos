package com.dg.adoption;

import java.util.List;

import javax.sql.DataSource;

import org.springaicommunity.mcp.security.client.sync.AuthenticationMcpTransportContextProvider;
import org.springaicommunity.mcp.security.client.sync.oauth2.http.client.OAuth2AuthorizationCodeSyncHttpRequestCustomizer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class AdoptionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdoptionApplication.class, args);
	}

	@Bean 
	SecurityFilterChain securityFilterChain(HttpSecurity security) {
		return security
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.oauth2Client(Customizer.withDefaults())
				.build();
	}

	@Bean
	JdbcPostgresDialect jdbcPostgresDialect() {
		return JdbcPostgresDialect.INSTANCE;
	}

	@Bean
	OAuth2AuthorizationCodeSyncHttpRequestCustomizer auth2AuthorizationCodeSyncHttpRequestCustomizer(
		OAuth2AuthorizedClientManager authorizedClientManager) {
		return new OAuth2AuthorizationCodeSyncHttpRequestCustomizer(authorizedClientManager, "authserver");
	}

	@Bean
	McpSyncClientCustomizer mcpSyncClientCustomizer() {
		return (_, spec) -> spec.transportContextProvider(new AuthenticationMcpTransportContextProvider());
	}

	@Bean
	QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
		return QuestionAnswerAdvisor
			.builder(vectorStore)
			.build();
	}

	@Bean
	PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
		var jdbcRepository = JdbcChatMemoryRepository
			.builder()
			.dataSource(dataSource)
			.build();

		var mwa = MessageWindowChatMemory
			.builder()
			.chatMemoryRepository(jdbcRepository)
			.build();
		
		return PromptChatMemoryAdvisor
			.builder(mwa)
			.build();
	}

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {}

record Dog(int id, String name, String description) {}

@RestController
class AdoptionsController {
	private final ChatClient client;

	AdoptionsController(
		ToolCallbackProvider toolCallbackProvider,
		DogRepository dogRepository,
		VectorStore vectorStore,
		QuestionAnswerAdvisor questionAnswerAdvisor,
		PromptChatMemoryAdvisor promptChatMemoryAdvisor,
		ChatClient.Builder clientBuilder) {

	
		if (false) {
			dogRepository.findAll().forEach(dog -> {
				var dogument = new Document("id: %s, name: %s, description: %s".formatted(dog.id(), dog.name(), dog.description()));

				vectorStore.add(List.of(dogument));
			});
		}

		var systemPrompt = """
				You are an AI powered assistant to help people adopt a dog from the adoptions agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris, Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs availables will be presented below. If there is no information, then return a polite response suggesting we don't have any dogs available.
If somebody asks for a time to pick up the dog, don't ask other questions: simply provide a time by consulting the tools you have available.
				""";
		this.client = clientBuilder
			.defaultSystem(systemPrompt)
			.defaultOptions(
				OpenAiChatOptions.builder()
					.promptCacheKey("system_cache_key")
					.build()
			)
			.defaultToolCallbacks(toolCallbackProvider)
			.defaultAdvisors(questionAnswerAdvisor, promptChatMemoryAdvisor)
			.build();
		;
	}

	@GetMapping("/ask")
	String ask(@RequestParam String question) {
		var user = SecurityContextHolder.getContext().getAuthentication().getName();

		return this.client
			.prompt()
			.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))
			.user(question)
			.call()
			.content();
	}
}