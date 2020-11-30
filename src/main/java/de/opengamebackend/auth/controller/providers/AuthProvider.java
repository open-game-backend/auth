package de.opengamebackend.auth.controller.providers;

public interface AuthProvider {
    /**
     * Returns a unique identifier for this auth provider class, which requests can specify to choose this provider.
     *
     * @return provider-specific unique ID
     */
    String getId();

    /**
     * Authenticates a user with the specified provider-specific key (e.g. OAuth2 code for token exchange) and optional
     * context (e.g. string to prevent fraud).
     *
     * @param key Provider-specific authentication key.
     * @param context Optional provider-specific authentication context.
     * @return Id of the authenticated player, or null if authentication failed.
     */
    String authenticate(String key, String context);
}
