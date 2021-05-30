CREATE TABLE auth_player (
    id VARCHAR(100) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    locked BIT(1) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT auth_player_unique UNIQUE (provider, provider_user_id)
);

CREATE INDEX ix_auth_player_provider_provider_user_id ON auth_player (provider, provider_user_id);

CREATE TABLE auth_player_roles (
    players_id VARCHAR(100) NOT NULL,
    roles_name VARCHAR(100) NOT NULL,

    PRIMARY KEY (players_id,roles_name),

    FOREIGN KEY (players_id) REFERENCES auth_player(id),
    FOREIGN KEY (roles_name) REFERENCES auth_role(name)
);
