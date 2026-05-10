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

Docker compose - build and run all
```
docker compose down
docker compose up -d --build
```

Docker compose restart server
```
docker compose up -d --build moviemanager-server
```

Postgres create tables
```
psql -U miwm64 -d postgres
```