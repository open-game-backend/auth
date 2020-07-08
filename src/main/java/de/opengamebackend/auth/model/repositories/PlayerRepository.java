package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Player;
import org.springframework.data.repository.CrudRepository;

public interface PlayerRepository extends CrudRepository<Player, String> {
}
