#!/bin/bash
docker pull postgres:16
docker stop moviemanager-postgres
docker rm moviemanager-postgres
docker run --name moviemanager-postgres --env-file .env -p 5433:5432/tcp -d postgres:16
