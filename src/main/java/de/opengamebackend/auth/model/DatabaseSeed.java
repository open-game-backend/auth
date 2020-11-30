package de.opengamebackend.auth.model;

import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

@Component
public class DatabaseSeed {
    @Autowired
    RoleRepository roleRepository;

    @PostConstruct
    public void postConstruct() {
        // Seed roles.
        getOrCreateRole(Role.ADMIN);
        getOrCreateRole(Role.USER);
    }

    @Transactional
    private Role getOrCreateRole(String name) {
        Role role = roleRepository.findByName(name);

        if (role == null) {
            role = new Role(name);
            roleRepository.save(role);
        }

        return role;
    }
}
