package chess.moves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessGame;
import java.util.HashSet;

public class KnightMoves {
    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        HashSet<ChessMove> possibleMoves = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();

        KingMoves.addMoveSpecial(board, position, new ChessPosition(row - 2, col - 1), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row - 2, col + 1), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row - 1, col - 2), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row - 1, col + 2), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row + 1, col - 2), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row + 1, col + 2), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row + 2, col - 1), color, possibleMoves);
        KingMoves.addMoveSpecial(board, position, new ChessPosition(row + 2, col + 1), color, possibleMoves);
        return possibleMoves;
    }
}