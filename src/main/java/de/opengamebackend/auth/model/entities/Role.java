package de.opengamebackend.auth.model.entities;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "auth_role")
public class Role {
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
