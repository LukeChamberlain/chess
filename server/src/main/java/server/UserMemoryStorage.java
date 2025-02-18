package server;
import java.util.*;

import dataaccess.UserStorage; 

public class UserMemoryStorage implements UserStorage {
    private final Map<String, String> users = new HashMap<>();
    
    public boolean addUser(String username, String password, String email) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, password);
        return true;
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    public String getPassword(String username) {
        return users.get(username);
    }
}