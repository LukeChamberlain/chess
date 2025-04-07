
package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import websocket.commands.*;
import websocket.messages.*;
import server.GameManager;
import dataaccess.*;
import chess.ChessGame;

public class WebSocketFacade extends WebSocketAdapter {
    private final Gson gson = new Gson();
    private Session session;
    private Integer gameID;
    private String authToken;

    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
    }

    @Override
    public void onWebSocketText(String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            authToken = command.getAuthToken();
            gameID = command.getGameID();

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect();
                case MAKE_MOVE -> handleMakeMove(gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeave();
                case RESIGN -> handleResign();
            }
        } catch (Exception e) {
            sendError(e.getMessage());
        }
    }

    private void handleConnect() throws DataAccessException {
        // Validate authToken and gameID
        String username = Server.userStorage.getUsernameFromToken(authToken);
        if (username == null) {
            sendError("Invalid auth token");
            return;
        }
        Game game = Server.gameStorage.getGame(String.valueOf(gameID));
        if (game == null) {
            sendError("Invalid game ID");
            return;
        }

        GameManager.addSessionToGame(gameID, session);
        sendLoadGame(game.gameState);
        broadcastNotification(username + " connected to the game");
    }

    private void handleMakeMove(MakeMoveCommand command) throws DataAccessException {
        Game game = Server.gameStorage.getGame(String.valueOf(gameID));
        ChessGame chessGame = game.gameState;
        if (!chessGame.makeMove(command.getMove())) {
            sendError("Invalid move");
            return;
        }

        Server.gameStorage.updateGameState(String.valueOf(gameID), chessGame);
        broadcastLoadGame(chessGame);
        broadcastNotification("Move made: " + command.getMove());
    }

    private void sendLoadGame(ChessGame game) {
        try {
            session.getRemote().sendString(gson.toJson(new LoadGameMessage(game)));
        } catch (Exception ignored) {}
    }

    private void broadcastLoadGame(ChessGame game) {
        GameManager.broadcastMessage(gameID, gson.toJson(new LoadGameMessage(game)), session);
    }

    private void broadcastNotification(String message) {
        GameManager.broadcastMessage(gameID, gson.toJson(new NotificationMessage(message)), session);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        GameManager.removeSession(gameID, session);
    }
}