package chess.moves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessGame;
import java.util.HashSet;

public class QueenMoves {
    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        HashSet<ChessMove> possibleMoves = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();

        CheckMove.upAndLeft(board, position, color, row, col, possibleMoves);
        CheckMove.upAndRight(board, position, color, row, col, possibleMoves);
        CheckMove.downAndLeft(board, position, color, row, col, possibleMoves);
        CheckMove.downAndRight(board, position, color, row, col, possibleMoves);
        CheckMove.left(board, position, color, row, col, possibleMoves);
        CheckMove.right(board, position, color, row, col, possibleMoves);
        CheckMove.up(board, position, color, row, col, possibleMoves);
        CheckMove.down(board, position, color, row, col, possibleMoves);

        return possibleMoves;
    }
}