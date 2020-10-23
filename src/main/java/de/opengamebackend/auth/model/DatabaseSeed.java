package de.opengamebackend.auth.model;

import com.google.common.base.Strings;
import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.PlayerRepository;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;

@Component
public class DatabaseSeed {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    @Value("${de.opengamebackend.auth.adminId}")
    private String adminId;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    RoleRepository roleRepository;

    @PostConstruct
    public void postConstruct() {
        if (Strings.isNullOrEmpty(adminId)) {
            throw new IllegalArgumentException("Property 'de.opengamebackend.auth.adminId' not set.");
        }

        // Seed roles.
        Role adminRole = getOrCreateRole(ROLE_ADMIN);
        Role userRole = getOrCreateRole(ROLE_USER);

        // Seed users.
        getOrCreatePlayer(adminId, userRole, adminRole);
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

    private Player getOrCreatePlayer(String playerId, Role... roles) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);

        if (optionalPlayer.isPresent()) {
            return optionalPlayer.get();
        }

        Player player = new Player();
        player.setPlayerId(playerId);
        player.setRoles(Arrays.asList(roles));

        playerRepository.save(player);

        return player;
    }
}
