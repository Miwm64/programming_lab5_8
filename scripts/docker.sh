#!/bin/bash
docker build -t moviemanager-server .
docker stop moviemanager-server
docker rm moviemanager-server
docker run --network moviemanager-net -it -v ./docker:/app/config -v ./logs:/app/logs --env-file .env_docker_server  -p  7878:7878/udp --name moviemanager-server moviemanager-server