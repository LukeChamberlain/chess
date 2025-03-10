package dataaccess;

import dataaccess.exception.ResponseException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import server.GameStorage;
import server.GameMemoryStorage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameStorage implements GameStorage {

    public MySQLGameStorage() throws ResponseException {
        configureDatabase();
    }

    @Override
    public void clearAllGames() throws ResponseException {
        executeUpdate("TRUNCATE game");
    }

    @Override
    public void addGame(String gameID, String gameName) throws ResponseException {
        var sql = "INSERT INTO game (id, name) VALUES (?, ?)";
        executeUpdate(sql, gameID, gameName);
    }

    @Override
    public List<GameMemoryStorage.Game> getAllGames() throws ResponseException {
        var games = new ArrayList<GameMemoryStorage.Game>();
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT id, name, white_username, black_username FROM game";
            try (var ps = conn.prepareStatement(sql)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        games.add(new GameMemoryStorage.Game(
                                rs.getString("id"),
                                rs.getString("white_username"),
                                rs.getString("black_username"),
                                rs.getString("name")
                        ));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Failed to retrieve games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public GameMemoryStorage.Game getGame(String gameID) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT id, name, white_username, black_username FROM game WHERE id=?";
            try (var ps = conn.prepareStatement(sql)) {
                ps.setString(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new GameMemoryStorage.Game(
                                rs.getString("id"),
                                rs.getString("white_username"),
                                rs.getString("black_username"),
                                rs.getString("name")
                        );
                    }
                    return null;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Failed to retrieve game: " + e.getMessage());
        }
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS  pet (
          `id` int NOT NULL AUTO_INCREMENT,
          `name` varchar(256) NOT NULL,
          `type` ENUM('CAT', 'DOG', 'FISH', 'FROG', 'ROCK') DEFAULT 'CAT',
          `json` TEXT DEFAULT NULL,
          PRIMARY KEY (`id`),
          INDEX(type),
          INDEX(name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
};


private void configureDatabase() throws ResponseException {
    DatabaseManager.createDatabase();
    try (var conn = DatabaseManager.getConnection()) {
        for (var statement : createStatements) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        }
    } catch (SQLException ex) {
        throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
    }
}

    private void executeBatch(String[] sqlStatements) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            for (var sql : sqlStatements) {
                try (var ps = conn.prepareStatement(sql)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Database configuration failed: " + e.getMessage());
        }
    }

    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}