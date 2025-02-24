package chess.moves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame;
import java.util.HashSet;

public class PawnMoves {
    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        HashSet<ChessMove> possibleMoves = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;



        if ((color == ChessGame.TeamColor.WHITE && row == 2) || (color == ChessGame.TeamColor.BLACK && row == 7) 
        && board.getPiece(new ChessPosition(row + 1 * direction, col)) == null) {
            addMoveForPawn(board, position, new ChessPosition(row + 2 * direction, col), color, possibleMoves, false);
        }

        addMoveForPawn(board, position, new ChessPosition(row + direction, col), color, possibleMoves, false);
        addMoveForPawn(board, position, new ChessPosition(row + direction, col - 1), color, possibleMoves, true);
        addMoveForPawn(board, position, new ChessPosition(row + direction, col + 1), color, possibleMoves, true);

        return possibleMoves;
    }

        private static void addMoveForPawn(ChessBoard board, ChessPosition startPosition, 
        ChessPosition endPosition, ChessGame.TeamColor color, HashSet<ChessMove> possibleMoves, boolean capture) {
            if (endPosition.getRow() >= 1 && endPosition.getRow() <= 8 && endPosition.getColumn() >= 1 && endPosition.getColumn() <= 8) {
                ChessPiece piece = board.getPiece(endPosition);
                if (capture) {
                    if (piece != null && piece.getTeamColor() != color) {
                        addPromo(startPosition, endPosition, possibleMoves);
                    }
                } else {
                    if (piece == null) {
                        addPromo(startPosition, endPosition, possibleMoves);
                    }
                }
            }
        }
        private static void addPromo(ChessPosition startPosition, ChessPosition endPosition, HashSet<ChessMove> possibleMoves) {
            int endRow = endPosition.getRow();
            if (endRow == 1 || endRow == 8) {
            possibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
            possibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
            possibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
            possibleMoves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
        }else {
            possibleMoves.add(new ChessMove(startPosition, endPosition, null));
        }
    }


}