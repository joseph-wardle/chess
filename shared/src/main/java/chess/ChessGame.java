package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamColor;
    private ChessBoard chessBoard;

    public ChessGame() {
        teamColor = TeamColor.WHITE;
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
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
        ChessPiece piece = chessBoard.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        return piece.pieceMoves(chessBoard, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("Invalid move: there is no piece there");
        }
        if (piece.getTeamColor() != teamColor) {
            throw new InvalidMoveException("Invalid move: It's not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // Simulate the move
        ChessBoard simulatedBoard = new ChessBoard(chessBoard);
        simulatedBoard.addPiece(move.getEndPosition(), simulatedBoard.getPiece(move.getStartPosition()));
        simulatedBoard.removePiece(move.getStartPosition());

        // Check if the player is still in check after the move on the simulated board
        if (isInCheck(teamColor, simulatedBoard)) {
            throw new InvalidMoveException("Invalid move: You are still in check");
        }

        ChessPiece newPiece = null;
        if (move.getPromotionPiece() != null) {
            newPiece = new ChessPiece(teamColor, move.getPromotionPiece());
        } else {
            newPiece = piece;
        }
        chessBoard.addPiece(move.getEndPosition(), newPiece);
        chessBoard.removePiece(move.getStartPosition());
        teamColor = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = null;
        Collection<ChessPiece> pieces = board.getAllPieces();

        // Find the king's position
        for (ChessPiece piece : pieces) {
            if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                kingPosition = board.getPosition(piece);
                break;
            }
        }

        if (kingPosition == null) {
            return false;
            //throw new RuntimeException("King not found for team " + teamColor);
        }

        // Check if any opponent piece can move to the king's position
        for (ChessPiece piece : pieces) {
            if (piece.getTeamColor() != teamColor) {
                Collection<ChessMove> opponentMoves = piece.pieceMoves(board, board.getPosition(piece));
                for (ChessMove move : opponentMoves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, chessBoard);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        Collection<ChessPiece> pieces = chessBoard.getAllPieces();
        for (ChessPiece piece : pieces) {
            if (piece.getTeamColor() == teamColor) {
                Collection<ChessMove> validMoves = piece.pieceMoves(chessBoard, chessBoard.getPosition(piece));
                for (ChessMove move : validMoves) {
                    ChessBoard newBoard = new ChessBoard(chessBoard);
                    newBoard.addPiece(move.getEndPosition(), newBoard.getPiece(move.getStartPosition()));
                    newBoard.removePiece(move.getStartPosition());
                    if (!isInCheck(teamColor)) {
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
        Collection<ChessPiece> pieces = chessBoard.getAllPieces();
        if (!pieces.contains(new ChessPiece(teamColor, ChessPiece.PieceType.KING))) {
            throw new RuntimeException("No king found for team");
        }

        for (ChessPiece piece : pieces) {
            if (piece.getTeamColor() == teamColor) {
                Collection<ChessMove> validMoves = piece.pieceMoves(chessBoard, chessBoard.getPosition(piece));
                if (!validMoves.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }
}
