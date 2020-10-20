package de.opengamebackend.auth.repositories;

import de.opengamebackend.auth.model.entities.Role;
import de.opengamebackend.auth.model.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RoleRepositoryTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void givenRole_whenFindByName_thenReturnRole() {
        // GIVEN
        Role role = new Role("testRole");
        entityManager.persist(role);
        entityManager.flush();

        // WHEN
        Role found = roleRepository.findByName(role.getName());

        // THEN
        assertThat(found.getName()).isEqualTo(role.getName());
    }
}
