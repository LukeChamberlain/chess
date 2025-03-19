package client;

import java.util.Arrays;

import com.google.gson.Gson;
import model.Pet;
import model.PetType;
import exception.ResponseException;
import client.websocket.NotificationHandler;
import server.ServerFacade;
import client.websocket.WebSocketFacade;
import model.AuthData;
import model.GameData;

public class ChessClient {
    private AuthData authData = null;
    private final ServerFacade server;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame();
                case "list" -> listGames(params);
                case "join" -> joinGame();
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            AuthData auth = server.register(params[0], params[1], params[2]);
            this.authData = auth;
            state = State.SIGNEDIN;
            return String.format("Logged in as %s.", auth.username());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            AuthData auth = server.login(params[0], params[1]);
            this.authData = auth;
            state = State.SIGNEDIN;
            return String.format("Logged in as %s.", auth.username());
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(authData.authToken());
        authData = null;
        state = State.SIGNEDOUT;
        return "Logged out successfully.";
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            server.createGame(authData.authToken(), gameName);
            return String.format("Game '%s' created.", gameName);
        }
        throw new ResponseException(400, "Expected: <gameName>");
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        currentGames = server.listGames(authData.authToken());
        var result = new StringBuilder();
        for (int i = 0; i < currentGames.size(); i++) {
            GameData game = currentGames.get(i);
            result.append(String.format("%d. %s (white: %s, black: %s)\n",
                    i + 1, game.gameName(), game.whiteUsername(), game.blackUsername()));
        }
        return result.toString();
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 2) {
            int gameIdx = Integer.parseInt(params[0]) - 1;
            String playerColor = params[1].toUpperCase();
            if (gameIdx < 0 || gameIdx >= currentGames.size()) {
                throw new ResponseException(400, "Invalid game number.");
            }
            GameData game = currentGames.get(gameIdx);
            server.joinGame(authData.authToken(), game.gameID(), playerColor);
            // Draw the board
            boolean isWhite = playerColor.equals("WHITE");
            return drawChessBoard(isWhite);
        }
        throw new ResponseException(400, "Expected: <gameNumber> <WHITE|BLACK>");
    }

    public String observeGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            int gameIdx = Integer.parseInt(params[0]) - 1;
            if (gameIdx < 0 || gameIdx >= currentGames.size()) {
                throw new ResponseException(400, "Invalid game number.");
            }
            GameData game = currentGames.get(gameIdx);
            server.joinGame(authData.authToken(), game.gameID(), null);
            // Draw the board from white's perspective
            return drawChessBoard(true);
        }
        throw new ResponseException(400, "Expected: <gameNumber>");
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    - help
                    """;
        }
        return """
                - create <gameName>
                - list
                - join <gameNumber> <WHITE|BLACK>
                - observe <gameNumber>
                - logout
                - quit
                - help
                """;
    }

    private String drawChessBoard(boolean isWhitePerspective) {
        // Implement board drawing logic using EscapeSequences
        // ... (see next step for implementation) ...
        return "Chess board displayed.";
    }
}