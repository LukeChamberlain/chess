package server;

import com.google.gson.Gson;
import dataaccess.*;
import spark.*;
import java.util.*;

public class JoinGame{
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
        try{
            JoinGameRequest joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
            
            String authToken  = request.headers("Authorization");
            if (authToken == null || !validTokens.contains(authToken) || authToken.isEmpty()) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            GameMemoryStorage.Game game = gameStorage.getGame(joinGameRequest.gameID);
            if (game == null) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: game not found"));
            }

            if ("WHITE".equals(joinGameRequest.playerColor)) {
                if (game.whiteUsername != null) {
                    response.status(403);
                    return gson.toJson(Map.of("message", "Error: already taken"));
                }
                game.whiteUsername = userStorage.getUsernameFromToken(authToken);
            } else if ("BLACK".equals(joinGameRequest.playerColor)){
                if (game.blackUsername != null) {
                    response.status(403);
                    return gson.toJson(Map.of("message", "Error: already taken"));
                }
                game.blackUsername = userStorage.getUsernameFromToken(authToken);
            } else {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            response.status(200);
            response.type("application/json");
            return gson.toJson(Map.of("gameID", game.gameID));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
    private static class JoinGameRequest {
        String playerColor;
        String gameID;
    }
}

