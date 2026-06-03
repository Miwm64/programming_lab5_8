package ru.spb.miwm64.moviemanager.server;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.server.collectionmanager.DbBatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.db.SQLRepository;
import ru.spb.miwm64.moviemanager.server.io.DatabaseProvider;
import ru.spb.miwm64.moviemanager.server.keycloak.KeycloakConfig;
import ru.spb.miwm64.moviemanager.server.keycloak.KeycloakService;
import ru.spb.miwm64.moviemanager.server.keycloak.UserAuthService;
import ru.spb.miwm64.moviemanager.server.net.UDPServer;

import javax.sql.DataSource;
import java.sql.*;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        UDPServer udpServer;

        try {
            XMLParser xmlParser = new XMLParser();
            DataSource dataSource = createDataSource();
            DatabaseProvider databaseProvider = new DatabaseProvider(dataSource);
            SQLRepository sql = new SQLRepository(databaseProvider);
            databaseProvider.getConnection();
            DbBatchCollectionManager collectionManager = new DbBatchCollectionManager(sql);
            log.info("Application started");

            KeycloakConfig keycloakConfig = new KeycloakConfig(
                    System.getenv("KEYCLOACK_BASE_URL"),
                    System.getenv("KEYCLOACK_ADMIN_USERNAME"),
                    System.getenv("KEYCLOACK_ADMIN_PASSWORD"),
                    System.getenv("KEYCLOACK_CLIENT_SECRET")
            );
            UserAuthService userAuthService = new KeycloakService(keycloakConfig);

            udpServer = new UDPServer(7878, collectionManager, xmlParser, sql, userAuthService);
            udpServer.run();
        }
        catch (IllegalStateException e) {
            System.out.println("Set XML_LOAD environment variable");
            log.error("Error: {}", e.getMessage());
        }
        catch (Exception e){
            log.error("Error: {}", e.getMessage());
        }
    }

    private static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(System.getenv("DB_URL"));
        ds.setUser(System.getenv("DB_USER"));
        ds.setPassword(System.getenv("DB_PASSWORD"));
        return ds;
    }
}