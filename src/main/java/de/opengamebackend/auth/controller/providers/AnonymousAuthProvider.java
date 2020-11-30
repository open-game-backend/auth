package de.opengamebackend.auth.controller.providers;

import org.springframework.stereotype.Component;

@Component
public class AnonymousAuthProvider implements AuthProvider {
    @Override
    public String getId() {
        return "";
    }

    @Override
    public String authenticate(String key, String context) {
        return key;
    }
}
