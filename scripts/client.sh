#!/bin/bash
mvn clean install
java -DLOG_FILE=client.log -DLOG_STDOUT=false -jar build/moviemanager-client.jar
