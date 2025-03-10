package dataaccess;

import com.google.gson.Gson;
import exception.ResponseException;
import model.Pet;
import model.PetType;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlDataAccess implements DataAccess, GameStorage, UserStorage {

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
        @Override
        public String getGame(String gameId) throws DataAccessException {
            // Implement the method to get a game by ID
            return null;
        }
    
        @Override
        public Collection<String> getAllGames() throws DataAccessException {
            // Implement the method to get all games
            return null;
        }
    
        @Override
        public String getPassword(String username) throws DataAccessException {
            // Implement the method to get a password by username
            return null;
        }
    
        @Override
        public void clearAllGames() throws DataAccessException {
            // Implement the method to clear all games
        }
    
        @Override
        public void addToken(String username, String token) throws DataAccessException {
            // Implement the method to add a token
        }
    
        @Override
        public void addGame(String gameId, String gameData) throws DataAccessException {
            // Implement the method to add a game
        }
    
        @Override
        public void addUser(String username, String password, String email) throws DataAccessException {
            // Implement the method to add a user
        }
    
        @Override
        public String getUsernameFromToken(String token) throws DataAccessException {
            // Implement the method to get a username from a token
            return null;
        }
    
        @Override
        public Collection<String> getAllUsers() throws DataAccessException {
            // Implement the method to get all users
            return null;
        }
    
        @Override
        public void clearAllUsers() throws DataAccessException {
            // Implement the method to clear all users
        }
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
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
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}