package ru.spb.miwm64.moviemanager.server.db;

import ru.spb.miwm64.moviemanager.common.entities.*;
import ru.spb.miwm64.moviemanager.common.net.VersionedObject;
import ru.spb.miwm64.moviemanager.server.io.DatabaseProvider;

import java.sql.*;
import java.time.ZoneId;
import java.util.*;

public class SQLRepository {
    private final DatabaseProvider db;

    public SQLRepository(DatabaseProvider db) {
        this.db = db;
    }

    public void insert(VersionedObject<Movie> vo) throws SQLException {
        Person operator = vo.data.getOperator();
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            Long personId = null;
            if (operator != null) {
                String sql = "INSERT INTO person (name, weight, hair_color, nationality) VALUES (?, ?, ?::color, ?::country) RETURNING id";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, operator.getName());
                    stmt.setFloat(2, operator.getWeight());
                    stmt.setString(3, operator.getHairColor().name());
                    stmt.setString(4, operator.getNationality().name());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) personId = rs.getLong(1);
                }
            }

            String sql = "INSERT INTO movie (version, coord_x, coord_y, name, creation_date, oscars_count, golden_palm_count, genre, mpaa_rating, operator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?::movie_genre, ?::mpaa_rating, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vo.version);
                stmt.setFloat(2, vo.data.getCoordinates().getX());
                stmt.setLong(3, vo.data.getCoordinates().getY());
                stmt.setString(4, vo.data.getName());
                stmt.setTimestamp(5, Timestamp.from(vo.data.getCreationDate().toInstant()));
                stmt.setInt(6, vo.data.getOscarsCount());
                stmt.setLong(7, vo.data.getGoldenPalmCount());
                stmt.setString(8, vo.data.getGenre() != null ? vo.data.getGenre().name() : null);
                stmt.setString(9, vo.data.getMpaaRating().name());
                stmt.setObject(10, personId);
                stmt.executeUpdate();
            }

            conn.commit();
        }
    }

    public void updateById(VersionedObject<Movie> vo, Person operator) throws SQLException {
        Long movieId = vo.data.getId();
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            // Get old operator
            Long oldPersonId = null;
            String selectSql = "SELECT operator_id FROM movie WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setLong(1, movieId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    long id = rs.getLong("operator_id");
                    if (!rs.wasNull()) oldPersonId = id;
                }
            }

            // Insert new operator FIRST
            Long newPersonId = null;
            if (operator != null) {
                String sql = "INSERT INTO person (name, weight, hair_color, nationality) VALUES (?, ?, ?::color, ?::country) RETURNING id";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, operator.getName());
                    stmt.setFloat(2, operator.getWeight());
                    stmt.setString(3, operator.getHairColor().name());
                    stmt.setString(4, operator.getNationality().name());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) newPersonId = rs.getLong(1);
                }
            }

            // Update movie
            String sql = "UPDATE movie SET version = ?, coord_x = ?, coord_y = ?, name = ?, creation_date = ?, oscars_count = ?, golden_palm_count = ?, genre = ?::movie_genre, mpaa_rating = ?::mpaa_rating, operator_id = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vo.version);
                stmt.setFloat(2, vo.data.getCoordinates().getX());
                stmt.setLong(3, vo.data.getCoordinates().getY());
                stmt.setString(4, vo.data.getName());
                stmt.setTimestamp(5, Timestamp.from(vo.data.getCreationDate().toInstant()));
                stmt.setInt(6, vo.data.getOscarsCount());
                stmt.setLong(7, vo.data.getGoldenPalmCount());
                stmt.setString(8, vo.data.getGenre() != null ? vo.data.getGenre().name() : null);
                stmt.setString(9, vo.data.getMpaaRating().name());
                stmt.setObject(10, newPersonId);
                stmt.setLong(11, movieId);
                stmt.executeUpdate();
            }

            // ONLY delete old operator AFTER successful update
            if (oldPersonId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM person WHERE id = ?")) {
                    stmt.setLong(1, oldPersonId);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        }
    }

    public boolean deleteById(Long id) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);

            // Get operator
            Long personId = null;
            String selectSql = "SELECT operator_id FROM movie WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    long pid = rs.getLong("operator_id");
                    if (!rs.wasNull()) personId = pid;
                }
            }

            // Delete movie
            boolean deleted = false;
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM movie WHERE id = ?")) {
                stmt.setLong(1, id);
                deleted = stmt.executeUpdate() == 1;
            }

            // Delete operator
            if (deleted && personId != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM person WHERE id = ?")) {
                    stmt.setLong(1, personId);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return deleted;
        }
    }

    public VersionedObject<Movie> findById(Long id) throws SQLException {
        String sql = "SELECT m.*, p.name as p_name, p.weight as p_weight, p.hair_color as p_hair, p.nationality as p_nat FROM movie m LEFT JOIN person p ON m.operator_id = p.id WHERE m.id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapMovie(rs);
            }
            return null;
        }
    }

    public List<VersionedObject<Movie>> findAllMovies() throws SQLException {
        String sql = "SELECT m.*, p.name as p_name, p.weight as p_weight, p.hair_color as p_hair, p.nationality as p_nat FROM movie m LEFT JOIN person p ON m.operator_id = p.id ORDER BY m.name, m.id";
        List<VersionedObject<Movie>> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapMovie(rs));
            }
        }
        return list;
    }

    public void clearAll() throws SQLException {
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM movie")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM person")) {
                stmt.executeUpdate();
            }
        }
    }

    private VersionedObject<Movie> mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getLong("id"));
        m.setCoordinates(new Coordinates(rs.getFloat("coord_x"), rs.getLong("coord_y")));
        m.setName(rs.getString("name"));
        m.setCreationDate(rs.getTimestamp("creation_date").toInstant().atZone(ZoneId.systemDefault()));
        m.setOscarsCount(rs.getInt("oscars_count"));
        m.setGoldenPalmCount(rs.getLong("golden_palm_count"));
        String genre = rs.getString("genre");
        m.setGenre(genre != null ? MovieGenre.valueOf(genre) : null);
        m.setMpaaRating(MpaaRating.valueOf(rs.getString("mpaa_rating")));

        if (rs.getString("p_name") != null) {
            Person p = new Person();
            p.setName(rs.getString("p_name"));
            p.setWeight(rs.getFloat("p_weight"));
            p.setHairColor(Color.valueOf(rs.getString("p_hair")));
            p.setNationality(Country.valueOf(rs.getString("p_nat")));
            m.setOperator(p);
        }

        return new VersionedObject<>(rs.getInt("version"), m);
    }
}