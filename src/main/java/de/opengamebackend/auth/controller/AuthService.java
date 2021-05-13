package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.controller.providers.AuthProvider;
import de.opengamebackend.auth.model.AuthRole;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.entities.SecretKey;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.repositories.SecretKeyRepository;
import de.opengamebackend.auth.model.requests.LockPlayerRequest;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.UnlockPlayerRequest;
import de.opengamebackend.auth.model.responses.*;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {
    private static final int PAGE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RoleRepository roleRepository;
    private final PlayerRepository playerRepository;
    private final SecretKeyRepository secretKeyRepository;

    private final List<AuthProvider> providers;

    @Autowired
    public AuthService(RoleRepository roleRepository, PlayerRepository playerRepository,
                       SecretKeyRepository secretKeyRepository, List<AuthProvider> providers) {
        this.roleRepository = roleRepository;
        this.playerRepository = playerRepository;
        this.secretKeyRepository = secretKeyRepository;

        this.providers = providers;
    }

    public GetPlayersResponse getPlayers(int page) {
        Role playerRole = roleRepository.findById(AuthRole.ROLE_USER.name()).orElse(null);
        Pageable sortedPageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id"));
        List<Player> players = playerRepository.findByRoles(playerRole, sortedPageable);
        List<GetPlayersResponsePlayer> responsePlayers = players.stream()
                .map(p -> new GetPlayersResponsePlayer(p.getId(), p.getProvider(), p.getProviderUserId()))
                .collect(Collectors.toList());
        int totalPlayers = playerRepository.countByRoles(playerRole);
        int totalPages = totalPlayers > PAGE_SIZE ? (totalPlayers / PAGE_SIZE) + 1 : 1;
        return new GetPlayersResponse(responsePlayers, totalPlayers, totalPages);
    }

    public GetAdminsResponse getAdmins() {
        Role adminRole = roleRepository.findById(AuthRole.ROLE_ADMIN.name()).orElse(null);
        List<Player> admins = playerRepository.findByRoles(adminRole);

        return new GetAdminsResponse(admins.stream()
                .map(a -> new GetAdminsResponseAdmin(a.getProvider(), a.getProviderUserId(), a.isLocked()))
                .collect(Collectors.toList()));
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
        Role role = roleRepository.findById(request.getRole()).orElse(null);

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
            if (AuthRole.ROLE_ADMIN.name().equals(request.getRole())) {
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

    public GetSecretKeysResponse getSecretKeys() {
        List<String> keys = new ArrayList<>();

        for (SecretKey key : secretKeyRepository.findAll()) {
            keys.add(key.getKey());
        }

        return new GetSecretKeysResponse(keys);
    }

    public GenerateSecretKeyResponse generateSecretKey() {
        String key = UUID.randomUUID().toString()
                .concat(UUID.randomUUID().toString())
                .concat(UUID.randomUUID().toString());

        SecretKey secretKey = new SecretKey(key);
        secretKeyRepository.save(secretKey);

        return new GenerateSecretKeyResponse(key);
    }

    public void removeSecretKey(String key) throws ApiException {
        Optional<SecretKey> secretKey = secretKeyRepository.findById(key);

        if (!secretKey.isPresent()) {
            throw new ApiException(ApiErrors.INVALID_SECRET_KEY_CODE, ApiErrors.INVALID_SECRET_KEY_MESSAGE);
        }

        secretKeyRepository.delete(secretKey.get());
    }
}
