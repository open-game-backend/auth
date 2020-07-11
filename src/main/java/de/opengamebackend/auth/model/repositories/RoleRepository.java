package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(String name);
}
