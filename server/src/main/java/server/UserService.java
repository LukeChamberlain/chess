package server;
import spark.*;

public interface UserService {
    String register(Request request, Response response);
}
