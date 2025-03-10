package dataaccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLGameStorage implements GameStorage {
    private final String url = "jdbc:mysql://localhost:3306/chess";
    private final String user = "root";
    private final String password = "LukeMySQL25";

    public SQLGameStorage() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        }
    }

    @Override
    public void updateGame(String gameID, String whiteUsername, String blackUsername) throws DataAccessException {
        String query = "UPDATE games SET whiteUsername = ?, blackUsername = ? WHERE gameID = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, whiteUsername);
            stmt.setString(2, blackUsername);
            stmt.setString(3, gameID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clearAllGames() throws DataAccessException {
        String query = "DELETE FROM games";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }

    @Override
    public void addGame(String gameID, String gameName) throws DataAccessException {
        String query = "INSERT INTO games (gameID, gameName) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, gameID);
            stmt.setString(2, gameName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error adding game: " + e.getMessage());
        }
    }

    @Override
    public List<Game> getAllGames() throws DataAccessException {
        List<Game> games = new ArrayList<>();
        String query = "SELECT * FROM games";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Game game = new Game(rs.getString("gameID"), rs.getString("gameName"));
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
        try (Connection conn = DriverManager.getConnection(url, user, password);
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