package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.controller.providers.AuthProvider;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AuthService {
    private Logger logger = LoggerFactory.getLogger(AuthService.class);

    private RoleRepository roleRepository;
    private PlayerRepository playerRepository;
    private List<AuthProvider> providers;

    @Autowired
    public AuthService(RoleRepository roleRepository, PlayerRepository playerRepository, List<AuthProvider> providers) {
        this.roleRepository = roleRepository;
        this.playerRepository = playerRepository;
        this.providers = providers;
    }

    public LoginResponse login(LoginRequest request) throws ApiException {
        // Look up provider.
        AuthProvider provider = providers.stream().filter(p -> p.getId().equals(request.getProvider())).findAny().orElse(null);

        if (provider == null) {
            logger.info("Login failed - unknown auth provider: {}", request.getProvider());
            throw new ApiException(ApiErrors.UNKNOWN_AUTH_PROVIDER_CODE, ApiErrors.UNKNOWN_AUTH_PROVIDER_MESSAGE);
        }

        // Authenticate player.
        String userId = provider.authenticate(request.getKey(), request.getContext());

        if (userId == null) {
            logger.info("Login failed - failed to authenticate with provider {}.", request.getProvider());
            throw new ApiException(ApiErrors.INVALID_CREDENTIALS_CODE, ApiErrors.INVALID_CREDENTIALS_MESSAGE);
        }

        // Look up role.
        Role role = roleRepository.findByName(request.getRole());

        if (role == null) {
            logger.info("Login failed - unknown role: {}", request.getRole());
            throw new ApiException(ApiErrors.INVALID_ROLE_CODE, ApiErrors.INVALID_ROLE_MESSAGE);
        }

        // Look up player.
        boolean firstTimeSetup = false;

        Player player = playerRepository.findByUserIdAndProvider(userId, request.getProvider()).orElse(null);

        if (player == null) {
            player = new Player();
            player.setRoles(Collections.singletonList(role));
            player.setUserId(userId);
            player.setProvider(request.getProvider());

            // Check if we're running the application for the very first time and need a first admin user.
            if (Role.ADMIN.equals(request.getRole())) {
                List<Player> admins = playerRepository.findByRoles(role);

                if (admins == null || admins.isEmpty()) {
                    // Create admin user and allow login.
                    logger.info("First time setup - admin created: {}", player.getUserId());

                    firstTimeSetup = true;
                } else {
                    // Lock new admin until unlocked by others.
                    player.setLocked(true);
                }
            }

            playerRepository.save(player);
        }

        // Send response.
        ArrayList<String> roles = new ArrayList<>();

        for (Role r : player.getRoles()) {
            roles.add(r.getName());
        }

        logger.info("Login successful for player {} as {} with provider {}{}.", player.getUserId(), request.getRole(),
                request.getProvider(), player.isLocked() ? " (locked)" : "");

        LoginResponse response = new LoginResponse(player.getUserId(), roles);
        response.setLocked(player.isLocked());
        response.setFirstTimeSetup(firstTimeSetup);
        return response;
    }
}
