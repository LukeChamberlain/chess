package dataaccess;

public interface UserStorage {
    boolean addUser(String username, String password, String email);
    String getPassword(String username);
    void clearAllUsers();
    String getUsernameFromToken(String token);
    void addToken(String token, String username);
}
