package de.opengamebackend.auth.controller;

import de.opengamebackend.auth.model.requests.LoginRequest;
import de.opengamebackend.auth.model.responses.LoginResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
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
