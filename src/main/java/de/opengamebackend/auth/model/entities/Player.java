package de.opengamebackend.auth.model.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Collection;

@Entity
public class Player {
    @Id
    private String playerId;

    private String nickname;

    @ManyToMany
    private Collection<Role> roles;

    public Player() {
    }

    public Player(String playerId, String nickname) {
        this.playerId = playerId;
        this.nickname = nickname;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }
}
