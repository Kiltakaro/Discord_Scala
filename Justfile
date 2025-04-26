default:
    just start-all

start-all:
    just kafka
    just frontend
    just database
    just backend
    just kafka-consumer
    just database-client

start:
    just kafka
    just frontend
    just database
    just backend

kafka:
    gnome-terminal -- bash -c 'kafka_2.13-3.9.0/bin/kafka-storage.sh format --config kafka_2.13-3.9.0/config/kraft/server.properties --cluster-id $(kafka_2.13-3.9.0/bin/kafka-storage.sh random-uuid); kafka_2.13-3.9.0/bin/kafka-server-start.sh kafka_2.13-3.9.0/config/kraft/server.properties; exec bash'

kafka-consumer:
    gnome-terminal -- bash -c 'kafka_2.13-3.9.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning; exec bash'

frontend:
    gnome-terminal -- bash -c 'cd frontend; npm run dev; exec bash'

database:
    gnome-terminal -- bash -c 'cd database; clickhouse-server; exec bash'

database-client:
    gnome-terminal -- bash -c 'cd database; clickhouse-client; exec bash'

backend:
    gnome-terminal -- bash -c 'cd backend; sbt run; exec bash'


