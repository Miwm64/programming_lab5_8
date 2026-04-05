#!/bin/bash
mvn clean install
java -DLOG_FILE=client.log -jar build/moviemanager-client.jar
