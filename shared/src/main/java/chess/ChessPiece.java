package chess;
import java.util.*;

import chess.moves.*;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> possibleMoves = new HashSet<>();
        switch (type) {
            case BISHOP:
                possibleMoves = BishopMoves.calculateMoves(board, myPosition, pieceColor);
                break;
            case KING:
                possibleMoves = KingMoves.calculateMoves(board, myPosition, pieceColor);
                break;
            case KNIGHT:
                possibleMoves = KnightMoves.calculateMoves(board, myPosition, pieceColor);
                break;
            case PAWN:
                possibleMoves = PawnMoves.calculateMoves(board, myPosition, pieceColor);
                break;
            case QUEEN:
                possibleMoves = QueenMoves.calculateMoves(board, myPosition, pieceColor);
                break;
            case ROOK:
                possibleMoves = RookMoves.calculateMoves(board, myPosition, pieceColor);
                break;
            default:
                break;
        }
        return possibleMoves;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        ChessPiece that = (ChessPiece) obj;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
