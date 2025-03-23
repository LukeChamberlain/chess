package dataaccess;
import java.util.*;


public interface GameStorage {
    String addGame(String gameName) throws DataAccessException;
    void clearAllGames() throws DataAccessException;
    List<Game> getAllGames() throws DataAccessException;
    Game getGame(String gameID) throws DataAccessException;
    void updateGame(String gameID, String whiteUsername, String blackUsername) throws DataAccessException;
}
