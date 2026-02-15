package com.dg.auth;


import static org.springaicommunity.mcp.security.authorizationserver.config.McpAuthorizationServerConfigurer.mcpAuthorizationServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@SpringBootApplication
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

	@Bean
	Customizer<HttpSecurity> httpSecurityCustomizer() {
		return http -> http.with(mcpAuthorizationServer(), Customizer.withDefaults());
	}

	@Bean
	InMemoryUserDetailsManager userDetailsManager(PasswordEncoder passwordEncoder) {
		return new InMemoryUserDetailsManager(
			User.withUsername("george")
				.password(passwordEncoder.encode("pass"))
				.roles("USER")
				.build(),
			User.withUsername("josh")
				.password(passwordEncoder.encode("pass"))
				.roles("USER")
				.build()
		);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}


}
