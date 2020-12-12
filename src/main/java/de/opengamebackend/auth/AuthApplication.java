package de.opengamebackend.auth;

import de.opengamebackend.util.EnableOpenGameBackendUtils;
import de.opengamebackend.util.config.ApplicationConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableOpenGameBackendUtils
public class AuthApplication {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	@Profile("!test")
	public OpenAPI customOpenAPI(ApplicationConfig applicationConfig) {
		return new OpenAPI().info(new Info()
				.title("Open Game Backend Auth")
				.version(applicationConfig.getVersion())
				.description("Provides authentication and authorization for all players of the Open Game Backend.")
				.license(new License().name("MIT").url("https://github.com/open-game-backend/auth/blob/develop/LICENSE")));
	}

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}
}
