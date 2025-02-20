package dataaccess;

public interface GameStorage {
    void clearAllGames();
    void addGame(String gameID, String gameName);
}
