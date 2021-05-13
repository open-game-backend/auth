package de.opengamebackend.auth.model.repositories;

import de.opengamebackend.auth.model.entities.SecretKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretKeyRepository extends CrudRepository<SecretKey, String> {
}
