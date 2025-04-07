package client.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.messages.*;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.Session;
import client.ChessClient;

public class ClientWebSocketFacade extends WebSocketAdapter {
    private final Gson gson = new Gson();
    private Session session;
    private ChessClient client;

    public ClientWebSocketFacade(ChessClient client) {
        this.client = client;
    }

    @Override
    public void onWebSocketText(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> client.updateBoard(((LoadGameMessage) serverMessage).getGame());
            case NOTIFICATION -> client.notify(((NotificationMessage) serverMessage).getMessage());
            case ERROR -> client.notifyError(((ErrorMessage) serverMessage).getErrorMessage());
        }
    }
}