-- Pour s'en servir :
-- clickhouse-client --query="SOURCE insert_data.sql"

INSERT INTO User (user_id, username, password, email,  creation_date) VALUES
(generateUUIDv4(), 'user1', 'password1', 'user1@user1.com', now()),
(generateUUIDv4(), 'user2', 'password2', 'user2@user2.com', now()),
(generateUUIDv4(), 'user3', 'password3', 'user3@user3.com', now());

INSERT INTO Guild (guild_id, guild_name, guild_description, owner_id, creation_date) VALUES
(generateUUIDv4(), 'Guild1', 'Description1', (SELECT user_id FROM User WHERE email = 'user1@user1.com'), now()),
(generateUUIDv4(), 'Guild2', 'Description2', (SELECT user_id FROM User WHERE email = 'user2@user2.com'), now()),
(generateUUIDv4(), 'Guild3', 'Description3', (SELECT user_id FROM User WHERE email = 'user3@user3.com'), now());