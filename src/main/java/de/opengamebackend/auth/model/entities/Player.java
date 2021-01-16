package de.opengamebackend.auth.model.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "auth_player")
public class Player {
    @Id
    private String id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerUserId;

    private boolean locked;

    @ManyToMany
    private Collection<Role> roles;

    public Player() {
        this.roles = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
