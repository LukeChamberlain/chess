package dataaccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import chess.ChessGame;

public class SQLGameStorage implements GameStorage {
    public void updateGame(int gameID, String username, String color) throws DataAccessException {
        String column = color.equalsIgnoreCase("WHITE") ? "whiteUsername" : "blackUsername";
        String query = "UPDATE games SET " + column + " = ? WHERE gameID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, gameID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
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

    public String addGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, gameState) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Initialize a valid ChessGame with default board
            ChessGame newGame = new ChessGame();
            newGame.getBoard().resetBoard();
            
            // Serialize to JSON
            String gameStateJson = new Gson().toJson(newGame);
            
            stmt.setString(1, gameName);
            stmt.setString(2, gameStateJson); // Store as JSON
            
            stmt.executeUpdate();
            
            // Retrieve and return the generated game ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
            throw new DataAccessException("Failed to create game");
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    
    public Game getGame(int gameID) throws DataAccessException {
        String query = "SELECT gameID, gameName, whiteUsername, blackUsername, gameState FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Game game = new Game(rs.getInt("gameID"), rs.getString("gameName"));
                game.whiteUsername = rs.getString("whiteUsername");
                game.blackUsername = rs.getString("blackUsername");
                
                String gameStateJson = rs.getString("gameState");
                if (gameStateJson != null && !gameStateJson.isEmpty()) {
                    game.gameState = new Gson().fromJson(gameStateJson, ChessGame.class);
                } else {
                    // Fallback to new game if state is invalid
                    game.gameState = new ChessGame();
                    game.gameState.getBoard().resetBoard();
                }
                return game;
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
    }

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
                Game game = new Game(rs.getInt("gameID"), rs.getString("gameName"));
                game.whiteUsername = rs.getString("whiteUsername");
                game.blackUsername = rs.getString("blackUsername");
                games.add(game);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving games: " + e.getMessage());
        }
        return games;
    }

    public void updateGameState(String gameID, ChessGame game) throws DataAccessException {
        String query = "UPDATE games SET gameState = ? WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, new Gson().toJson(game));
            stmt.setInt(2, Integer.parseInt(gameID));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game state: " + e.getMessage());
        }
    }
}