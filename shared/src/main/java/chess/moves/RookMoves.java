package chess.moves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessGame;
import java.util.HashSet;

public class RookMoves {
    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        HashSet<ChessMove> possibleMoves = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();

        CheckMove.left(board, position, color, row, col, possibleMoves);
        CheckMove.right(board, position, color, row, col, possibleMoves);
        CheckMove.up(board, position, color, row, col, possibleMoves);
        CheckMove.down(board, position, color, row, col, possibleMoves);

        return possibleMoves;
    }
}