package de.opengamebackend.auth.model.requests;

public class LoginRequest {
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
