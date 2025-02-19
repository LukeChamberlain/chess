package server;

import dataaccess.UserStorage;
import java.util.*;
import dataaccess.GameStorage;

public class Clear {
    private final UserStorage userStorage;
    private final GameStorage gameStorage;
    private final Set<String> authTokens;

    public Clear(UserStorage userStorage, GameStorage gameStorage, Set<String> authTokens) {
        this.userStorage = userStorage;
        this.gameStorage = gameStorage;
        this.authTokens = authTokens;
    }

    public void clearAll() {
        clearAllUsers();
        clearAllGames();
        clearAllAuthTokens();
    }

    public void clearAllUsers() {
        userStorage.clearAllUsers();
    }
    public void clearAllGames() {
        gameStorage.clearAllGames();
    }
    public void clearAllAuthTokens() {
        authTokens.clear();
    }
}
