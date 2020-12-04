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
}
