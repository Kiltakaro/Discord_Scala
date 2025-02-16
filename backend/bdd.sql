CREATE TABLE IF NOT EXISTS User (
    user_id UUID DEFAULT generateUUIDv4(),
    username String,
    password String,
    is_admin UInt8 DEFAULT 0,
    creation_date DateTime DEFAULT now()
) ENGINE = MergeTree
ORDER BY user_id;

-- Il va nous falloir une friend list


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
    channel_description String, -- en y repensant ça à l'air overkill une desc pour un channel
    guild_id UUID,
    is_private UInt8 DEFAULT 0 -- Jsuis pas sur de ce truc vu qu'on a DM_CHANNEL en dessous
) ENGINE = MergeTree
ORDER BY channel_id;

-- jpropose Guild_channel relation avec guild
-- et dm_channel, relation entre 2 users

CREATE TABLE IF NOT EXISTS DM_Channel (
    dm_channel_id UUID DEFAULT generateUUIDv4()
) ENGINE = MergeTree
ORDER BY dm_channel_id;

CREATE TABLE IF NOT EXISTS DM_Message (
    dm_message_id UUID DEFAULT generateUUIDv4(),
    dm_channel_id UUID,
    sender_id UUID,
    content String,
    sent_at DateTime64 DEFAULT now()
) ENGINE = MergeTree
ORDER BY sent_at;


CREATE TABLE IF NOT EXISTS Message (
    message_id UUID DEFAULT generateUUIDv4(),
    channel_id UUID,
    sender_id UUID,
    content String,
    sent_at DateTime64 DEFAULT now()
) ENGINE = MergeTree
ORDER BY sent_at;


CREATE TABLE IF NOT EXISTS Role (
    role_id UUID DEFAULT generateUUIDv4(),
    guild_id UUID,
    role_name String
) ENGINE = MergeTree
ORDER BY role_id;

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

CREATE TABLE IF NOT EXISTS User_Role (
    user_id UUID,
    role_id UUID
) ENGINE MergeTree
ORDER BY user_id;

CREATE TABLE IF NOT EXISTS User_DM_Channel (
    user_id UUID,
    dm_channel_id UUID
) ENGINE MergeTree
ORDER BY user_id;
