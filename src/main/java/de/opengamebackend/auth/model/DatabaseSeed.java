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
        getOrCreateRole(AuthRole.ROLE_ADMIN);
        getOrCreateRole(AuthRole.ROLE_SERVER);
        getOrCreateRole(AuthRole.ROLE_USER);
    }

    @Transactional
    private Role getOrCreateRole(AuthRole authRole) {
        Role role = roleRepository.findById(authRole.name()).orElse(null);

        if (role == null) {
            role = new Role(authRole.name());
            roleRepository.save(role);
        }

        return role;
    }
}
