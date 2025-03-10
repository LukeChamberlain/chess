package server;

import com.google.gson.Gson;

import dataaccess.UserStorage;
import spark.*;
import java.util.*;

import org.mindrot.jbcrypt.BCrypt;

public class Login{
    public static Gson gson = new Gson();
    private final UserStorage userStorage;
    private final Set<String> validTokens;

    public Login(UserStorage storage, Set<String> validTokens) {
        this.userStorage = storage;
        this.validTokens = validTokens;
    }

    public String login(Request request, Response response) {
        try{
            User user = new Gson().fromJson(request.body(), User.class);

            if (user.username == null || user.password == null || user.username.isEmpty() || user.password.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
            String storedHash = userStorage.getPassword(user.username);
            if (storedHash == null) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }
            if (!BCrypt.checkpw(user.password, storedHash)) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            // Generate token and proceed
            String authToken = generateToken();
            validTokens.add(authToken);
            userStorage.addToken(authToken, user.username);

            response.status(200);
            response.type("application/json");
            return gson.toJson(new AuthResponse(user.username, authToken));
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
    private static class User {
        String username;
        String password;
    }
    private static class AuthResponse {
        @SuppressWarnings("unused")
        String username;
        @SuppressWarnings("unused")
        String authToken;
        AuthResponse(String username, String authToken) {
            this.username = username;
            this.authToken = authToken;
        }
    }
}


