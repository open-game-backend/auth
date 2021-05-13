package de.opengamebackend.auth.controller.providers;

import de.opengamebackend.auth.model.repositories.SecretKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerAuthProvider implements AuthProvider {
    private final SecretKeyRepository secretKeyRepository;

    @Autowired
    public ServerAuthProvider(SecretKeyRepository secretKeyRepository) {
        this.secretKeyRepository = secretKeyRepository;
    }

    @Override
    public String getId() {
        return "server";
    }

    @Override
    public String authenticate(String key, String context) {
        return secretKeyRepository.findById(key).isPresent() ? "SERVER" : null;
    }
}
