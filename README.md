Build

Build all projects:
```bash
mvn clean install
```

Run server
```bash
java -jar build/moviemanager-server.jar 
```

Run client
```bash
java -jar build/moviemanager-client.jar 
```

Exchange format(JSON-RPC):

```json
{
  "jsonrpc": "2.0",
  "method": "{method}",
  "id": 1,
  "params": {
    
  }
}
```


Logger
```
-DLOG_LEVEL=DEBUG 
-DLOG_TO_FILE=true 
-DLOG_DIR=/path/to/logs 
-DLOG_FILE=server.log
-DLOG_STDOUT=true
```

Docker
```
docker build -t moviemanager-server .
docker stop moviemanager-server
docker rm moviemanager-server
docker run -d -p 7878:7878/udp --name moviemanager-server moviemanager-server
docker exec -it moviemanager-server /bin/sh
```

DB
```
docker pull postgres:16
docker stop moviemanager-postgres
docker rm moviemanager-postgres
docker run --name moviemanager-postgres --env-file .env -p 5432:5432/tcp -d postgres:16
docker restart moviemanager-postgres
docker exec -it moviemanager-postgres /bin/bash

then run create.sql
```