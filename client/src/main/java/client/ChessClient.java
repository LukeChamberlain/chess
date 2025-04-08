package client;


import java.util.*;
import java.util.stream.Collectors;

import java.net.*;
import java.io.*;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GameList;
import chess.ChessPosition;
import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;

public class ChessClient {
    private Integer currentGameID;
    private String playerColor;
    private String currentGameState;
    private String pendingAction;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    private AuthData authData;
    private List<GameData> currentGames;
    private GameData currentGame;
    private final Gson gson = new Gson();
    

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public enum State {
        SIGNEDOUT, SIGNEDIN, IN_GAME
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
    
            if (state == State.IN_GAME) {
                return handleGameCommand(cmd, params);
            } else {
                return handleNonGameCommand(cmd, params);
            }
        } catch (DataAccessException ex) {
            return ex.getMessage();
        }
    }
    
    private String handleNonGameCommand(String cmd, String[] params) throws DataAccessException {
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
    }
    
    private String handleGameCommand(String cmd, String[] params) throws DataAccessException {
        return switch (cmd) {
            case "help" -> gameHelp();
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove(params);
            case "resign" -> resign(params);
            case "highlight" -> highlightMoves(params);
            case "quit" -> "quit";
            default -> "Unknown command. Type 'help' for available commands.\n" + gameHelp();
        };
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
                String errorJson = br.readLine();
                @SuppressWarnings("unchecked")
                Map<String, String> error = gson.fromJson(errorJson, Map.class);
                throw new DataAccessException(error.get("message")); 
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
        if (params.length < 3){
            throw new DataAccessException("Expected: <username> <password> <email>");
        }
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
        if (params.length < 2) {
            throw new DataAccessException("Expected: <username> <password>");
        }
    
        try {
            Map<String, String> request = Map.of("username", params[0], "password", params[1]);
            String response = sendRequest("POST", "/session", gson.toJson(request), null);
            authData = gson.fromJson(response, AuthData.class);
            state = State.SIGNEDIN;
            return "Logged in as " + authData.username();
        } catch (DataAccessException ex) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> error = gson.fromJson(ex.getMessage(), Map.class);
                return error.get("message"); 
            } catch (Exception e) {
                return ex.getMessage(); 
            }
        }
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
        if (params.length < 1) {
            throw new DataAccessException("Expected: <gameName>");
        }
        
        String gameName = String.join(" ", params);
        Map<String, String> request = Map.of("gameName", gameName);
        
        // Use the HTTP API instead of direct database access
        String response = sendRequest("POST", "/game", gson.toJson(request), authData.authToken());
        
        // Parse the response to get the game ID
        @SuppressWarnings("unchecked")
        Map<String, String> result = gson.fromJson(response, Map.class);
        return "Game created with ID: " + result.get("gameID");
    }


    public String listGames() throws DataAccessException {
        assertSignedIn();
        String response = sendRequest("GET", "/game", null, authData.authToken());
        GameList games = gson.fromJson(response, GameList.class);
        currentGames = games.games() != null ? games.games() : new ArrayList<>();
    
        var result = new StringBuilder();
        for (int i = 0; i < currentGames.size(); i++) {
            GameData game = currentGames.get(i);
            String white = game.whiteUsername() != null ? game.whiteUsername() : "no player";
            String black = game.blackUsername() != null ? game.blackUsername() : "no player";
            result.append(String.format("%d. %s (white: %s, black: %s)\n",
                    i + 1, game.gameName(), white, black));
        }
        return result.toString();
    }

    public String joinGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length < 2) {
            throw new DataAccessException("Expected: <gameNumber> <WHITE|BLACK>");
        }
    
        String chosenColor = params[1].toUpperCase();
        if (!chosenColor.equals("WHITE") && !chosenColor.equals("BLACK")) {
            throw new DataAccessException("Invalid color. Use 'WHITE' or 'BLACK'.");
        }
    
        try {
            int gameIndex = Integer.parseInt(params[0]) - 1;
            if (gameIndex < 0 || gameIndex >= currentGames.size()) {
                throw new DataAccessException("Invalid game number");
            }
    
            int gameID = currentGames.get(gameIndex).gameID();
            Map<String, Object> request = new HashMap<>();
            request.put("gameID", gameID);
            request.put("playerColor", chosenColor);
    
            sendRequest("PUT", "/game", gson.toJson(request), authData.authToken());
    
            try {
                String gameResponse = sendRequest("GET", "/game/" + gameID, null, authData.authToken());
                System.out.println("🔍 Raw Game Response: " + gameResponse);
            
                // Verify if response starts with a JSON object
                if (!gameResponse.trim().startsWith("{")) {
                    throw new DataAccessException("Unexpected response: " + gameResponse);
                }
            
                currentGame = gson.fromJson(gameResponse, GameData.class);
                currentGameID = gameID;
                playerColor = chosenColor;
                ChessGame chessGame = currentGame.gameState();
                currentGameState = gson.toJson(chessGame);
                state = State.IN_GAME;
                return drawChessBoard(playerColor.equals("WHITE"), null);
            } catch (Exception e) {
                System.out.println("❌ Failed to parse or fetch game: " + e.getMessage());
                e.printStackTrace();
                throw new DataAccessException("Error joining game: " + e.getMessage());
            }
            
    
        } catch (NumberFormatException e) {
            throw new DataAccessException("Game number must be a valid integer (e.g., '1')");
        }
    }
    
    public String observeGame(String... params) throws DataAccessException {
        assertSignedIn();
        try {
            int gameIndex = Integer.parseInt(params[0]) - 1;
            if (gameIndex < 0 || gameIndex >= currentGames.size()) {
                throw new DataAccessException("Invalid game number");
            }
            int gameID = currentGames.get(gameIndex).gameID();
            String gameResponse = sendRequest("GET", "/game/" + gameID, null, authData.authToken());
                currentGame = gson.fromJson(gameResponse, GameData.class);
                currentGameID = gameID;
                playerColor = null;
                currentGameState = gson.toJson(currentGame.gameState());
                state = State.IN_GAME;
                return drawChessBoard(true, null);
            } catch (NumberFormatException e) {
                throw new DataAccessException("Game number must be a valid integer (e.g., '1')");
            }
        }

        public String help() {
            if (state == State.SIGNEDOUT) {
                return """
                    - register <USERNAME> <PASSWORD> <EMAIL>
                    - login <USERNAME> <PASSWORD>
                    - quit
                    - help
                    """;
            } else if (state == State.SIGNEDIN) {
                return """
                    - create <GAME_NAME>
                    - list
                    - join <GAME_NUMBER> <WHITE|BLACK>
                    - observe <GAME_NUMBER>
                    - logout
                    - quit
                    - help
                    """;
            } else { 
                return gameHelp();
            }
        }

        private String gameHelp() {
            return """
                Game Commands:
                - help: Show this help message
                - redraw: Redraw the chess board
                - leave: Leave the current game
                - move <FROM> <TO> [PROMOTION]: Make a move (e.g., move e2 e4 or move e7 e8 QUEEN)
                - resign: Forfeit the game
                - highlight <SQUARE>: Show legal moves (e.g., highlight e2)
                - quit: Exit the program
                """;
        }

    private String redrawBoard() {
        boolean isWhite = (playerColor == null) ? true : playerColor.equals("WHITE");
        return drawChessBoard(isWhite, null);
    }

    private String leaveGame() throws DataAccessException {
        sendRequest("POST", "/game/leave", "{\"gameID\":" + currentGameID + "}", authData.authToken());
        state = State.SIGNEDIN;
        currentGameID = null;
        playerColor = null;
        return "Left the game.";
    }

    private String makeMove(String... params) throws DataAccessException {
        if (params.length < 2) throw new DataAccessException("Expected: <from> <to> [promotion]");
        
        try {
            ChessPosition from = parsePosition(params[0]);
            ChessPosition to = parsePosition(params[1]);
            ChessPiece.PieceType promotion = parsePromotion(params.length > 2 ? params[2] : null);
    
            ChessBoard board = new ChessBoard();
            board.loadFEN(currentGameState);
            ChessPiece piece = board.getPiece(from);
    
            // Validate move
            Set<ChessMove> validMoves = new HashSet<>(piece.pieceMoves(board, from));
            boolean isValid = validMoves.stream()
                .anyMatch(m -> m.getEndPosition().equals(to) && 
                          (m.getPromotionPiece() == promotion));
    
            if (!isValid) return "Invalid move!";
    
            // Build move request
            Map<String, String> moveReq = new HashMap<>();
            moveReq.put("from", params[0]);
            moveReq.put("to", params[1]);
            moveReq.put("gameID", currentGameID.toString());
            if (promotion != null) {
                moveReq.put("promotion", promotion.name());
            }
    
            sendRequest("POST", "/game/move", gson.toJson(moveReq), authData.authToken());
            
            // Update game state
            String response = sendRequest("GET", "/game/" + currentGameID, null, authData.authToken());
            currentGameState = gson.toJson(gson.fromJson(response, GameData.class).gameState());
            return redrawBoard();
        } catch (IllegalArgumentException e) {
            return "Invalid move: " + e.getMessage();
        }
    }
    
    private ChessPiece.PieceType parsePromotion(String promo) {
        if (promo == null) return null;
        try {
            return ChessPiece.PieceType.valueOf(promo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid promotion piece. Use QUEEN/ROOK/BISHOP/KNIGHT");
        }
    }

    private String resign(String... params) throws DataAccessException { // Add throws declaration
        if (pendingAction == null) {
            pendingAction = "resign";
            return "Confirm resignation with 'resign confirm'";
        } else if (pendingAction.equals("resign") && params.length > 0 && params[0].equals("confirm")) {
            pendingAction = null;
            sendRequest("POST", "/game/resign", "{\"gameID\":" + currentGameID + "}", authData.authToken());
            state = State.SIGNEDIN;
            currentGameID = null;
            return "You have resigned.";
        } else {
            pendingAction = null;
            return "Resignation canceled.";
        }
    }

    private String highlightMoves(String... params) throws DataAccessException { // Add throws declaration
        if (params.length < 1) throw new DataAccessException("Expected: <square>");
        try {
            ChessPosition pos = parsePosition(params[0]);
            Set<ChessPosition> moves = calculateLegalMoves(pos);
            return drawChessBoard(playerColor.equals("WHITE"), moves);
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("Invalid position: " + params[0]);
        }
    }

    private ChessPosition parsePosition(String input) throws DataAccessException {
        if (input.length() != 2) {
            throw new DataAccessException("Invalid position format. Use like 'e4'");
        }
        char fileChar = input.charAt(0);
        char rankChar = input.charAt(1);
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            throw new DataAccessException("Position out of bounds");
        }
        return new ChessPosition(
            Character.getNumericValue(rankChar),
            fileChar - 'a' + 1
        );
    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException("You must be logged in");
        }
    }

    private String drawChessBoard(boolean isWhitePerspective, Set<ChessPosition> highlights) {
        try{
            ChessGame game = new Gson().fromJson(currentGameState, ChessGame.class);
            ChessBoard board = game.getBoard();
        StringBuilder sb = new StringBuilder();
        int[] ranks = isWhitePerspective ? new int[]{8, 7, 6, 5, 4, 3, 2, 1} : new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        int[] files = isWhitePerspective ? new int[]{1, 2, 3, 4, 5, 6, 7, 8} : new int[]{8, 7, 6, 5, 4, 3, 2, 1};
    
        for (int rank : ranks) {
            sb.append(EscapeSequences.SET_TEXT_COLOR_WHITE)
              .append(rank).append(" ");
            
            for (int file : files) {
                ChessPosition pos = new ChessPosition(rank, file);
                String bgColor = determineBgColor(pos, highlights);
                ChessPiece piece = board.getPiece(pos);
                sb.append(bgColor).append(getPieceSymbol(piece));
            }
            sb.append(EscapeSequences.RESET_BG_COLOR).append("\n");
        }
        
        sb.append("   ");
        for (char c : (isWhitePerspective ? new char[]{'a','b','c','d','e','f','g','h'} 
                                        : new char[]{'h','g','f','e','d','c','b','a'})) {
            sb.append(" ").append(c).append(" ");
        }
        return sb.toString();
    } catch (Exception e) {
        return "Error drawing board: " + e.getMessage();
    }

    }
    
    private String determineBgColor(ChessPosition pos, Set<ChessPosition> highlights) {
        if (highlights != null && highlights.contains(pos)) {
            return EscapeSequences.SET_BG_COLOR_YELLOW;
        }
        boolean isLight = (pos.getRow() + pos.getColumn()) % 2 == 0;
        return isLight ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY 
                     : EscapeSequences.SET_BG_COLOR_DARK_GREY;
    }

private String getPieceSymbol(ChessPiece piece) {
    if (piece == null) return EscapeSequences.EMPTY;
    
    return switch (piece.getPieceType()) {
        case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
        case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
        case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
        case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
        case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
        case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 
            EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
    };
}

        private Set<ChessPosition> calculateLegalMoves(ChessPosition from) {
        try {
            ChessBoard board = new ChessBoard();
            board.loadFEN(currentGameState);
            ChessPiece piece = board.getPiece(from);
            
            if (piece == null) return Collections.emptySet();
            if (playerColor == null) return Collections.emptySet(); // Observers can't move
            
            ChessGame.TeamColor currentColor = playerColor.equals("WHITE") 
                ? ChessGame.TeamColor.WHITE 
                : ChessGame.TeamColor.BLACK;
                
            if (piece.getTeamColor() != currentColor) return Collections.emptySet();
            
            return piece.pieceMoves(board, from).stream()
                .map(ChessMove::getEndPosition)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}