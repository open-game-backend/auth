package de.opengamebackend.auth.controller.providers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class GithubAuthProvider implements AuthProvider {

    @Value("${de.opengamebackend.auth.provider.github.clientId}")
    private String clientId;

    @Value("${de.opengamebackend.auth.provider.github.clientSecret}")
    private String clientSecret;

    @Value("${de.opengamebackend.auth.provider.github.redirectUri}")
    private String redirectUri;

    @Override
    public String getId() {
        return "github";
    }

    @Override
    public String authenticate(String key, String context) {
        RestTemplate restTemplate = new RestTemplate();

        // Get OAuth2 token.
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/access_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", key)
                .queryParam("state", context);
        String uri = builder.toUriString();

        HttpHeaders headers = getDefaultHttpHeaders();
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<GithubGetAccessTokenResponse> getAccessTokenResponse = restTemplate.exchange
                (uri, HttpMethod.POST, httpEntity, GithubGetAccessTokenResponse.class);

        if (getAccessTokenResponse.getStatusCodeValue() >= 400 ||
                getAccessTokenResponse.getBody() == null ||
                getAccessTokenResponse.getBody().getAccessToken() == null) {
            return null;
        }

        // Get user details.
        headers.set("Authorization", "token " + getAccessTokenResponse.getBody().getAccessToken());
        httpEntity = new HttpEntity<>(headers);

        ResponseEntity<GithubGetUserResponse> getUserResponse = restTemplate.exchange("https://api.github.com/user",
                HttpMethod.GET, httpEntity, GithubGetUserResponse.class);

        if (getUserResponse.getStatusCodeValue() >= 400 ||
                getUserResponse.getBody() == null ||
                getUserResponse.getBody().getLogin() == null) {
            return null;
        }

        return getUserResponse.getBody().getLogin();
    }

    private HttpHeaders getDefaultHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        List<MediaType> acceptedTypes = new ArrayList<>();
        acceptedTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(acceptedTypes);

        return httpHeaders;
    }
}
