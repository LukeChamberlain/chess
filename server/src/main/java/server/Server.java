package server;

import spark.*;
import com.google.gson.Gson;
import dataaccess.UserStorage;
import dataaccess.GameStorage;
import java.util.HashSet;
import java.util.Set;

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

        Spark.before((request, response) -> { 
            String path = request.pathInfo();
            if ((!path.equals("/user")) && (!path.equals("/db"))) {
                String authToken = request.headers("Authorization");
                boolean authenticated = authToken != null && tokens.contains(authToken);
            if (!authenticated) { 
                Spark.halt(401, "You are not welcome here"); 
                } 
            }
        });

        UserStorage userStorage = new UserMemoryStorage();
        Spark.post("/user", (request, response) -> new UserReg(userStorage).register(request, response));
        
        GameStorage gameStorage = new GameMemoryStorage();
        Spark.delete("/db", (request, response) -> {
            new Clear(userStorage, gameStorage, tokens).clearAll();
            response.status(200);
            return "{\"message\":\"Database cleared\"}";
        });

        Spark.exception(Exception.class, (exception, req, res) -> {
            res.status(500);
            res.body("Internal Server Error: " + exception.getMessage());
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
