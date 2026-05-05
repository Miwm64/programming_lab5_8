package ru.spb.miwm64.moviemanager.server.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.io.DatabaseProvider;

import java.sql.*;
import java.time.ZonedDateTime;

public class SQLRepository {
    private static final Logger log = LoggerFactory.getLogger(SQLRepository.class);
    private final DatabaseProvider db;

    // 🔹 Full SQL with ENUM casts and proper columns
    private static final String INSERT_MOVIE = """
        INSERT INTO movie (
            version, coord_x, coord_y, name, creation_date,
            oscars_count, golden_palm_count, genre, mpaa_rating, operator_id
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::movie_genre, ?::mpaa_rating, ?)
        RETURNING id
        """;

    public SQLRepository(DatabaseProvider db) {
        this.db = db;
    }

    public Long insert(VersionedObject<Movie> vm, Long operatorId) throws SQLException {
        Movie m = vm.data;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_MOVIE)) {

            int i = 1;
            stmt.setInt(i++, vm.version);                              // 1: version
            stmt.setFloat(i++, m.getCoordinates().getX());             // 2: coord_x (REAL → float)
            stmt.setLong(i++, m.getCoordinates().getY());              // 3: coord_y
            stmt.setString(i++, m.getName());                          // 4: name
            stmt.setTimestamp(i++, Timestamp.from(
                    m.getCreationDate().toInstant()));                     // 5: creation_date
            stmt.setInt(i++, m.getOscarsCount());                      // 6: oscars_count
            stmt.setLong(i++, m.getGoldenPalmCount());                 // 7: golden_palm_count
            stmt.setString(i++, m.getGenre() != null ? m.getGenre().name() : null);  // 8: genre (ENUM, nullable)
            stmt.setString(i++, m.getMpaaRating().name());             // 9: mpaa_rating (ENUM, NOT NULL)
            stmt.setObject(i, null);                             // 10: operator_id (FK, nullable)

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        }
    }

    // 🔹 Add more methods: findById, update, delete, etc.
}