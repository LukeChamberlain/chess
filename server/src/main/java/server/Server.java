package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.get("/", (request, response) -> {
            //show something
            return "get";
        });
        Spark.post("/", (request, response) -> {
            //create something
            return "post";
        });
        Spark.put("/", (request, response) -> {
            //Update something
            return "put";
        });
        Spark.delete("/", (request, response) -> {
            //Delete something
            return "delete";
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
