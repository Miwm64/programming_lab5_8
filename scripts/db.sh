#!/bin/bash
docker network create moviemanager-net
docker pull postgres:16
docker stop moviemanager-postgres
docker rm moviemanager-postgres
docker run --name moviemanager-postgres --network moviemanager-net --env-file .env_db -p 5433:5432/tcp -d postgres:16
