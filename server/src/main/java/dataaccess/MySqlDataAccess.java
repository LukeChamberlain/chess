package dataaccess;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;
import server.GameMemoryStorage;

public class MySqlDataAccess implements DataAccess{

    @Override
    public void clearAllGames() throws DataAccessException {
        String sql = "DELETE FROM game";
        try {
            executeUpdate(sql);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error clearing all games: " + e.getMessage());
        }
    }

    @Override
    public void addGame(String gameId, String gameName) throws DataAccessException {
        String sql = "INSERT INTO game (id, name) VALUES (?, ?)";
        try {
            executeUpdate(sql, gameId, gameName);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Duplicate entry"))
            throw e;
        }
    }

    @Override
    public void addToken(String token, String username) throws DataAccessException {
        String sql = "INSERT INTO auth_token (token, username) VALUES (?, ?)";
        try {
            executeUpdate(sql, token, username);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error adding token: " + e.getMessage());
        }
    }

    @Override
    public String getUsernameFromToken(String token) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT username FROM auth_token WHERE token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? rs.getString("username") : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving username from token: " + e.getMessage());
        }
    }

    @Override
    public List<server.UserMemoryStorage.User> getAllUsers() throws DataAccessException {
        List<server.UserMemoryStorage.User> users = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT username, password, email FROM user";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    users.add(new server.UserMemoryStorage.User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to retrieve users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public void clearAllUsers() throws DataAccessException {
        String sql = "DELETE FROM user";
        try {
            executeUpdate(sql);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error clearing all users: " + e.getMessage());
        }
    }

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public GameMemoryStorage.Game getGame(String gameId) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT id, name, white_username, black_username FROM game WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, gameId);
                ResultSet rs = stmt.executeQuery();
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
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }

    @Override
    public List<GameMemoryStorage.Game> getAllGames() throws DataAccessException {
        List<GameMemoryStorage.Game> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT id, name, white_username, black_username FROM game";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    games.add(new GameMemoryStorage.Game(
                        rs.getString("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("name")
                    ));
                }
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving games: " + e.getMessage());
        }
    }
    @Override
    public String getPassword(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT password FROM user WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? rs.getString("password") : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving password: " + e.getMessage());
        }
    }

    @Override
    public boolean addUser(String username, String password, String email) throws DataAccessException {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try {
            executeUpdate(sql, username, password, email);
            return true;
        } catch (DataAccessException e) {
            if (e.getMessage().contains("Duplicate entry")) return false;
            throw e;
        }
    }
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        ps.setNull(i + 1, NULL);
                    } else if (param instanceof String) {
                        ps.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        ps.setInt(i + 1, (Integer) param);
                    }
                }
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database update failed: " + e.getMessage());
        }
    }

    private final String[] createStatements = {
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
        """,
        """
        CREATE TABLE IF NOT EXISTS game (
            id VARCHAR(36) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            white_username VARCHAR(255),
            black_username VARCHAR(255)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Database configuration failed: " + ex.getMessage());
        }
    }
}