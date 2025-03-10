package dataaccess;

import java.util.List;
import server.GameMemoryStorage;

public interface DataAccess extends GameStorage, UserStorage {
    
    public interface GameStorage {
        void clearAllGames() throws DataAccessException;
        void addGame(String gameID, String gameName) throws DataAccessException;
        List<server.GameMemoryStorage.Game> getAllGames() throws DataAccessException;
        GameMemoryStorage.Game getGame(String gameID) throws DataAccessException;
    }

    public interface UserStorage {
        boolean addUser(String username, String password, String email) throws DataAccessException;
        String getPassword(String username) throws DataAccessException;
        List<server.UserMemoryStorage.User> getAllUsers() throws DataAccessException;
        void clearAllUsers() throws DataAccessException;
        String getUsernameFromToken(String token) throws DataAccessException;
        void addToken(String token, String username) throws DataAccessException;
    }

}