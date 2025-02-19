package server;

import com.google.gson.Gson;

import dataaccess.UserStorage;
import spark.*;
import java.util.*;

public class UserReg{
    public static Gson gson = new Gson();
    private final UserStorage userStorage;
    public static Set<String> validTokens = new HashSet<>();
    
    public UserReg(UserStorage storage) {
        this.userStorage = storage;
    }

        public String register(Request request, Response response) {
            try{
                User user = new Gson().fromJson(request.body(), User.class);

                if (user.username == null || user.password == null || user.email == null || user.username.isEmpty() || user.password.isEmpty() || user.email.isEmpty()) {
                    response.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }

                if (!userStorage.addUser(user.username, user.password, user.email)) {
                    response.status(403);
                    return gson.toJson(Map.of("message", "Error: already taken"));
                }

                String authToken = generateToken();
                Server.tokens.add(authToken);

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
            String email;
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

