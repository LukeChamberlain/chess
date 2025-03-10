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
            // Validate authentication token
            String authToken = request.headers("Authorization");
            if (authToken == null || !validTokens.contains(authToken)) {
                response.status(401);
                return "{\"message\": \"Error: unauthorized\"}";
            }

            // Get username from token
            String username = userStorage.getUsernameFromToken(authToken);
            if (username == null) {
                response.status(401);
                return "{\"message\": \"Error: unauthorized\"}";
            }

            JoinGameRequest joinRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            String gameID = joinRequest.gameID;
            String playerColor = joinRequest.playerColor;

            // Validate game exists
            Game game = gameStorage.getGame(gameID);
            if (game == null) {
                response.status(400);
                return "{\"message\": \"Error: bad request\"}";
            }

            // Handle player color assignment
            if (playerColor != null) {
                playerColor = playerColor.toUpperCase();
                if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                    response.status(400);
                    return "{\"message\": \"Error: bad request\"}";
                }

                // Check if color is already taken
                if ((playerColor.equals("WHITE") && game.whiteUsername != null) ||
                    (playerColor.equals("BLACK") && game.blackUsername != null)) {
                    response.status(403);
                    return "{\"message\": \"Error: already taken\"}";
                }

                // Update game with new player
                String newWhite = playerColor.equals("WHITE") ? username : game.whiteUsername;
                String newBlack = playerColor.equals("BLACK") ? username : game.blackUsername;
                gameStorage.updateGame(gameID, newWhite, newBlack);
            }

            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(500);
            return "{\"message\": \"Internal server error\"}"; // Generic message for security
        }
    }

    private static class JoinGameRequest {
        String playerColor;
        String gameID;
    }
}