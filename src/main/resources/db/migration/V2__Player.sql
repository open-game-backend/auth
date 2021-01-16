CREATE TABLE player (
    id VARCHAR(100) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    locked BIT(1) NOT NULL,

    PRIMARY KEY (id)
);

CREATE INDEX ix_player_provider_provider_user_id ON player (provider, provider_user_id);

CREATE TABLE player_roles (
    players_id VARCHAR(100) NOT NULL,
    roles_name VARCHAR(100) NOT NULL,

    PRIMARY KEY (players_id,roles_name),

    FOREIGN KEY (players_id) REFERENCES player(id),
    FOREIGN KEY (roles_name) REFERENCES role(name)
);
