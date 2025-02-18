package server;

import spark.*;

public class Server {

    public static void main(String[] args) {
        Server server = new Server();
        server.run(4567);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Spark.before((request, response) -> { 
        //     boolean authenticated = false; 
          
        //     // ... check if authenticated 
          
        //     if (!authenticated) { 
        //       Spark.halt(401, "You are not welcome here"); 
        //     } 
        //   });
          

        Spark.get("/", (request, response) -> {
            response.status(200);
            response.type("text/plain");
            response.header("CS240", "Awesome!");
            response.body("Hello, World!");
            return response.body();
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
