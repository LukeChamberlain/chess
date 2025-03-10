package server;

import java.util.Set;
import dataaccess.DataAccessException;
import dataaccess.GameStorage;
import dataaccess.UserStorage;
import spark.Request;
import spark.Response;

public class Clear {
    private final UserStorage userStorage;
    private final GameStorage gameStorage;
    private final Set<String> validTokens;

    public Clear(UserStorage userStorage, GameStorage gameStorage, Set<String> validTokens) {
        this.userStorage = userStorage;
        this.gameStorage = gameStorage;
        this.validTokens = validTokens;
    }

    public Object clearAll(Request request, Response response) throws DataAccessException {
        userStorage.clearAllUsers();
        gameStorage.clearAllGames();
        validTokens.clear();

        response.status(200);
        response.type("application/json");
        return "{}";
    }
}