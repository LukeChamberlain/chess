package server;

import com.google.gson.Gson;

import dataaccess.GameStorage;
import spark.*;
import java.util.*;
import java.util.stream.*;

public class ListGames{
    public static Gson gson = new Gson();
    private final GameStorage gameStorage;
    public static Set<String> tokens;
    
    @SuppressWarnings("static-access")
    public ListGames(GameStorage storage, Set<String> tokens) {
        this.gameStorage = storage;
        this.tokens = tokens;
    }

        public String list(Request request, Response response) {
            try{
                String authToken = request.headers("Authorization");
                if (authToken == null || authToken.isEmpty() || !tokens.contains(authToken)) {
                    response.status(401);
                    return gson.toJson(Map.of("message", "Error: unauthorized"));
                }
                List<GameResponse> games = gameStorage.getAllGames().stream()
                    .map(game -> new GameResponse(game.gameID, game.whiteUsername, game.blackUsername, game.gameName))
                    .collect(Collectors.toList());

                response.status(200);
                response.type("application/json");
                return gson.toJson(Map.of("games", games));
            } catch (Exception e) {
                response.status(500);
                return gson.toJson(Map.of("message", e.getMessage()));
            }
        }
        private static class GameResponse{
            @SuppressWarnings("unused")
            String gameID;
            @SuppressWarnings("unused")
            String whiteUsername;
            @SuppressWarnings("unused")
            String blackUsername;
            @SuppressWarnings("unused")
            String gameName;
            GameResponse(String gameID, String whiteUsername, String blackUsername, String gameName){
                this.gameID = gameID;
                this.whiteUsername = whiteUsername;
                this.blackUsername = blackUsername;
                this.gameName = gameName;
            }
        }
    }

