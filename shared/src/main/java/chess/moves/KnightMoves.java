package chess.moves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame;
import java.util.HashSet;

public class KnightMoves {
    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        HashSet<ChessMove> possibleMoves = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();

        addMoveForKnight(board, position, new ChessPosition(row - 2, col - 1), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row - 2, col + 1), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row - 1, col - 2), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row - 1, col + 2), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row + 1, col - 2), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row + 1, col + 2), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row + 2, col - 1), color, possibleMoves);
        addMoveForKnight(board, position, new ChessPosition(row + 2, col + 1), color, possibleMoves);
        return possibleMoves;
    }

        private static void addMoveForKnight(ChessBoard board, ChessPosition startPosition, ChessPosition endPosition, ChessGame.TeamColor color, HashSet<ChessMove> possibleMoves) {
            if (endPosition.getRow() >= 1 && endPosition.getRow() <= 8 && endPosition.getColumn() >= 1 && endPosition.getColumn() <= 8) {
                ChessPiece piece = board.getPiece(endPosition);
                if (piece == null || piece.getTeamColor() != color) {
                    possibleMoves.add(new ChessMove(startPosition, endPosition, null));
                }
            }
        }


}