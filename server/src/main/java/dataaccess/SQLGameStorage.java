package dataaccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLGameStorage implements GameStorage {

    public SQLGameStorage() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        }
    }

    @Override
    public void clearAllGames() throws DataAccessException {
        String query = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }

    // Change addGame to return the generated gameID
@Override
public String addGame(String gameName) throws DataAccessException {
    String query = "INSERT INTO games (gameName) VALUES (?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, gameName);
        stmt.executeUpdate();
        
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return String.valueOf(rs.getInt(1)); // Return generated gameID
            }
            throw new DataAccessException("Failed to retrieve generated game ID.");
        }
    } catch (SQLException e) {
        throw new DataAccessException("Error adding game: " + e.getMessage());
    }
}

    @Override
    public void updateGame(String gameID, String username, String color) throws DataAccessException {
        String column = color.equalsIgnoreCase("WHITE") ? "whiteUsername" : "blackUsername";
        String query = String.format("UPDATE games SET %s = ? WHERE gameID = ?", column);
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, Integer.parseInt(gameID)); // Convert gameID to int
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new DataAccessException("Invalid game ID: " + gameID);
        }
    }

    @Override
    public List<Game> getAllGames() throws DataAccessException {
        List<Game> games = new ArrayList<>();
        String query = "SELECT gameID, gameName, whiteUsername, blackUsername FROM games"; // Explicit columns
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Game game = new Game(String.valueOf(rs.getInt("gameID")), rs.getString("gameName"));
                game.whiteUsername = rs.getString("whiteUsername");
                game.blackUsername = rs.getString("blackUsername");
                games.add(game);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public Game getGame(String gameID) throws DataAccessException {
        String query = "SELECT * FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Game game = new Game(rs.getString("gameID"), rs.getString("gameName"));
                game.whiteUsername = rs.getString("whiteUsername");
                game.blackUsername = rs.getString("blackUsername");
                return game;
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }
}