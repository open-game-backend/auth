package de.opengamebackend.auth.model.responses;

public class RegisterResponse {
    private String playerId;

    public RegisterResponse(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
