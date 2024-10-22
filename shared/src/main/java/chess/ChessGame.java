package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * Manages a chess game, handling moves on a chess board.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTeamColor;
    private ChessBoard chessBoard;

    public ChessGame() {
        currentTeamColor = TeamColor.WHITE;
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    /**
     * @return The team whose turn it currently is
     */
    public TeamColor getTeamTurn() {
        return currentTeamColor;
    }

    /**
     * Set's the team whose turn it currently is.
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeamColor = team;
    }

    /**
     * Enum identifying the two possible teams in a chess game.
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location.
     *
     * @param startPosition the position of the piece to get valid moves for.
     * @return A collection of valid moves for the requested piece, or null if no piece is at startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        TeamColor pieceColor = piece.getTeamColor();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : possibleMoves) {

            // Simulate the move
            ChessBoard simulatedBoard = new ChessBoard(chessBoard);
            simulatedBoard.addPiece(move.getEndPosition(), piece);
            simulatedBoard.removePiece(move.getStartPosition());

            // Check if the player is still in check after the move on the simulated board
            if (isMoveSafe(pieceColor, move)) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Checks if performing a move would leave the king in check.
     *
     * @param pieceColor the color of the team making the move.
     * @param move       the move to check.
     * @return True if the move is safe, false otherwise.
     */
    private boolean isMoveSafe(TeamColor pieceColor, ChessMove move) {
        ChessBoard simulatedBoard = new ChessBoard(chessBoard);
        ChessPiece pieceToMove = chessBoard.getPiece(move.getStartPosition());

        if (move.getPromotionPiece() != null) {
            pieceToMove = new ChessPiece(pieceColor, move.getPromotionPiece());
        }

        simulatedBoard.addPiece(move.getEndPosition(), pieceToMove);
        simulatedBoard.removePiece(move.getStartPosition());

        return !isInCheck(pieceColor, simulatedBoard);
    }

    /**
     * Makes a move in a chess game.
     *
     * @param move the chess move to preform.
     * @throws InvalidMoveException if move is invalid.
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Invalid move: There is no piece at the starting position.");
        }
        if (piece.getTeamColor() != currentTeamColor) {
            throw new InvalidMoveException("Invalid move: It's not your turn.");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move.");
        }

        ChessPiece pieceAfterMove = move.getPromotionPiece() != null
                ? new ChessPiece(currentTeamColor, move.getPromotionPiece())
                : piece;

        chessBoard.addPiece(move.getEndPosition(), pieceAfterMove);
        chessBoard.removePiece(move.getStartPosition());
        toggleTeamTurn();
    }

    /**
     * Toggles the current team's turn to the other team.
     */
    private void toggleTeamTurn() {
        currentTeamColor = (currentTeamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check.
     *
     * @param teamColor the team to check for check.
     * @param board     the current state of the chessboard.
     * @return True if the specified team is in check, otherwise false.
     */
    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = board.findKingPosition(teamColor);

        if (kingPosition == null) {
            return false;
        }

        return board.isPositionUnderAttack(kingPosition, teamColor);
    }


    /**
     * Determines if the given team is in check.
     *
     * @param teamColor the team to check for check.
     * @return True if the specified team is in check, false otherwise.
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, chessBoard);
    }

    /**
     * Determines if the given team is in checkmate.
     *
     * @param teamColor which team to check for checkmate.
     * @return True if the specified team is in checkmate.
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor, chessBoard) && noValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor, chessBoard) && noValidMoves(teamColor);
    }

    public boolean noValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(pos);

                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

                Collection<ChessMove> validMoves = validMoves(pos);

                if (validMoves != null && !validMoves.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard with a given board.
     *
     * @param board the new board to use.
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard.
     *
     * @return the chessboard.
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }
}
