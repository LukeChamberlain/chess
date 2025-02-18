package server;

import com.google.gson.Gson;
import spark.*;
import java.util.*;

public class UserReg implements UserService {
    public static Gson gson = new Gson();
    public static Map<String, String> users = new HashMap<>();
    public static Set<String> validTokens = new HashSet<>();
    
    @Override
        public String register(Request request, Response response) {
            try{
                User user = new Gson().fromJson(request.body(), User.class);

                if (user.username == null || user.password == null || user.email == null || user.username.isEmpty() || user.password.isEmpty() || user.email.isEmpty()) {
                    response.status(400);
                    return gson.toJson(Map.of("message", "Error: bad request"));
                }
                users.put(user.username, user.password);
                String authToken = generateToken();
                validTokens.add(authToken);

                response.status(200);
                response.type("application/json");
                return gson.toJson(new AuthResponse(user.username,user.password, user.email, authToken));
            } catch (Exception e) {
                response.status(500);
                return gson.toJson(Map.of("message", "Error: Invalid user data"));
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
            String username;
            String password;
            String authToken;
            String email;
            AuthResponse(String username, String password, String email, String authToken) {
                this.username = username;
                this.password = password;
                this.email = email;
                this.authToken = authToken;
            }
        }
    }

