package chess.calculateMoves;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class CheckMove {
    static void up(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int r = row - 1; r >= 1; r--){
            ChessPosition newPosition = new ChessPosition(r, col);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void down(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int r = row + 1; r <= 8; r++){
            ChessPosition newPosition = new ChessPosition(r, col);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void right(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int c = col + 1; c <= 8; c++){
            ChessPosition newPosition = new ChessPosition(row, c);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void left(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int c = col - 1; c >= 1; c--){
            ChessPosition newPosition = new ChessPosition(row, c);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void upAndLeft(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int r = row -1, c = col - 1; r >= 1 && c >= 1; r--, c--){
            ChessPosition newPosition = new ChessPosition(r, c);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void upAndRight(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int r = row -1, c = col + 1; r >= 1 && c <= 8; r--, c++){
            ChessPosition newPosition = new ChessPosition(r, c);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void downAndLeft(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int r = row + 1, c = col - 1; r <= 8 && c >= 1; r++, c--){
            ChessPosition newPosition = new ChessPosition(r, c);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }
    static void downAndRight(ChessBoard board, ChessPosition position, ChessGame.TeamColor color, int row, int col, HashSet<ChessMove> possibleMoves) {
        for (int r = row +1, c = col + 1; r <= 8 && c <= 8; r++, c++){
            ChessPosition newPosition = new ChessPosition(r, c);
            if (addMove(board, position, newPosition, color, possibleMoves)) break;
        }
    }

    private static boolean addMove(ChessBoard board, ChessPosition startPosition, ChessPosition endPosition, ChessGame.TeamColor color, HashSet<ChessMove> possibleMoves) {
        ChessPiece piece = board.getPiece(endPosition);
        if (piece == null){
            possibleMoves.add(new ChessMove(startPosition, endPosition, null));
            return false;
        } else if (piece.getTeamColor() != color){
            possibleMoves.add(new ChessMove(startPosition, endPosition, null));
            return true;
        } else {
            return true;
            
        }
    }

}

