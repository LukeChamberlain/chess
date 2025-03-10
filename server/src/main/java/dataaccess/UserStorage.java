package dataaccess;

import java.util.List;

public interface UserStorage {
    boolean addUser(String username, String password, String email) throws DataAccessException;
    String getPassword(String username) throws DataAccessException;
    List<server.UserMemoryStorage.User> getAllUsers() throws DataAccessException;
    void clearAllUsers() throws DataAccessException;
    String getUsernameFromToken(String token) throws DataAccessException;
    void addToken(String token, String username) throws DataAccessException;
}
