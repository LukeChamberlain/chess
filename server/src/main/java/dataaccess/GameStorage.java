package dataaccess;
import java.util.*;


public interface GameStorage {
    void updateGame(String gameID, String whiteUsername, String blackUsername) throws DataAccessException;
    void clearAllGames() throws DataAccessException;
    void addGame(String gameID, String gameName) throws DataAccessException;
    List<Game> getAllGames() throws DataAccessException;
    Game getGame(String gameID) throws DataAccessException;

}
