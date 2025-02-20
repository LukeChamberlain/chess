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
        games.put(gameID, new Game(gameID, gameName));
    }
    private static class Game{
        @SuppressWarnings("unused")
        String gameID;
        @SuppressWarnings("unused")
        String gameName;

        Game(String gameID, String gameName) {
            this.gameID = gameID;
            this.gameName = gameName;
        }

    }
}