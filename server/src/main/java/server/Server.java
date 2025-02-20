package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.*;
import java.util.*;

public class Server {
    public static Set<String> tokens = new HashSet<>();
    public static Gson gson = new Gson();
    public static void main(String[] args) {
        Server server = new Server();
        var port = server.run(4567);
        System.out.println("Server started on port " + port);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        Spark.before((request, response) -> { 
            String path = request.pathInfo();
            if (!path.equals("/user") && !path.equals("/session") && !path.equals("/db")) {
                String authToken = request.headers("Authorization");
                boolean authenticated = authToken != null && tokens.contains(authToken);
                if (!authenticated) { 
                    response.status(401);
                    response.type("application/json");
                    response.body(gson.toJson(Map.of("message", "Error: unauthorized")));
                    Spark.halt();
                }
            }
        });

        UserStorage userStorage = new UserMemoryStorage();
        Spark.post("/user", (request, response) -> new UserReg(userStorage).register(request, response));
        Spark.post("/session", (request, response) -> new Login(userStorage, tokens).login(request, response));

        GameStorage gameStorage = new GameMemoryStorage();
        Spark.delete("/db", (request, response) -> {
            new Clear(userStorage, gameStorage, tokens).clearAll();
            response.status(200);
            response.type("application/json");
            return gson.toJson(Map.of("message", "Database cleared"));
        });

        Spark.exception(Exception.class, (exception, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Internal Server Error", "description", exception.getMessage())));
        });

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
