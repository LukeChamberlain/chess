package dataaccess;
import java.util.*;

import server.GameMemoryStorage;

public interface GameStorage {
    void updateGame(String gameID, String whiteUsername, String blackUsername) throws DataAccessException;
    void clearAllGames() throws DataAccessException;
    void addGame(String gameID, String gameName) throws DataAccessException;
    List<server.GameMemoryStorage.Game> getAllGames() throws DataAccessException;
    GameMemoryStorage.Game getGame(String gameID) throws DataAccessException;
}
