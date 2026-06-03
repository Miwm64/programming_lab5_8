#!/bin/bash
mvn clean install
mvn -pl client exec:java -DLOG_FILE=../logs/client.log -DLOG_STDOUT=false