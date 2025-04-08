package server;

import com.google.gson.Gson;
import dataaccess.*;
import spark.*;
import java.util.*;

public class JoinGame {
    public static Gson gson = new Gson();
    private final GameStorage gameStorage;
    private final Set<String> validTokens;
    private final UserStorage userStorage;

    public JoinGame(GameStorage storage, Set<String> validTokens, UserStorage userStorage) {
        this.gameStorage = storage;
        this.validTokens = validTokens;
        this.userStorage = userStorage;
    }

    public String join(Request request, Response response) {
        try {
            JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            
            // Validate authentication token
            String authToken = request.headers("Authorization");
            if (authToken == null || !validTokens.contains(authToken)) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            // Get username from token
            String username = userStorage.getUsernameFromToken(authToken);
            if (username == null) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            // Validate game exists
            int gameID = joinRequest.gameID;
            Game game = gameStorage.getGame(gameID);
            if (game == null) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: game not found"));
            }

            // Extract and validate playerColor
            String playerColor = joinRequest.playerColor;
            if (playerColor == null || playerColor.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            playerColor = playerColor.toUpperCase();
            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            // Check if color is already taken
            if ((playerColor.equals("WHITE") && game.whiteUsername != null) ||
                (playerColor.equals("BLACK") && game.blackUsername != null)) {
                response.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }

            // Update game with new player
            if (playerColor.equals("WHITE")) {
                game.whiteUsername = username;
            } else {
                game.blackUsername = username;
            }
            gameStorage.updateGame(gameID, username, playerColor);

            response.status(200);
            return gson.toJson(Map.of("gameID", gameID,"gameState", game.gameState));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private static class JoinGameRequest {
        String playerColor;
        int gameID;
    }
}