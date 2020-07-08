package de.opengamebackend.auth.model.responses;

public class AuthResponse {
    private String playerId;

    public AuthResponse(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
