package de.opengamebackend.auth.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.requests.AuthRequest;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.RegisterRequest;
import de.opengamebackend.auth.model.responses.AuthResponse;
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
import java.util.Date;
import java.util.UUID;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@RestController
public class AuthController {
    private static final ErrorResponse ERROR_INVALID_CREDENTIALS =
            new ErrorResponse(100, "Invalid credentials.");
    private static final ErrorResponse ERROR_INVALID_ACCESS_TOKEN =
            new ErrorResponse(101, "Invalid access token.");

    public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${de.opengamebackend.auth.JWTSecret}")
    private String JWTSecret;

    @PostConstruct
    public void init() {
        if (Strings.isNullOrEmpty(JWTSecret)) {
            throw new IllegalArgumentException("de.opengamebackend.auth.JWTSecret not set.");
        }
    }

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

        String token = JWT.create()
                .withSubject(request.getPlayerId())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(JWTSecret.getBytes()));

        // Send response.
        LoginResponse response = new LoginResponse(token);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/auth")
    public ResponseEntity auth(@RequestBody AuthRequest request) {
        try {
            // Verify token.
            String playerId = JWT.require(Algorithm.HMAC512(JWTSecret.getBytes()))
                    .build()
                    .verify(request.getToken())
                    .getSubject();

            // Send response.
            AuthResponse response = new AuthResponse(playerId);
            return new ResponseEntity(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(ERROR_INVALID_ACCESS_TOKEN, HttpStatus.BAD_REQUEST);
        }
    }
}
