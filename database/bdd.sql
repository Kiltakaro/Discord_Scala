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

CREATE TABLE IF NOT EXISTS Friends (
    friendship_id UUID,
    user_id1 UUID,
    user_id2 UUID
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
    guild_id UUID
) ENGINE = MergeTree
ORDER BY user_id;

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

-- Pour le moment on a pas besoin de ces deux dernières tables
-- Je les laisse au cas où on décide de gérer les rôles au final
-- ouais on garde pour le moment et jpense qu'on mettra des id normaux plutot que des uuid
CREATE TABLE IF NOT EXISTS Role (
    role_id UUID DEFAULT generateUUIDv4(),
    guild_id UUID,
    role_name String
) ENGINE = MergeTree
ORDER BY role_id;

CREATE TABLE IF NOT EXISTS User_Role (
    user_id UUID,
    role_id UUID
) ENGINE MergeTree
ORDER BY user_id;
