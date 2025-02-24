package dataaccess;

public interface UserStorage {
    boolean addUser(String username, String password, String email);
    boolean userExists(String username);
    String getPassword(String username);
    void clearAllUsers();
    String getUsernameFromToken(String token);
    void addToken(String token, String username);
}
