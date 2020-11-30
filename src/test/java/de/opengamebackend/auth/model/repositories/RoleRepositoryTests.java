package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RoleRepositoryTests {
    private TestEntityManager entityManager;
    private RoleRepository roleRepository;

    @Autowired
    public RoleRepositoryTests(TestEntityManager entityManager, RoleRepository roleRepository) {
        this.entityManager = entityManager;
        this.roleRepository = roleRepository;
    }

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
