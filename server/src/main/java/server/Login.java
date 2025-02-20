package server;

import com.google.gson.Gson;

import dataaccess.UserStorage;
import spark.*;
import java.util.*;

public class Login{
    public static Gson gson = new Gson();
    private final UserStorage userStorage;
    public static Set<String> validTokens;

    public Login(UserStorage storage, Set<String> validTokens) {
        this.userStorage = storage;
        Login.validTokens = validTokens;
    }

    public String login(Request request, Response response) {
        try{
            User user = new Gson().fromJson(request.body(), User.class);

            if (user.username == null || user.password == null || user.username.isEmpty() || user.password.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
            String password = userStorage.getPassword(user.username);
            if (password == null || !password.equals(user.password)) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            String authToken = generateToken();
            validTokens.add(authToken);

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


