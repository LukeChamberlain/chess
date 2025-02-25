package dataaccess;

import java.util.List;

public interface UserStorage {
    boolean addUser(String username, String password, String email);
    String getPassword(String username);
    List<server.UserMemoryStorage.User> getAllUsers();
    void clearAllUsers();
    String getUsernameFromToken(String token);
    void addToken(String token, String username);
}
