package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import server.GameMemoryStorage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameStorage implements GameStorage {

    public MySQLGameStorage() throws DataAccessException{
        configureDatabase();
    }

    @Override
    public void clearAllGames() throws DataAccessException {
        executeUpdate("TRUNCATE game");
    }

    @Override
    public void addGame(String gameID, String gameName) throws DataAccessException {
        var sql = "INSERT INTO game (id, name) VALUES (?, ?)";
        executeUpdate(sql, gameID, gameName);
    }

    @Override
    public List<GameMemoryStorage.Game> getAllGames() throws DataAccessException {
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
            throw new DataAccessException("Failed to retrieve games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public GameMemoryStorage.Game getGame(String gameID) throws DataAccessException {
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
            throw new DataAccessException("Failed to retrieve game: " + e.getMessage());
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


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private void executeUpdate(String sql, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Database update failed: " + e.getMessage());
        }
    }
}