-- DROP DATABASE IF EXISTS scala_discord;
-- CREATE DATABASE scala_discord;
-- USE scala_discord;
-- si on fait ça faudra changer le DATABASE.scala pour remplacer default par scala_discord

CREATE TABLE IF NOT EXISTS User (
    user_id UUID DEFAULT generateUUIDv4(),
    username String,
    password String,
    email String,
    creation_date DateTime DEFAULT now()
) ENGINE = MergeTree
ORDER BY user_id;


-- j'hésite entre 'pending', 'accepted', 'refused'
-- car dans ma tete un booléen avec False => pas encore acceptée
-- True => acceptée
-- et si le mec clique sur le bouton refusé ça supprime la demande
-- mais ça voudrait dire qu'on a pas d'historique donc un mec peut spam request
CREATE TABLE IF NOT EXISTS Friends (
    friendship_id UUID,
    user_id1 UUID,
    user_id2 UUID,
    request_accepted UInt8 DEFAULT 0,
) ENGINE = MergeTree
ORDER BY user_id1;

CREATE TABLE IF NOT EXISTS Guild (
    guild_id UUID DEFAULT generateUUIDv4(),
    guild_name String,
    guild_description String,
    owner_id UUID,
    creation_date DateTime DEFAULT now()
) ENGINE = MergeTree
ORDER BY guild_id;

CREATE TABLE IF NOT EXISTS Channel (
    channel_id UUID DEFAULT generateUUIDv4(),
    channel_name String,
    friendship_id Nullable(UUID), -- Cas où c'est un channel DM
    guild_id Nullable(UUID) -- Cas où c'est un channel de guild
) ENGINE = MergeTree
ORDER BY channel_id;

CREATE TABLE IF NOT EXISTS Message (
    message_id UUID DEFAULT generateUUIDv4(),
    channel_id UUID,
    sender_id UUID,
    content String,
    sent_at DateTime64 DEFAULT now()
) ENGINE = MergeTree
ORDER BY sent_at;


CREATE TABLE IF NOT EXISTS User_Guild (
    user_id UUID,
    guild_id UUID,
    invite_accepted UInt8 DEFAULT 0
) ENGINE = MergeTree
ORDER BY user_id;

-- CREATE TABLE IF NOT EXISTS Guild_Invites (
--     invite_code String,
--     guild_id UUID,
--     creator_id UUID,
--     max_uses UInt8, -- 0 = Usage illimité
--     uses UInt8 DEFAULT 0, -- Compteur d'utilisation
--     expires_at DateTime DEFAULT (now() + INTERVAL 1 HOUR)
-- ) ENGINE = MergeTree
-- ORDER BY invite_code;

CREATE TABLE IF NOT EXISTS Guild_Ban (
    user_id UUID,
    guild_id UUID
) ENGINE = MergeTree
ORDER BY user_id;

CREATE TABLE IF NOT EXISTS User_DM_Channel (
    user_id UUID,
    dm_channel_id UUID
) ENGINE MergeTree
ORDER BY user_id;

CREATE TABLE IF NOT EXISTS Role (
    role_id UUID DEFAULT generateUUIDv4(),
    guild_id UUID,
    role_name String,
    priority UInt8,
    creation_date DateTime DEFAULT now()
) ENGINE = MergeTree
ORDER BY role_id;

CREATE TABLE IF NOT EXISTS User_Role (
    user_id UUID,
    guild_id UUID,
    role_id UUID
) ENGINE = MergeTree
ORDER BY (user_id, role_id);

CREATE TABLE IF NOT EXISTS Role_Permission (
    role_id UUID,
    permission_name String
) ENGINE = MergeTree
ORDER BY (role_id, permission_name);
