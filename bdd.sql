CREATE TABLE IF NOT EXISTS User (
    user_id UUID DEFAULT generateUUIDv4(),
    username String,
    password String,
    is_admin UInt8 DEFAULT 0,
    creation_date DateTime DEFAULT now(),
    roles Array(UUID) DEFAULT [],
    guilds Array(UUID) DEFAULT []
) ENGINE = MergeTree
ORDER BY user_id;


CREATE TABLE IF NOT EXISTS Guild (
    guild_id UUID DEFAULT generateUUIDv4(),
    guild_name String,
    guild_description String,
    owner_id UUID,
    creation_date DateTime,
    members Array(UUID),
    channels Array(UUID),
    roles Array(UUID) DEFAULT [],
    banned_users Array(UUID) DEFAULT []
) ENGINE = MergeTree
ORDER BY guild_id;


CREATE TABLE IF NOT EXISTS Channel (
    channel_id UUID DEFAULT generateUUIDv4(),
    channel_name String,
    channel_description String,
    guild_id UUID,
    is_private UInt8 DEFAULT 0,
    roles_allowed Array(UUID) DEFAULT [],
    members_allowed Array(UUID) DEFAULT [],
    messages Array(UUID) DEFAULT []
) ENGINE = MergeTree
ORDER BY channel_id;


CREATE TABLE IF NOT EXISTS DM_Channel (
    dm_channel_id UUID DEFAULT generateUUIDv4(),
    members Array(UUID),
    messages Array(UUID) DEFAULT []
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
    content String
    sent_at DateTime64 DEFAULT now(),
) ENGINE = MergeTree
ORDER BY sent_at;


CREATE TABLE IF NOT EXISTS Role (
    role_id UUID DEFAULT generateUUIDv4(),
    role_name String
) ENGINE = MergeTree
ORDER BY role_id;