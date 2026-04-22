#!/bin/bash
docker build -t moviemanager-server .
docker restart moviemanager-server