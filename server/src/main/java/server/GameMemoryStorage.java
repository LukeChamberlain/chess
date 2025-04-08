package server;

import java.util.*;
import dataaccess.DataAccessException;
import dataaccess.Game;
import dataaccess.GameStorage;

public class GameMemoryStorage implements GameStorage {
    private final Map<String, Game> games = new HashMap<>();
    private int nextID = 1;

    @Override
    public void clearAllGames() {
        games.clear();
        nextID = 1;
    }

    @Override
    public String addGame(String gameName) {
        int gameID = nextID++;
        games.put(String.valueOf(gameID), new Game(gameID, gameName));
        return String.valueOf(gameID);
    }

    @Override
    public List<Game> getAllGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public Game getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(int gameID, String username, String color) {
        Game game = games.get(gameID);
        if (game != null) {
            if (color.equalsIgnoreCase("WHITE")) {
                game.whiteUsername = username;
            } else {
                game.blackUsername = username;
            }
        }
    }
}