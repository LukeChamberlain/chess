package client;

import java.util.*;
import java.net.*;
import java.io.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GameList;

public class ChessClient {
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    private AuthData authData;
    private List<GameData> currentGames;
    private final Gson gson = new Gson();
    

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
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
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }
    private String sendRequest(String method, String path, String body, String authToken) throws DataAccessException {
        try {
            URL url = URI.create(serverUrl + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoOutput(true);

            if (authToken != null) {
                conn.setRequestProperty("Authorization", authToken);
            }

            if (body != null) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes());
                }
            }

            int status = conn.getResponseCode();
            if (status >= 400) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String error = br.readLine();
                    throw new DataAccessException(error);
                }
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    public String register(String... params) throws DataAccessException {
        if (params.length < 3) throw new DataAccessException("Expected: <username> <password> <email>");
        
        Map<String, String> request = Map.of(
            "username", params[0],
            "password", params[1],
            "email", params[2]
        );
        
        String response = sendRequest("POST", "/user", gson.toJson(request), null);
        authData = gson.fromJson(response, AuthData.class);
        state = State.SIGNEDIN;
        return "Logged in as " + authData.username();
    }


    public String login(String... params) throws DataAccessException {
        if (params.length < 2) throw new DataAccessException("Expected: <username> <password>");
        Map<String, String> request = Map.of(
            "username", params[0],
            "password", params[1]
        );
        String response = sendRequest("POST", "/session", gson.toJson(request), null);
        authData = gson.fromJson(response, AuthData.class);
        state = State.SIGNEDIN;
        return "Logged in as " + authData.username();
    }

    public String logout() throws DataAccessException {
        assertSignedIn();
        sendRequest("DELETE", "/session", null, authData.authToken());
        authData = null;
        state = State.SIGNEDOUT;
        return "Logged out successfully";
    }

    public String createGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length < 1) throw new DataAccessException("Expected: <gameName>");
        
        Map<String, String> request = Map.of("gameName", String.join(" ", params));
        sendRequest("POST", "/game", gson.toJson(request), authData.authToken());
        return "Game '" + params[0] + "' created";
    }


    public String listGames() throws DataAccessException {
        assertSignedIn();
        String response = sendRequest("GET", "/game", null, authData.authToken());
        GameList games = gson.fromJson(response, GameList.class);
        currentGames = games.games();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentGames.size(); i++) {
            GameData game = currentGames.get(i);
            sb.append(String.format("%d. %s (White: %s, Black: %s)\n",
                i+1, game.gameName(), game.whiteUsername(), game.blackUsername()));
        }
        return sb.toString();
    }

    public String joinGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length < 2) throw new DataAccessException("Expected: <gameNumber> <WHITE|BLACK>");
        
        int gameIndex = Integer.parseInt(params[0]) - 1;
        if (gameIndex < 0 || gameIndex >= currentGames.size()) {
            throw new DataAccessException("Invalid game number");
        }
        Map<String, String> request = Map.of(
            "gameID", String.valueOf(currentGames.get(gameIndex).gameID()),
            "playerColor", params[1].toUpperCase()
        );
        sendRequest("PUT", "/game", gson.toJson(request), authData.authToken());
        return drawChessBoard(params[1].equalsIgnoreCase("WHITE"));
    }

    public String observeGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length < 1) throw new DataAccessException("Expected: <gameNumber>");
        
        int gameIndex = Integer.parseInt(params[0]) - 1;
        if (gameIndex < 0 || gameIndex >= currentGames.size()) {
            throw new DataAccessException("Invalid game number");
        }
        
        Map<String, String> request = Map.of(
            "gameID", String.valueOf(currentGames.get(gameIndex).gameID())
        );
        
        sendRequest("PUT", "/game", gson.toJson(request), authData.authToken());
        return drawChessBoard(true);
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

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException("You must be logged in");
        }
    }

    private String drawChessBoard(boolean isWhitePerspective) {
        // Implement board drawing logic using EscapeSequences
        // ... (see next step for implementation) ...
        return "Chess board displayed.";
    }
}