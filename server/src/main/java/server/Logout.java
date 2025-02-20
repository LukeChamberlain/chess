package server;

import com.google.gson.Gson;

import dataaccess.UserStorage;
import spark.*;
import java.util.*;

public class Logout{
    public static Gson gson = new Gson();
    private final UserStorage userStorage;
    public static Set<String> validTokens;

    public Logout(UserStorage storage, Set<String> validTokens) {
        this.userStorage = storage;
        this.validTokens = validTokens;
    }

    public String logout(Request request, Response response) {
        try{
            String authToken = request.headers("Authorization");
            if (authToken == null || authToken.isEmpty() ||!validTokens.contains(authToken)) {
                response.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            validTokens.remove(authToken);

            response.status(200);
            return "";
        } catch (Exception e) {
            response.status(500);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
}


