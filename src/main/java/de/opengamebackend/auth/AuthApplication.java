package de.opengamebackend.auth;

import de.opengamebackend.util.EnableOpenGameBackendUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
@EnableOpenGameBackendUtils
public class AuthApplication {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public OpenAPI customOpenAPI() throws IOException, XmlPullParserException {
		MavenXpp3Reader reader = new MavenXpp3Reader();
		Model model = reader.read(new FileReader("pom.xml"));

		return new OpenAPI().info(new Info()
				.title("Open Game Backend Auth")
				.version(model.getVersion())
				.description("Provides authentication and authorization for all players of the Open Game Backend.")
				.license(new License().name("MIT").url("https://github.com/open-game-backend/auth/blob/develop/LICENSE")));
	}

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}
}
