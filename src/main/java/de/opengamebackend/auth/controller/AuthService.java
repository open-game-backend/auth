package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.controller.providers.AuthProvider;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.LockPlayerRequest;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.UnlockPlayerRequest;
import de.opengamebackend.auth.model.responses.*;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    public GetAdminsResponse getAdmins() {
        GetAdminsResponse response = new GetAdminsResponse();

        Role adminRole = roleRepository.findByName(Role.ADMIN);
        List<Player> admins = playerRepository.findByRoles(adminRole);

        for (Player admin : admins) {
            GetAdminsResponseAdmin responseAdmin =
                    new GetAdminsResponseAdmin(admin.getProvider(), admin.getProviderUserId(), admin.isLocked());
            response.getAdmins().add(responseAdmin);
        }

        return response;
    }

    public LoginResponse login(LoginRequest request) throws ApiException {
        // Look up provider.
        AuthProvider provider = providers.stream().filter(p -> p.getId().equals(request.getProvider())).findAny().orElse(null);

        if (provider == null) {
            logger.error("Login failed - unknown auth provider: {}", request.getProvider());
            throw new ApiException(ApiErrors.UNKNOWN_AUTH_PROVIDER_CODE, ApiErrors.UNKNOWN_AUTH_PROVIDER_MESSAGE);
        }

        // Authenticate player.
        String userId = provider.authenticate(request.getKey(), request.getContext());

        if (userId == null) {
            logger.error("Login failed - failed to authenticate with provider {}.", request.getProvider());
            throw new ApiException(ApiErrors.INVALID_CREDENTIALS_CODE, ApiErrors.INVALID_CREDENTIALS_MESSAGE);
        }

        // Look up role.
        Role role = roleRepository.findByName(request.getRole());

        if (role == null) {
            logger.error("Login failed - unknown role: {}", request.getRole());
            throw new ApiException(ApiErrors.INVALID_ROLE_CODE, ApiErrors.INVALID_ROLE_MESSAGE);
        }

        // Look up player.
        boolean firstTimeSetup = false;

        Player player = playerRepository.findByProviderAndProviderUserId(request.getProvider(), userId).orElse(null);

        if (player == null) {
            player = new Player();
            player.setId(UUID.randomUUID().toString());
            player.setRoles(Collections.singletonList(role));
            player.setProviderUserId(userId);
            player.setProvider(request.getProvider());

            // Check if we're running the application for the very first time and need a first admin user.
            if (Role.ADMIN.equals(request.getRole())) {
                List<Player> admins = playerRepository.findByRoles(role);

                if (admins == null || admins.isEmpty()) {
                    // Create admin user and allow login.
                    logger.info("First time setup - admin created: {} ({} {})",
                            player.getId(), player.getProvider(), player.getProviderUserId());

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

        logger.info("Login successful for player {} ({}) as {} with provider {}{}.", player.getProviderUserId(),
                player.getId(), request.getRole(), request.getProvider(), player.isLocked() ? " (locked)" : "");

        LoginResponse response = new LoginResponse(player.getId(), roles);
        response.setProvider(request.getProvider());
        response.setProviderUserId(userId);
        response.setLocked(player.isLocked());
        response.setFirstTimeSetup(firstTimeSetup);
        return response;
    }

    public LockPlayerResponse lockPlayer(LockPlayerRequest request) throws ApiException {
        setPlayerLocked(request.getProvider(), request.getProviderUserId(), true);
        return new LockPlayerResponse(request.getProvider(), request.getProviderUserId(), true);
    }

    public UnlockPlayerResponse unlockPlayer(UnlockPlayerRequest request) throws ApiException {
        setPlayerLocked(request.getProvider(), request.getProviderUserId(), false);
        return new UnlockPlayerResponse(request.getProvider(), request.getProviderUserId(), false);
    }

    private void setPlayerLocked(String provider, String providerUserId, boolean locked) throws ApiException {
        Player player = playerRepository.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);

        if (player == null) {
            logger.error("Failed to change player lock - player not found: {} ({})", providerUserId, provider);
            throw new ApiException(ApiErrors.PLAYER_NOT_FOUND_CODE, ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
        }

        player.setLocked(locked);
        playerRepository.save(player);

        logger.info("Player lock changed - {} ({}) - locked: {}", providerUserId, provider, locked);
    }
}
