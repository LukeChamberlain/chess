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
        var port = server.run(8080);
        System.out.println("Server started on port " + port);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        try {
            DatabaseManager.createDatabase();
            UserStorage userStorage = new SQLUserStorage();
            List<String> tokensFromDB = userStorage.getAllTokens(); 
            Server.tokens.addAll(tokensFromDB);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database initialization failed: " + e.getMessage());
        }

        UserStorage userStorage = new SQLUserStorage();
        Spark.post("/user", (request, response) -> new UserReg(userStorage).register(request, response));
        Spark.post("/session", (request, response) -> new Login(userStorage, tokens).login(request, response));
        Spark.delete("/session", (request, response) -> new Logout(userStorage, tokens).logout(request, response));

        GameStorage gameStorage = new SQLGameStorage();
        Spark.post("/game", (request, response) -> new CreateGame(gameStorage, tokens).create(request, response));
        Spark.get("/game", (request, response) -> new ListGames(gameStorage, tokens).list(request, response));
        Spark.put("/game", (request, response) -> new JoinGame(gameStorage, tokens, userStorage).join(request, response));
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
