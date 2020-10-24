package de.opengamebackend.auth.model.responses;

import de.opengamebackend.auth.model.entities.Role;

import java.util.ArrayList;
import java.util.Collection;

public class LoginResponse {
    private String playerId;
    private ArrayList<String> roles;

    public LoginResponse() {
    }

    public LoginResponse(String playerId, ArrayList<String> roles) {
        this.playerId = playerId;
        this.roles = roles;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<String> roles) {
        this.roles = roles;
    }
}
