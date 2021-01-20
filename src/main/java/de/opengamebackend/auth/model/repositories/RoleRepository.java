package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
}
