#!/bin/bash
docker build -t moviemanager-server .
docker stop moviemanager-server
docker rm moviemanager-server
docker run -it -v ./docker:/app/config -e XML_LOAD="config/collection.xml" -p  7878:7878/udp --name moviemanager-server moviemanager-server