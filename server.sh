#!/bin/bash
mvn clean install
java -DLOG_FILE=server.log -jar build/moviemanager-server.jar
