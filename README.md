#Build

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