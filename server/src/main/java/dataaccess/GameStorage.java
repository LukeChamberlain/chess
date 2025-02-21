package dataaccess;
import java.util.*;

import server.GameMemoryStorage;

public interface GameStorage {
    void clearAllGames();
    void addGame(String gameID, String gameName);
    List<server.GameMemoryStorage.Game> getAllGames();
    GameMemoryStorage.Game getGame(String gameID);
}
