package model;

public record GameData(
    int gameID, 
    String gameName, 
    String whiteUsername, 
    String blackUsername
) {}