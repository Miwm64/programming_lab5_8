package ru.spb.miwm64.moviemanager.server;

import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.collection.CollectionManager;
import ru.spb.miwm64.moviemanager.common.entities.Coordinates;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.MovieGenre;
import ru.spb.miwm64.moviemanager.common.entities.MpaaRating;
import ru.spb.miwm64.moviemanager.common.io.XMLParser;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.BatchStreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.DbBatchCollectionManager;
import ru.spb.miwm64.moviemanager.server.collectionmanager.StreamCollectionManager;
import ru.spb.miwm64.moviemanager.server.db.SQLRepository;
import ru.spb.miwm64.moviemanager.server.io.DatabaseProvider;
import ru.spb.miwm64.moviemanager.server.net.UDPServer;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;

public class Main {
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        XMLParser xmlParser = new XMLParser();
        UDPServer udpServer;

        try {
            DataSource dataSource = createDataSource();
            DatabaseProvider databaseProvider = new DatabaseProvider(dataSource);
            SQLRepository sql = new SQLRepository(databaseProvider);
            databaseProvider.getConnection();
            DbBatchCollectionManager collectionManager = new DbBatchCollectionManager(sql);

            log.info("Application started");
            udpServer = new UDPServer(7878, collectionManager, xmlParser);
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