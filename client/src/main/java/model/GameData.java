package model;

import com.google.gson.Gson;

import chess.ChessGame;

public class GameData {
    private int gameID;
    private String gameName;
    private String whiteUsername;
    private String blackUsername;
    private String gameState;

    // Getters
    public int gameID() { return gameID; }
    public String gameName() { return gameName; }
    public String whiteUsername() { return whiteUsername; }
    public String blackUsername() { return blackUsername; }
    public ChessGame gameState() { return new Gson().fromJson(gameState, ChessGame.class); }
}