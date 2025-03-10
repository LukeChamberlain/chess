package server;

import java.util.*;
import dataaccess.DataAccessException;
import dataaccess.Game;
import dataaccess.GameStorage;

public class GameMemoryStorage implements GameStorage {
    private final Map<String, Game> games = new HashMap<>();

    @Override
    public void clearAllGames() {
        games.clear();
    }
    @Override
    public void addGame(String gameID, String gameName) throws DataAccessException {
        games.put(gameID, new Game(gameID, gameName));
    }
    @Override
    public List<Game> getAllGames() {
        return new ArrayList<>(games.values());
    }
    @Override
    public Game getGame(String gameID){
        return games.get(gameID);
    }
    @Override
    public void updateGame(String gameID, String whiteUsername, String blackUsername) {
        Game game = games.get(gameID);
        if (game != null) {
            game.whiteUsername = whiteUsername;
            game.blackUsername = blackUsername;
        }
    }
}