package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Player;
import de.opengamebackend.auth.model.entities.Role;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PlayerRepositoryTests {
    private TestEntityManager entityManager;
    private PlayerRepository playerRepository;

    @Autowired
    public PlayerRepositoryTests(TestEntityManager entityManager, PlayerRepository playerRepository) {
        this.entityManager = entityManager;
        this.playerRepository = playerRepository;
    }

    @Test
    public void givenPlayerWithRole_whenFindByRoles_thenReturnPlayer() {
        // GIVEN
        Role role = new Role("testRole");
        entityManager.persist(role);

        Player player = new Player();
        player.setPlayerId("testPlayer");
        player.setRoles(Lists.list(role));
        entityManager.persist(player);

        entityManager.flush();

        // WHEN
        List<Player> playersWithRole = playerRepository.findByRoles(role);

        // THEN
        assertThat(playersWithRole).isNotNull();
        assertThat(playersWithRole).isNotEmpty();
        assertThat(playersWithRole.get(0)).isEqualTo(player);
    }
}
