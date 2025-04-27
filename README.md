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
6. Si ça a fonctionné, on fournit la base de données a clickhouse
7. Ouvrir une autre fenetre dans le dossier database
8. ```clickhouse client < bdd.sql```

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

## Utilisation de l'application

- L'utilisateur peut s'inscrire, faire une demande d'amis.  
- L'autre utilisateur peut accepter sa demande d'amis.  
- Les utilisateurs peuvent s'envoyer des messages en privé.  
- Les utilisateurs peuvent créer des serveurs et y inviter des gens.  
- Il est possible de créer des channels pour envoyer des messages dans un serveur. (autrement il sera impossible d'envoyer des messages)
- L'admin peut CLIC DROIT pour expluser / bannir des utilisateurs.  
- Les utilisateurs d'un serveur peuvent recevoir des roles qui changeant leurs droits.  


# Auteurs
Kat'Orz  
Kiltakaro  
Hugo7764  
Fripouney  
