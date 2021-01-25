package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends PagingAndSortingRepository<Player, String> {
    int countByRoles(Role role);
    List<Player> findByRoles(Role role);
    List<Player> findByRoles(Role role, Pageable pageable);
    Optional<Player> findByProviderAndProviderUserId(String provider, String providerUserId);
}
