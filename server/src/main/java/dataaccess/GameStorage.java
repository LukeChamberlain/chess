package dataaccess;
import java.util.*;


public interface GameStorage {
    String addGame(String gameName) throws DataAccessException;
    void clearAllGames() throws DataAccessException;
    List<Game> getAllGames() throws DataAccessException;
    Game getGame(int gameID) throws DataAccessException;
    void updateGame(int gameID, String username, String color) throws DataAccessException;

}
