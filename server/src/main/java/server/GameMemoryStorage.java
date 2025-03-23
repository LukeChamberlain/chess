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
    public String addGame(String gameName) throws DataAccessException {
        String gameID = String.valueOf(games.size() + 1);
        games.put(gameID, new Game(gameID, gameName));
        return gameID;
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
    public void updateGame(String gameID, String username, String color) throws DataAccessException {
        Game game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        if (color.equalsIgnoreCase("WHITE")) {
            game.whiteUsername = username;
        } else {
            game.blackUsername = username;
        }
    }
}