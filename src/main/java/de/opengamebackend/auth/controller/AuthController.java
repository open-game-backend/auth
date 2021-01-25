package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.requests.LockPlayerRequest;
import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.requests.UnlockPlayerRequest;
import de.opengamebackend.auth.model.responses.*;
import de.opengamebackend.net.ApiErrors;
import de.opengamebackend.net.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/admin/players")
    @Operation(summary = "Gets all players registered for this application, including locked ones.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All players registered for this application, including locked ones.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetAdminsResponse.class)) })
    })
    public ResponseEntity<GetPlayersResponse> getPlayers(@RequestParam(required = false, defaultValue = "0") int page) {
        GetPlayersResponse response = authService.getPlayers(page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin/admins")
    @Operation(summary = "Gets all admins registered for this application, including locked ones.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All admins registered for this application, including locked ones.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetAdminsResponse.class)) })
    })
    public ResponseEntity<GetAdminsResponse> getAdmins() {
        GetAdminsResponse response = authService.getAdmins();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/admin/lockPlayer")
    @Operation(summary = "Locks the specified player, preventing them from logging in.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player locked.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LockPlayerResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error " + ApiErrors.PLAYER_NOT_FOUND_CODE + ": " + ApiErrors.PLAYER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<LockPlayerResponse> lockPlayer(@RequestBody LockPlayerRequest request) throws ApiException {
        LockPlayerResponse response = authService.lockPlayer(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/admin/unlockPlayer")
    @Operation(summary = "Unlocks the specified player, allowing them to log in.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Player unlocked.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UnlockPlayerResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error " + ApiErrors.PLAYER_NOT_FOUND_CODE + ": " + ApiErrors.PLAYER_NOT_FOUND_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<UnlockPlayerResponse> unlockPlayer(@RequestBody UnlockPlayerRequest request) throws ApiException {
        UnlockPlayerResponse response = authService.unlockPlayer(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/login")
    @Operation(summary = "Verifies and logs in the specified player.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful.",
                    content = { @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)) }),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Error " + ApiErrors.INVALID_CREDENTIALS_CODE + ": " + ApiErrors.INVALID_CREDENTIALS_MESSAGE + "<br />" +
                            "Error " + ApiErrors.UNKNOWN_AUTH_PROVIDER_CODE + ": " + ApiErrors.UNKNOWN_AUTH_PROVIDER_MESSAGE + "<br />" +
                            "Error " + ApiErrors.INVALID_ROLE_CODE + ": " + ApiErrors.INVALID_ROLE_MESSAGE,
                    content = { @Content })
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws ApiException {
        LoginResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
