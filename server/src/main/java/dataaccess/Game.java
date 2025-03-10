package dataaccess;

public class Game {
    public String gameID;
    public String gameName;
    public String whiteUsername;
    public String blackUsername;

    public Game(String gameID, String gameName) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.whiteUsername = null;
        this.blackUsername = null;
    }
}