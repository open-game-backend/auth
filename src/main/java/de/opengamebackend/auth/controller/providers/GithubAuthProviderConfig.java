package de.opengamebackend.auth.controller.providers;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@ConstructorBinding
@ConfigurationProperties("de.opengamebackend.auth.provider.github")
@Validated
public class GithubAuthProviderConfig {
    @NotNull
    private String clientId;

    @NotNull
    private String clientSecret;

    @NotNull
    private String redirectUri;

    public GithubAuthProviderConfig(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
