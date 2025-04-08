package dataaccess;

import com.google.gson.Gson;

import chess.ChessGame;

public class Game {
    public final int gameID;
    public final String gameName;
    public String whiteUsername;
    public String blackUsername;
    public ChessGame gameState;

    // Constructor for memory storage
    public Game(int gameID, String gameName) {
        this.gameID = gameID;
        this.gameName = gameName;
    }

    public Game(String gameID, String gameName) {
        this.gameID = Integer.parseInt(gameID);
        this.gameName = gameName;
    }

    public String getGameStateJson() {
        return new Gson().toJson(gameState);
    }
    
    public void setGameStateFromJson(String json) {
        this.gameState = new Gson().fromJson(json, ChessGame.class);
    }
}