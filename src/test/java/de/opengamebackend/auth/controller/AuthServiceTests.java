package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AuthServiceTests {
    private RoleRepository roleRepository;
    private PlayerRepository playerRepository;
    private ModelMapper modelMapper;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        roleRepository = mock(RoleRepository.class);
        playerRepository = mock(PlayerRepository.class);
        modelMapper = mock(ModelMapper.class);

        authService = new AuthService(roleRepository, playerRepository, modelMapper);
    }

    @Test
    public void whenRegister_thenPlayerIdIsSet() {
        // GIVEN
        RegisterRequest request = mock(RegisterRequest.class);

        Player player = new Player();
        when(modelMapper.map(request, Player.class)).thenReturn(player);

        // WHEN
        RegisterResponse response = authService.register(request);

        // THEN
        assertThat(player.getPlayerId()).isNotNull();
        assertThat(player.getPlayerId()).isNotEmpty();
        assertThat(response.getPlayerId()).isEqualTo(player.getPlayerId());
    }

    @Test
    public void givenUserRole_whenRegister_thenPlayerHasUserRole() {
        // GIVEN
        RegisterRequest request = mock(RegisterRequest.class);

        Role role = mock(Role.class);
        when(roleRepository.findByName(AuthService.ROLE_USER)).thenReturn(role);

        Player player = new Player();
        when(modelMapper.map(request, Player.class)).thenReturn(player);

        // WHEN
        authService.register(request);

        // THEN
        assertThat(player.getRoles()).isNotNull();
        assertThat(player.getRoles()).containsExactly(role);
    }

    @Test
    public void whenRegister_thenPlayerIsSaved() {
        // GIVEN
        RegisterRequest request = mock(RegisterRequest.class);

        Player player = new Player();
        when(modelMapper.map(request, Player.class)).thenReturn(player);

        // WHEN
        authService.register(request);

        // THEN
        verify(playerRepository).save(player);
    }
}
