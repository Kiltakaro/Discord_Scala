# Discord_Scala
Discord en scala

## Prérequis, installations nécessaires

- Clickhouse
- Node
- Sbt
- Just (Pas nécessaire mais automatise le lancement des services)

## Installation nécessaire 

- Installer et initialliser clickhouse dans le dossier database

1. cd database
2. curl https://clickhouse.com/ | sh
3. ./clickhouse
4. install clickhouse
5. Verifier si clickhouse fonctionne correctement
6. clickhouse-server
7. CTRL + C    (stopper le processus, si ça a fonctionné, fournir la base de données a clickhouse)
8. clickhouse client < bdd.sql
9. clickhouse-server

Source : https://clickhouse.com/docs/install

## Lancement

3 tabs / terminaux différents

1. ```cd backend && sbt run```
2. ```cd frontend && npm run dev```
3. ```cd database && clickhouse-server``` ou ```cd database && ./clickhouse-server```


# Auteurs
Kat'Orz  
Kiltakaro  
Hugo7764  
Fripouney  
