package de.opengamebackend.auth.controller;

import com.google.common.base.Strings;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.ErrorResponse;
import de.opengamebackend.auth.model.responses.LoginResponse;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AuthController {
    private static final ErrorResponse ERROR_INVALID_CREDENTIALS =
            new ErrorResponse(100, "Invalid credentials.");
    private static final ErrorResponse ERROR_INVALID_ACCESS_TOKEN =
            new ErrorResponse(101, "Invalid access token.");

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";


    @Value("${de.opengamebackend.auth.adminId}")
    private String adminId;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @PostConstruct
    public void postConstruct() {
        if (Strings.isNullOrEmpty(adminId)) {
            throw new IllegalArgumentException("Property 'de.opengamebackend.auth.adminId' not set.");
        }

        // Seed roles.
        Role adminRole = getOrCreateRole(ROLE_ADMIN);
        Role userRole = getOrCreateRole(ROLE_USER);

        // Seed users.
        getOrCreatePlayer(adminId, userRole, adminRole);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequest request) {
        Role userRole = roleRepository.findByName(ROLE_USER);

        // Get request data.
        Player player = modelMapper.map(request, Player.class);
        player.setPlayerId(UUID.randomUUID().toString());
        player.setRoles(Arrays.asList(userRole));

        while (playerRepository.existsById(player.getPlayerId()))
        {
            player.setPlayerId(UUID.randomUUID().toString());
        }

        playerRepository.save(player);

        // Send response.
        RegisterResponse response = new RegisterResponse(player.getPlayerId());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequest request) {
        // Look up player.
        Optional<Player> optionalPlayer = playerRepository.findById(request.getPlayerId());

        if (!optionalPlayer.isPresent())
        {
            return new ResponseEntity(ERROR_INVALID_CREDENTIALS, HttpStatus.BAD_REQUEST);
        }

        Player player = optionalPlayer.get();

        // Send response.
        LoginResponse loginResponse = new LoginResponse(player.getPlayerId(), player.getRoles());
        return new ResponseEntity(loginResponse, HttpStatus.OK);
    }

    @Transactional
    private Role getOrCreateRole(String name) {
        Role role = roleRepository.findByName(name);

        if (role == null) {
            role = new Role(name);
            roleRepository.save(role);
        }

        return role;
    }

    private Player getOrCreatePlayer(String playerId, Role... roles) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);

        if (optionalPlayer.isPresent()) {
            return optionalPlayer.get();
        }

        Player player = new Player();
        player.setPlayerId(playerId);
        player.setRoles(Arrays.asList(roles));

        playerRepository.save(player);

        return player;
    }
}
