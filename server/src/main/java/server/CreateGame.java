package server;

import com.google.gson.Gson;

import dataaccess.*;
import spark.*;
import java.util.*;

public class CreateGame{
    public static int gameIDCounter = 1;
    public static Gson gson = new Gson();
    private final GameStorage gameStorage;
    public static Set<String> validTokens;

    @SuppressWarnings("static-access")
    public CreateGame(GameStorage storage, Set<String> validTokens) {
        this.gameStorage = storage;
        this.validTokens = validTokens;
    }

    public String create(Request request, Response response) {
        try{
            String authToken  = request.headers("Authorization");
            if (authToken == null || !validTokens.contains(authToken) || authToken.isEmpty()) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }
            GameRequest gameRequest = gson.fromJson(request.body(), GameRequest.class);
            if (gameRequest.gameName == null || gameRequest.gameName.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }

            String gameID = generateToken();
            gameStorage.addGame(gameID, gameRequest.gameName);

            response.status(200);
            response.type("application/json");
            return gson.toJson(Map.of("gameID", Integer.parseInt(gameID)));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }

    public static String generateToken() {
       return String.valueOf(gameIDCounter++);
    }
    private static class GameRequest {
        String gameName;
    }
}

