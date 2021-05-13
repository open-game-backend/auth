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
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class AuthServiceTests {
    private static final String TEST_PROVIDER_ID = "testProvider";

    private RoleRepository roleRepository;
    private PlayerRepository playerRepository;
    private SecretKeyRepository secretKeyRepository;
    private AuthProvider authProvider;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        roleRepository = mock(RoleRepository.class);
        playerRepository = mock(PlayerRepository.class);
        secretKeyRepository = mock(SecretKeyRepository.class);

        authProvider = mock(AuthProvider.class);
        when(authProvider.getId()).thenReturn(TEST_PROVIDER_ID);

        authService = new AuthService(roleRepository, playerRepository, secretKeyRepository, Lists.list(authProvider));
    }

    @Test
    public void givenPlayers_whenGetPlayers_thenReturnPlayers() {
        // GIVEN
        Role role = mock(Role.class);
        when(roleRepository.findById(AuthRole.ROLE_USER.name())).thenReturn(Optional.of(role));

        String player1Id = "player1";
        String player1ProviderUserId = "providerPlayer1";
        String player2Id = "player2";
        String player2ProviderUserId = "providerPlayer2";

        Player player1 = mock(Player.class);
        when(player1.getId()).thenReturn(player1Id);
        when(player1.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(player1.getProviderUserId()).thenReturn(player1ProviderUserId);
        when(player1.isLocked()).thenReturn(false);

        Player player2 = mock(Player.class);
        when(player2.getId()).thenReturn(player2Id);
        when(player2.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(player2.getProviderUserId()).thenReturn(player2ProviderUserId);
        when(player2.isLocked()).thenReturn(true);

        List<Player> players = Lists.list(player1, player2);

        when(playerRepository.findByRoles(eq(role), any())).thenReturn(players);
        when(playerRepository.countByRoles(role)).thenReturn(players.size());

        // WHEN
        GetPlayersResponse response = authService.getPlayers(0);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getPlayers()).isNotNull();
        assertThat(response.getPlayers()).hasSize(2);
        assertThat(response.getPlayers().get(0).getPlayerId()).isEqualTo(player1Id);
        assertThat(response.getPlayers().get(0).getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getPlayers().get(0).getProviderUserId()).isEqualTo(player1ProviderUserId);
        assertThat(response.getPlayers().get(1).getPlayerId()).isEqualTo(player2Id);
        assertThat(response.getPlayers().get(1).getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getPlayers().get(1).getProviderUserId()).isEqualTo(player2ProviderUserId);
        assertThat(response.getTotalPlayers()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
    }

    @Test
    public void givenAdmins_whenGetAdmins_thenReturnAdmins() {
        // GIVEN
        Role role = mock(Role.class);
        when(roleRepository.findById(AuthRole.ROLE_ADMIN.name())).thenReturn(Optional.of(role));

        String admin1Id = "admin1";
        String admin2Id = "admin2";

        Player admin1 = mock(Player.class);
        when(admin1.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(admin1.getProviderUserId()).thenReturn(admin1Id);
        when(admin1.isLocked()).thenReturn(false);

        Player admin2 = mock(Player.class);
        when(admin2.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(admin2.getProviderUserId()).thenReturn(admin2Id);
        when(admin2.isLocked()).thenReturn(true);

        when(playerRepository.findByRoles(role)).thenReturn(Lists.list(admin1, admin2));

        // WHEN
        GetAdminsResponse response = authService.getAdmins();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getAdmins()).isNotNull();
        assertThat(response.getAdmins()).hasSize(2);
        assertThat(response.getAdmins().get(0).getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getAdmins().get(0).getProviderUserId()).isEqualTo(admin1Id);
        assertThat(response.getAdmins().get(0).isLocked()).isEqualTo(false);
        assertThat(response.getAdmins().get(1).getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getAdmins().get(1).getProviderUserId()).isEqualTo(admin2Id);
        assertThat(response.getAdmins().get(1).isLocked()).isEqualTo(true);
    }

    @Test
    public void givenUnknownProvider_whenLogin_thenThrowException() {
        // GIVEN
        LoginRequest request = mock(LoginRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> authService.login(request))
                .withMessage(ApiErrors.UNKNOWN_AUTH_PROVIDER_MESSAGE);
    }

    @Test
    public void givenInvalidKey_whenLogin_thenThrowException() {
        // GIVEN
        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> authService.login(request))
                .withMessage(ApiErrors.INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    public void givenInvalidRole_whenLogin_thenThrowException() {
        // GIVEN
        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(authProvider.authenticate(any(), any())).thenReturn("testPlayerId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> authService.login(request))
                .withMessage(ApiErrors.INVALID_ROLE_MESSAGE);
    }

    @Test
    public void givenValidRequest_whenLogin_thenReturnPlayer() throws ApiException {
        // GIVEN
        String roleName = "testRole";

        Role role = mock(Role.class);
        when(role.getName()).thenReturn(roleName);
        when(roleRepository.findById(roleName)).thenReturn(Optional.of(role));

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(roleName);
        when(authProvider.authenticate(any(), any())).thenReturn("testPlayerId");

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertThat(response.getPlayerId()).isNotNull();
        assertThat(response.getPlayerId()).isNotEmpty();
        assertThat(response.getRoles()).containsExactly(roleName);
    }

    @Test
    public void givenExistingPlayer_whenLogin_thenReturnPlayer() throws ApiException {
        // GIVEN
        String playerId = "testPlayerId";
        String providerUserId = "testProviderUserId";
        String roleName = "testRole";

        Role role = mock(Role.class);
        when(roleRepository.findById(roleName)).thenReturn(Optional.of(role));

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(roleName);
        when(authProvider.authenticate(any(), any())).thenReturn(providerUserId);

        Player player = mock(Player.class);
        when(player.getId()).thenReturn(playerId);
        when(playerRepository.findByProviderAndProviderUserId(TEST_PROVIDER_ID, providerUserId)).thenReturn(Optional.of(player));

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertThat(response.getPlayerId()).isEqualTo(playerId);
        assertThat(response.getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getProviderUserId()).isEqualTo(providerUserId);
    }

    @Test
    public void givenFirstAdmin_whenLogin_thenReturnFirstTimeSetup() throws ApiException {
        // GIVEN
        Role role = mock(Role.class);
        when(role.getName()).thenReturn(AuthRole.ROLE_ADMIN.name());
        when(roleRepository.findById(AuthRole.ROLE_ADMIN.name())).thenReturn(Optional.of(role));

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(AuthRole.ROLE_ADMIN.name());
        when(authProvider.authenticate(any(), any())).thenReturn("testPlayerId");

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertThat(response.isFirstTimeSetup()).isTrue();
    }

    @Test
    public void givenSecondAdmin_whenLogin_thenLockPlayer() throws ApiException {
        // GIVEN
        Role role = mock(Role.class);
        when(role.getName()).thenReturn(AuthRole.ROLE_ADMIN.name());
        when(roleRepository.findById(AuthRole.ROLE_ADMIN.name())).thenReturn(Optional.of(role));

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(AuthRole.ROLE_ADMIN.name());
        when(authProvider.authenticate(any(), any())).thenReturn("testPlayerId");

        Player existingAdmin = mock(Player.class);
        when(playerRepository.findByRoles(role)).thenReturn(Lists.list(existingAdmin));

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertThat(response.isLocked()).isTrue();
    }

    @Test
    public void givenInvalidPlayer_whenLockPlayer_thenThrowException() {
        // GIVEN
        LockPlayerRequest request = mock(LockPlayerRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> authService.lockPlayer(request))
                .withMessage(ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenValidPlayer_whenLockPlayer_thenPlayerLocked() throws ApiException {
        // GIVEN
        String providerUserId = "testPlayer";

        Player player = mock(Player.class);
        when(playerRepository.findByProviderAndProviderUserId(TEST_PROVIDER_ID, providerUserId)).thenReturn(Optional.ofNullable(player));

        LockPlayerRequest request = mock(LockPlayerRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getProviderUserId()).thenReturn(providerUserId);

        // WHEN
        LockPlayerResponse response = authService.lockPlayer(request);

        // THEN
        verify(player).setLocked(true);

        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getProviderUserId()).isEqualTo(providerUserId);
        assertThat(response.isLocked()).isTrue();
    }

    @Test
    public void givenInvalidPlayer_whenUnlockPlayer_thenThrowException() {
        // GIVEN
        UnlockPlayerRequest request = mock(UnlockPlayerRequest.class);

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> authService.unlockPlayer(request))
                .withMessage(ApiErrors.PLAYER_NOT_FOUND_MESSAGE);
    }

    @Test
    public void givenValidPlayer_whenUnlockPlayer_thenPlayerUnlocked() throws ApiException {
        // GIVEN
        String providerUserId = "testPlayer";

        Player player = mock(Player.class);
        when(playerRepository.findByProviderAndProviderUserId(TEST_PROVIDER_ID, providerUserId)).thenReturn(Optional.ofNullable(player));

        UnlockPlayerRequest request = mock(UnlockPlayerRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getProviderUserId()).thenReturn(providerUserId);

        // WHEN
        UnlockPlayerResponse response = authService.unlockPlayer(request);

        // THEN
        verify(player).setLocked(false);

        assertThat(response).isNotNull();
        assertThat(response.getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getProviderUserId()).isEqualTo(providerUserId);
        assertThat(response.isLocked()).isFalse();
    }

    @Test
    public void givenSecretKeys_whenGetSecretKeys_thenReturnKeys() {
        // GIVEN
        SecretKey key1 = mock(SecretKey.class);
        when(key1.getKey()).thenReturn("key1");

        SecretKey key2 = mock(SecretKey.class);
        when(key2.getKey()).thenReturn("key2");

        when(secretKeyRepository.findAll()).thenReturn(Lists.list(key1, key2));

        // WHEN
        GetSecretKeysResponse response = authService.getSecretKeys();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getKeys()).isNotNull();
        assertThat(response.getKeys()).hasSize(2);
        assertThat(response.getKeys().get(0)).isEqualTo(key1.getKey());
        assertThat(response.getKeys().get(1)).isEqualTo(key2.getKey());
    }

    @Test
    public void whenGenerateSecretKey_thenSavesNewKey() {
        // WHEN
        authService.generateSecretKey();

        // THEN
        ArgumentCaptor<SecretKey> argumentCaptor = ArgumentCaptor.forClass(SecretKey.class);
        verify(secretKeyRepository).save(argumentCaptor.capture());

        SecretKey savedKey = argumentCaptor.getValue();

        assertThat(savedKey).isNotNull();
        assertThat(savedKey.getKey()).isNotNull();
        assertThat(savedKey.getKey()).isNotEmpty();
    }

    @Test
    public void whenGenerateSecretKey_thenReturnsNewKey() {
        // WHEN
        GenerateSecretKeyResponse response = authService.generateSecretKey();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getKey()).isNotNull();
        assertThat(response.getKey()).isNotEmpty();
    }

    @Test
    public void givenInvalidKey_whenRemoveSecretKey_thenThrowException() {
        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class)
                .isThrownBy(() -> authService.removeSecretKey(null))
                .withMessage(ApiErrors.INVALID_SECRET_KEY_MESSAGE);
    }

    @Test
    public void givenValidKey_whenRemoveSecretKey_thenDeletesKey() throws ApiException {
        // GIVEN
        String key = "testKey";

        SecretKey secretKey = mock(SecretKey.class);
        when(secretKeyRepository.findById(key)).thenReturn(Optional.of(secretKey));

        // WHEN
        authService.removeSecretKey(key);

        // THEN
        verify(secretKeyRepository).delete(secretKey);
    }
}
