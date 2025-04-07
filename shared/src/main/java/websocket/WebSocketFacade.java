package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import websocket.commands.*;
import websocket.messages.*;

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

    private void handleConnect() {
        // Validate authToken and gameID, then add session to GameData
        // Send LOAD_GAME and broadcast NOTIFICATION
    }

    private void handleMakeMove(MakeMoveCommand command) {
        // Validate move, update game in database, broadcast LOAD_GAME and NOTIFICATION
    }

    private void sendError(String errorMessage) {
        try {
            getRemote().sendString(gson.toJson(new ErrorMessage(errorMessage)));
        } catch (IOException ignored) {}
    }
}