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
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    public static final String ROLE_USER = "ROLE_USER";

    private Logger logger = LoggerFactory.getLogger(AuthService.class);

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
        logger.info("Player created: {}", player.getPlayerId());
        return new RegisterResponse(player.getPlayerId());
    }

    public LoginResponse login(LoginRequest request) throws ApiException {
        // Look up player.
        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayerId());

        if (!optionalPlayer.isPresent())
        {
            logger.info("Login failed for player {}: {}", request.getPlayerId(), ApiErrors.INVALID_CREDENTIALS_MESSAGE);
            throw new ApiException(ApiErrors.INVALID_CREDENTIALS_CODE, ApiErrors.INVALID_CREDENTIALS_MESSAGE);
        }

        Player player = optionalPlayer.get();

        // Send response.
        ArrayList<String> roles = new ArrayList<>();

        for (Role role : player.getRoles()) {
            roles.add(role.getName());
        }

        logger.info("Login successful for player {}.", request.getPlayerId());
        return new LoginResponse(player.getPlayerId(), roles);
    }
}
