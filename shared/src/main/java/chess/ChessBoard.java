package chess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessBoard {

    private final Map<ChessPosition, ChessPiece> board;

    public ChessBoard() {
        this.board = new HashMap<>();
    }

    /**
     * Creates a new ChessBoard by copying an existing board.
     *
     * @param otherBoard the board to copy.
     */
    public ChessBoard(ChessBoard otherBoard) {
        this.board = new HashMap<>(otherBoard.board);
    }


    /**
     * Adds a chess piece to the chessboard.
     *
     * @param position The position to add the piece at.
     * @param piece    the piece to add.
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board.put(position, piece);
    }

    /**
     * Gets a chess piece from the chessboard.
     *
     * @param position The position to get the piece from.
     * @return         The piece at the position, or null if no piece is at that position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board.get(position);
    }

    /**
     * Removes a chess piece from the chessboard.
     *
     * @param position The position to remove the piece from.
     */
    public void removePiece(ChessPosition position) {
        board.remove(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(board);
    }

    /**
     * Resets the chessboard to the default starting position.
     */
    public void resetBoard() {
        board.clear();
        initializePawns();
        initializeOtherPieces();
    }

    /**
     * Initializes pawns for both teams on the board.
     */
    private void initializePawns() {
        for (int col = 1; col <= 8; col++) {
            board.put(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            board.put(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    /**
     * Initializes other pieces (rooks, knights, bishops, queen, king) for both teams.
     */
    private void initializeOtherPieces() {
        // Initialize White pieces
        board.put(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        board.put(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        board.put(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        board.put(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        board.put(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        board.put(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        board.put(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        board.put(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        // Initialize Black pieces
        board.put(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        board.put(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        board.put(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        board.put(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        board.put(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        board.put(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        board.put(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        board.put(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
    }

    /**
     * Finds the position of the king for a specific team.
     *
     * @param teamColor the team whose king to find.
     * @return The position of the king, or null if the king is not on the board.
     */
    public ChessPosition findKingPosition(ChessGame.TeamColor teamColor) {
        return board.entrySet().stream()
                .filter(entry -> entry.getValue().getTeamColor() == teamColor
                        && entry.getValue().getPieceType() == ChessPiece.PieceType.KING)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Determines if a specific position is under attack by the opponent.
     *
     * @param position  the position to check.
     * @param teamColor the team that owns the position.
     * @return True if the position is under attack, false otherwise.
     */
    public boolean isPositionUnderAttack(ChessPosition position, ChessGame.TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = getPiece(pos);

                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }

                Collection<ChessMove> opponentMoves = piece.pieceMoves(this, pos);

                for (ChessMove move : opponentMoves) {
                    if (move.getEndPosition().equals(position)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Prints the chessboard to the console.
     */
    public void printBoard() {
        char[][] displayBoard = new char[8][8];

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                displayBoard[row][col] = ' ';
            }
        }

        for (Map.Entry<ChessPosition, ChessPiece> entry : board.entrySet()) {
            ChessPosition position = entry.getKey();
            ChessPiece piece = entry.getValue();
            displayBoard[position.getRow() - 1][position.getColumn() - 1] = piece.getSymbol();
        }

        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                System.out.print("|" + displayBoard[row][col]);
            }
            System.out.println("|");
        }
    }
}
