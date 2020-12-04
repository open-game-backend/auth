package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends CrudRepository<Player, String> {
    List<Player> findByRoles(Role role);
    Optional<Player> findByUserIdAndProvider(String userId, String provider);
}
