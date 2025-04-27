# Discord_Scala
Discord en scala

## Prérequis, installations nécessaires

- Clickhouse
- Node
- Sbt
- Just (Pas nécessaire mais automatise le lancement des services)

## Installation nécessaire 

- Installer et initialliser clickhouse dans le dossier database

1. ```cd database```
2. ```curl https://clickhouse.com/ | sh```
3. ./clickhouse
4. ```install clickhouse```
5. ```clickhouse-server``` (Verifier si clickhouse fonctionne correctement)
6. CTRL + C    (stopper le processus, si ça a fonctionné, fournir la base de données a clickhouse)
7. ```clickhouse client < bdd.sql```
8. ```clickhouse-server```

Source : https://clickhouse.com/docs/install

## Lancement

Si "just" est installé si la machine : 

1. ```just start```

Ou alors à la main :

4 tabs / terminaux différents

1. ```cd backend && sbt run```
2. ```cd frontend && npm run dev```
3. ```cd database && clickhouse-server``` ou ```cd database && ./clickhouse-server```
4. ```
   kafka_2.13-3.9.0/bin/kafka-storage.sh format \
    --config kafka_2.13-3.9.0/config/kraft/server.properties \
    --cluster-id $(kafka_2.13-3.9.0/bin/kafka-storage.sh random-uuid)
   ```
5. ```kafka_2.13-3.9.0/bin/kafka-server-start.sh kafka_2.13-3.9.0/config/kraft/server.properties```


# Auteurs
Kat'Orz  
Kiltakaro  
Hugo7764  
Fripouney  
