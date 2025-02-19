package server;

import java.util.*;

import dataaccess.GameStorage;

public class GameMemoryStorage implements GameStorage {
    private final Map<String, Game> games = new HashMap<>();

    @Override
    public void clearAllGames() {
        games.clear();
    }
    private static class Game{

    }
}