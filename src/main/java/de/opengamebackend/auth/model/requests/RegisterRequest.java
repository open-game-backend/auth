package de.opengamebackend.auth.model.requests;

public class RegisterRequest {
    private String nickname;

    public RegisterRequest() {
    }

    public RegisterRequest(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
