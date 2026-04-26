package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlCheckRepository extends BaseRepository {

    private static UrlCheck mapRow(ResultSet resultSet) throws SQLException {
        var urlCheck = new UrlCheck();
        urlCheck.setId(resultSet.getLong("id"));
        urlCheck.setUrlId(resultSet.getLong("url_id"));
        urlCheck.setStatusCode(resultSet.getInt("status_code"));
        urlCheck.setH1(resultSet.getString("h1"));
        urlCheck.setTitle(resultSet.getString("title"));
        urlCheck.setDescription(resultSet.getString("description"));
        var timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            urlCheck.setCreatedAt(timestamp.toLocalDateTime());
        }
        return urlCheck;
    }

    public static void save(UrlCheck urlCheck) throws SQLException {
        var sql = "INSERT INTO url_checks (url_id, status_code, h1, title, description, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, urlCheck.getUrlId());
            stmt.setInt(2, urlCheck.getStatusCode());
            stmt.setString(3, urlCheck.getH1());
            stmt.setString(4, urlCheck.getTitle());
            stmt.setString(5, urlCheck.getDescription());
            var createdAt = LocalDateTime.now();
            stmt.setTimestamp(6, Timestamp.valueOf(createdAt));

            stmt.executeUpdate();
            var generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
                urlCheck.setCreatedAt(createdAt);
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static List<UrlCheck> findByUrlId(Long urlId) throws SQLException {
        List<UrlCheck> checks = new ArrayList<>();
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            var resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                checks.add(mapRow(resultSet));
            }
            return checks;
        }
    }

    public static Optional<UrlCheck> findLastByUrlId(Long urlId) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC LIMIT 1";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            var resultSet = stmt.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.of(mapRow(resultSet));
        }
    }
}
