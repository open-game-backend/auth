package de.opengamebackend.auth.model.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "auth_secretkey")
public class SecretKey {
    @Id
    private String secretKey;

    public SecretKey() {
    }

    public SecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
