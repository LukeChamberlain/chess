package chess;

import java.util.Collection;
import java.util.HashSet;
import chess.ChessGame;



/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor currentTurn;
    ChessBoard board;

    public ChessGame() {
        this.currentTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for(ChessMove move : possibleMoves){
            ChessBoard copiedBoard = copyBoard(board);
            applyMoveToBoard(move, copiedBoard);
            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(copiedBoard);
            if (!tempGame.isInCheck(piece.getTeamColor())){
                validMoves.add(move);
            }
        }
        return validMoves;
    }
    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) throw new InvalidMoveException("No piece at start position");
        if (piece.getTeamColor() != currentTurn) throw new InvalidMoveException("Not the current team's turn");
        Collection<ChessMove> valid = validMoves(start);
        if (valid == null || !valid.contains(move)) throw new InvalidMoveException("Invalid move");

        applyMoveToBoard(move, board);
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }
    private ChessBoard copyBoard(ChessBoard original){
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(position);
                if (piece != null){
                    copy.addPiece(position, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return copy;
    }
    private void applyMoveToBoard(ChessMove move, ChessBoard board){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        board.addPiece(start, null);
        ChessPiece.PieceType promotion = move.getPromotionPiece();
        if(promotion != null){
            piece = new ChessPiece(piece.getTeamColor(), promotion);
        }
        board.addPiece(end, piece);
    }
    public void setBoard(ChessBoard board){
        this.board = board;
    }

    public ChessBoard getBoard(){
        return board;
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        for (int row = 1; row <= 8; row++){
            for (int col= 1; col <= 8; col++){
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if(piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING){
                    kingPosition = new ChessPosition(row, col); break;
                }
            }
        }
        if (kingPosition == null){
            throw new RuntimeException("Error: King is not found check isInCheck Function in ChessGame.java");
        }

        TeamColor opponentColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int row = 1; row <= 8; row++){
            for (int col= 1; col <= 8; col++){
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if(piece != null && piece.getTeamColor() == opponentColor){
                    Collection<ChessMove> moves = piece.pieceMoves(board, new ChessPosition(row, col));
                    for(ChessMove move: moves){
                        if(move.getEndPosition().equals(kingPosition)){
                            return true;
                        }
                    }
                }            
            }
        }
        return false;
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }
        for (int row = 1; row <= 8; row++){
            for (int col= 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> validMoves = validMoves(position);
                    if (validMoves != null && !validMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }
        for (int row = 1; row <= 8; row++){
            for (int col= 1; col <= 8; col++){
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> validMoves = validMoves(position);
                    if (validMoves != null && !validMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
}
