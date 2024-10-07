package chess;

import java.util.Collection;
import java.util.HashSet;

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
//        chessBoard.printBoard();
        ChessPiece piece = chessBoard.getPiece(startPosition);

        if (piece == null) return null;
        TeamColor pieceColor = piece.getTeamColor();

        Collection<ChessMove> possibleMoves = piece.pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : possibleMoves) {

            // Simulate the move
            ChessBoard simulatedBoard = new ChessBoard(chessBoard);
            simulatedBoard.addPiece(move.getEndPosition(), piece);
            simulatedBoard.removePiece(move.getStartPosition());

            // Check if the player is still in check after the move on the simulated board
            if (!isInCheck(pieceColor, simulatedBoard)) validMoves.add(move);
        }

        return validMoves;
    }

    private void printMoves(Collection<ChessMove> moves) {
        char[][] board = new char[8][8];

        // Initialize the board with empty spaces
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = ' ';
            }
        }

        // Mark the start and end positions
        for (ChessMove move : moves) {
            ChessPosition start = move.getStartPosition();
            ChessPosition end = move.getEndPosition();
            board[start.getRow() - 1][start.getColumn() - 1] = '#';
            board[end.getRow() - 1][end.getColumn() - 1] = '*';
        }

        // Print the board
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                System.out.print("|" + board[i][j]);
            }
            System.out.println("|");
        }
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
     * @param board the current state of the chessboard
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = null;

        // Step 1: Find the king's position by iterating through all board positions
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = pos;
                    break;
                }
            }
            if (kingPosition != null) {
                break;
            }
        }

        // If the king is not found on the board, return false
        if (kingPosition == null) {
            return false;
        }

        // Step 2: Iterate through all board positions to find opponent's pieces
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                // Skip if there's no piece or if it's the same team
                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }

                // Generate all possible moves for the opponent's piece
                Collection<ChessMove> opponentMoves = piece.pieceMoves(board, pos);

                // Check if any of these moves can capture the king
                for (ChessMove move : opponentMoves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }

        // If no opponent's piece can capture the king, return false
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
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // First, check if the team is in check
        if (!isInCheck(teamColor, chessBoard)) {
            return false;
        }

        // Iterate through all board positions to find team's pieces
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(pos);

                // Skip if there's no piece or if it's not the team's piece
                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

                // Generate all valid moves for this piece
                Collection<ChessMove> validMoves = validMoves(pos);

                // If any piece has a valid move, it's not checkmate
                if (validMoves != null && !validMoves.isEmpty()) {
                    return false;
                }
            }
        }

        // If no valid moves are available, and the team is in check, it's checkmate
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
        // First, check if the team is not in check
        if (isInCheck(teamColor, chessBoard)) {
            return false;
        }

        // Iterate through all board positions to find team's pieces
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(pos);

                // Skip if there's no piece or if it's not the team's piece
                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

                // Generate all valid moves for this piece
                Collection<ChessMove> validMoves = validMoves(pos);

                // If any piece has a valid move, it's not stalemate
                if (validMoves != null && !validMoves.isEmpty()) {
                    return false;
                }
            }
        }

        // If no valid moves are available and the team is not in check, it's stalemate
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
