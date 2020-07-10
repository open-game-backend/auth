package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.ErrorResponse;
import de.opengamebackend.auth.model.responses.RegisterResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController {
    private static final ErrorResponse ERROR_INVALID_CREDENTIALS =
            new ErrorResponse(100, "Invalid credentials.");
    private static final ErrorResponse ERROR_INVALID_ACCESS_TOKEN =
            new ErrorResponse(101, "Invalid access token.");

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequest request) {
        // Get request data.
        Player player = modelMapper.map(request, Player.class);
        player.setPlayerId(UUID.randomUUID().toString());

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
        if (!playerRepository.existsById(request.getPlayerId()))
        {
            return new ResponseEntity(ERROR_INVALID_CREDENTIALS, HttpStatus.BAD_REQUEST);
        }

        // Send response.
        return new ResponseEntity(HttpStatus.OK);
    }
}
