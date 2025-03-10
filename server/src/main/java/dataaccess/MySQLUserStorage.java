package dataaccess;

import java.sql.*;
import java.util.*;

public class MySQLUserStorage implements UserStorage {

    public MySQLUserStorage() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public boolean addUser(String username, String password, String email) throws DataAccessException {
        var sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try {
            executeUpdate(sql, username, password, email);
            return true;
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Duplicate entry")) return false;
            throw e;
        }
    }

    @Override
    public String getPassword(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT password FROM user WHERE username = ?";
            try (var ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("password") : null;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(500, "Failed to get password: " + e.getMessage());
        }
    }

    @Override
    public List<server.UserMemoryStorage.User> getAllUsers() throws DataAccessException {
        var users = new ArrayList<server.UserMemoryStorage.User>();
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT username, password, email FROM user";
            try (var ps = conn.prepareStatement(sql)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        users.add(new server.UserMemoryStorage.User(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        ));
                    }
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(500, "Failed to retrieve users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public void clearAllUsers() throws DataAccessException {
        executeUpdate("TRUNCATE user");
        executeUpdate("TRUNCATE auth_token");
    }

    @Override
    public String getUsernameFromToken(String token) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = "SELECT username FROM auth_token WHERE token = ?";
            try (var ps = conn.prepareStatement(sql)) {
                ps.setString(1, token);
                try (var rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("username") : null;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Failed to get username from token: " + e.getMessage());
        }
    }

    @Override
    public void addToken(String token, String username) throws DataAccessException {
        executeUpdate("INSERT INTO auth_token (token, username) VALUES (?, ?)", token, username);
    }

    private void configureDatabase() throws DataAccessException {
        String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """,
                """
                CREATE TABLE IF NOT EXISTS auth_token (
                    token VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    FOREIGN KEY (username) REFERENCES user(username)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """
        };
        executeBatch(createStatements);
    }

    private void executeBatch(String[] sqlStatements) throws DataAccessException {
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

    private void executeUpdate(String sql, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new ResponseException(500, "Database update failed: " + e.getMessage());
        }
    }
}