package de.opengamebackend.auth.model.entities;

import javax.persistence.*;
import java.util.Collection;

@Entity
public class Role {
    public static final String USER = "ROLE_USER";
    public static final String ADMIN = "ROLE_ADMIN";

    @Id
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Collection<Player> players;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
