package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class AuthService {
    public static final String ROLE_USER = "ROLE_USER";

    private RoleRepository roleRepository;
    private PlayerRepository playerRepository;
    private ModelMapper modelMapper;

    @Autowired
    public AuthService(RoleRepository roleRepository, PlayerRepository playerRepository, ModelMapper modelMapper) {
        this.roleRepository = roleRepository;
        this.playerRepository = playerRepository;
        this.modelMapper = modelMapper;
    }

    public RegisterResponse register(RegisterRequest request) {
        Role userRole = roleRepository.findByName(ROLE_USER);

        // Get request data.
        Player player = modelMapper.map(request, Player.class);
        player.setPlayerId(UUID.randomUUID().toString());
        player.setRoles(Collections.singletonList(userRole));

        while (playerRepository.existsById(player.getPlayerId())) {
            player.setPlayerId(UUID.randomUUID().toString());
        }

        playerRepository.save(player);

        // Return response.
        return new RegisterResponse(player.getPlayerId());
    }
}
