CREATE TABLE IF NOT EXISTS User (
    user_id UUID DEFAULT generateUUIDv4(),
    username String,
    password String,
    is_admin UInt8 DEFAULT 0,
    creation_date DateTime,
    roles Array(UUID),
    guilds Array(UUID)
) ENGINE = MergeTree
ORDER BY user_id;


CREATE TABLE IF NOT EXISTS Guild (
    guild_id UUID DEFAULT generateUUIDv4(),
    guild_name String,
    guild_description String,
    owner_id UUID,
    creation_date DateTime,
    members Array(UUID),
    roles Array(UUID),
    channels Array(UUID),
    banned_users Array(UUID)
) ENGINE = MergeTree
ORDER BY guild_id;


CREATE TABLE IF NOT EXISTS Channel (
    channel_id UUID DEFAULT generateUUIDv4(),
    channel_name String,
    channel_description Nullable(String),
    guild_id UUID,
    is_private UInt8 DEFAULT 0,
    roles_allowed Array(UUID),
    members_allowed Array(UUID),
    messages Array(UUID)
) ENGINE = MergeTree
ORDER BY channel_id;


CREATE TABLE IF NOT EXISTS DM_Channel (
    dm_channel_id UUID DEFAULT generateUUIDv4(),
    members Array(UUID),
) ENGINE = MergeTree
ORDER BY dm_channel_id;


CREATE TABLE IF NOT EXISTS Message (
    message_id UUID DEFAULT generateUUIDv4(),
    channel_id UUID,
    sender_id UUID,
    sent_at DateTime64,
    content String
) ENGINE = MergeTree
ORDER BY sent_at;


CREATE TABLE IF NOT EXISTS Role (
    role_id UUID DEFAULT generateUUIDv4(),
    role_name String
) ENGINE = MergeTree
ORDER BY role_id;