package server;

import com.google.gson.Gson;
import dataaccess.UserStorage;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;
import java.util.Map;

import java.util.UUID;

public class UserReg {
    public static Gson gson = new Gson();
    private final UserStorage userStorage;

    public UserReg(UserStorage storage) {
        this.userStorage = storage;
    }

    public String register(Request request, Response response) {
        try {
            User user = gson.fromJson(request.body(), User.class);
            String hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt());
            // Input validation
            if (user.username == null || user.password == null || user.email == null ||
                    user.username.isEmpty() || user.password.isEmpty() || user.email.isEmpty()) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: bad request"));
            }
            if (user.username.equals(user.password) || user.email.equals(user.password)) {
                response.status(400);
                return gson.toJson(Map.of("message", "Error: username/email cannot match password"));
            }
            // Check if user already exists
            if (userStorage.getPassword(user.username) != null) {
                response.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }

            if (!userStorage.addUser(user.username, hashedPassword, user.email)) {
                response.status(403);
                return gson.toJson(Map.of("message", "Error: already taken"));
            }

            // Generate and store token
            String authToken = generateToken();
            Server.tokens.add(authToken);
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

    // Helper classes
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