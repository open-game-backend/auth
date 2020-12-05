package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.controller.providers.AuthProvider;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.LockPlayerRequest;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.UnlockPlayerRequest;
import de.opengamebackend.auth.model.responses.GetAdminsResponse;
import de.opengamebackend.auth.model.responses.LockPlayerResponse;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.auth.model.responses.UnlockPlayerResponse;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class AuthServiceTests {
    private static final String TEST_PROVIDER_ID = "testProvider";

    private RoleRepository roleRepository;
    private PlayerRepository playerRepository;
    private AuthProvider authProvider;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        roleRepository = mock(RoleRepository.class);
        playerRepository = mock(PlayerRepository.class);

        authProvider = mock(AuthProvider.class);
        when(authProvider.getId()).thenReturn(TEST_PROVIDER_ID);

        authService = new AuthService(roleRepository, playerRepository, Lists.list(authProvider));
    }

    @Test
    public void givenAdmins_whenGetAdmins_thenReturnAdmins() throws ApiException {
        // GIVEN
        Role role = mock(Role.class);
        when(roleRepository.findByName(Role.ADMIN)).thenReturn(role);

        String admin1Id = "admin1";
        String admin2Id = "admin2";

        Player admin1 = mock(Player.class);
        when(admin1.getUserId()).thenReturn(admin1Id);
        when(admin1.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(admin1.isLocked()).thenReturn(false);

        Player admin2 = mock(Player.class);
        when(admin2.getUserId()).thenReturn(admin2Id);
        when(admin2.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(admin2.isLocked()).thenReturn(true);

        when(playerRepository.findByRoles(role)).thenReturn(Lists.list(admin1, admin2));

        // WHEN
        GetAdminsResponse response = authService.getAdmins();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getAdmins()).isNotNull();
        assertThat(response.getAdmins()).hasSize(2);
        assertThat(response.getAdmins().get(0).getUserId()).isEqualTo(admin1Id);
        assertThat(response.getAdmins().get(0).getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.getAdmins().get(0).isLocked()).isEqualTo(false);
        assertThat(response.getAdmins().get(1).getUserId()).isEqualTo(admin2Id);
        assertThat(response.getAdmins().get(1).getProvider()).isEqualTo(TEST_PROVIDER_ID);
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
        when(roleRepository.findByName(roleName)).thenReturn(role);

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
        String roleName = "testRole";

        Role role = mock(Role.class);
        when(roleRepository.findByName(roleName)).thenReturn(role);

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(roleName);
        when(authProvider.authenticate(any(), any())).thenReturn(playerId);

        Player player = mock(Player.class);
        when(player.getUserId()).thenReturn(playerId);
        when(playerRepository.findByUserIdAndProvider(playerId, TEST_PROVIDER_ID)).thenReturn(Optional.of(player));

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertThat(response.getPlayerId()).isEqualTo(playerId);
        assertThat(response.getProvider()).isEqualTo(TEST_PROVIDER_ID);
    }

    @Test
    public void givenFirstAdmin_whenLogin_thenReturnFirstTimeSetup() throws ApiException {
        // GIVEN
        Role role = mock(Role.class);
        when(role.getName()).thenReturn(Role.ADMIN);
        when(roleRepository.findByName(Role.ADMIN)).thenReturn(role);

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(Role.ADMIN);
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
        when(role.getName()).thenReturn(Role.ADMIN);
        when(roleRepository.findByName(Role.ADMIN)).thenReturn(role);

        LoginRequest request = mock(LoginRequest.class);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);
        when(request.getRole()).thenReturn(Role.ADMIN);
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
        String userId = "testPlayer";

        Player player = mock(Player.class);
        when(playerRepository.findByUserIdAndProvider(userId, TEST_PROVIDER_ID)).thenReturn(Optional.ofNullable(player));

        LockPlayerRequest request = mock(LockPlayerRequest.class);
        when(request.getUserId()).thenReturn(userId);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);

        // WHEN
        LockPlayerResponse response = authService.lockPlayer(request);

        // THEN
        verify(player).setLocked(true);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getProvider()).isEqualTo(TEST_PROVIDER_ID);
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
        String userId = "testPlayer";

        Player player = mock(Player.class);
        when(playerRepository.findByUserIdAndProvider(userId, TEST_PROVIDER_ID)).thenReturn(Optional.ofNullable(player));

        UnlockPlayerRequest request = mock(UnlockPlayerRequest.class);
        when(request.getUserId()).thenReturn(userId);
        when(request.getProvider()).thenReturn(TEST_PROVIDER_ID);

        // WHEN
        UnlockPlayerResponse response = authService.unlockPlayer(request);

        // THEN
        verify(player).setLocked(false);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getProvider()).isEqualTo(TEST_PROVIDER_ID);
        assertThat(response.isLocked()).isFalse();
    }
}
