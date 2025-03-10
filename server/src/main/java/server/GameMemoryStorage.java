package server;

import java.util.*;

import dataaccess.GameStorage;

public class GameMemoryStorage implements GameStorage {
    private final Map<String, Game> games = new HashMap<>();

    @Override
    public void clearAllGames() {
        games.clear();
    }
    @Override
    public void addGame(String gameID, String gameName) {
        games.put(gameID, new Game(gameID, null, null, gameName));
    }
    @Override
    public List<Game> getAllGames() {
        return new ArrayList<>(games.values());
    }
    @Override
    public Game getGame(String gameID){
        return games.get(gameID);
    }
    public static class Game{
        public String gameID;
        public String gameName;
        public String whiteUsername;
        public String blackUsername;

        Game(String gameID, String gameName) {
            this.gameID = gameID;
            this.gameName = gameName;
            this.whiteUsername = null;
            this.blackUsername = null;
        }

        Game(String gameID, String whiteUsername, String blackUsername, String gameName) {
            this.gameID = gameID;
            this.whiteUsername = whiteUsername;
            this.blackUsername = blackUsername;
            this.gameName = gameName;
        }

    }
}