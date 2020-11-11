package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import de.opengamebackend.net.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class AuthServiceTests {
    private RoleRepository roleRepository;
    private PlayerRepository playerRepository;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        roleRepository = mock(RoleRepository.class);
        playerRepository = mock(PlayerRepository.class);

        authService = new AuthService(roleRepository, playerRepository, new ModelMapper());
    }

    @Test
    public void whenRegister_thenPlayerIdIsSet() {
        // GIVEN
        RegisterRequest request = mock(RegisterRequest.class);

        // WHEN
        RegisterResponse response = authService.register(request);

        // THEN
        assertThat(response.getPlayerId()).isNotNull();
        assertThat(response.getPlayerId()).isNotEmpty();
    }

    @Test
    public void givenUserRole_whenRegister_thenPlayerHasUserRole() {
        // GIVEN
        RegisterRequest request = mock(RegisterRequest.class);

        Role role = mock(Role.class);
        when(roleRepository.findByName(AuthService.ROLE_USER)).thenReturn(role);

        // WHEN
        authService.register(request);

        // THEN
        ArgumentCaptor<Player> argument = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(argument.capture());
        Player player = argument.getValue();

        assertThat(player.getRoles()).isNotNull();
        assertThat(player.getRoles()).containsExactly(role);
    }

    @Test
    public void givenInvalidPlayerId_whenLogin_thenThrowApiError() {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setPlayerId("testId");

        // WHEN & THEN
        assertThatExceptionOfType(ApiException.class).isThrownBy(() -> authService.login(request));
    }

    @Test
    public void givenValidPlayerId_whenLogin_thenReturnRoles() throws ApiException {
        // GIVEN
        String roleName = "testRole";

        Role role = mock(Role.class);
        when(role.getName()).thenReturn(roleName);

        Player player = new Player();
        player.setRoles(Collections.singletonList(role));

        when(playerRepository.findById(any())).thenReturn(Optional.of(player));

        LoginRequest request = mock(LoginRequest.class);

        // WHEN
        LoginResponse response = authService.login(request);

        // THEN
        assertThat(response.getRoles()).containsExactly(roleName);
    }
}
