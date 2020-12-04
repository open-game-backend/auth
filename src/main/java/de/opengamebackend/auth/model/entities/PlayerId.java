package de.opengamebackend.auth.model.entities;

import java.io.Serializable;
import java.util.Objects;

public class PlayerId implements Serializable {
    private String userId;
    private String provider;

    public PlayerId() {
    }

    public PlayerId(String userId, String provider) {
        this.userId = userId;
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerId playerId = (PlayerId) o;
        return userId.equals(playerId.userId) &&
                provider.equals(playerId.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, provider);
    }
}
