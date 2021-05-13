package de.opengamebackend.auth.model.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "auth_secretkey")
public class SecretKey {
    @Id
    private String key;

    public SecretKey() {
    }

    public SecretKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
