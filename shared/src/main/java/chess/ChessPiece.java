package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
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
        return color;
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
        return switch (type) {
            case KING -> kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-1, -1}, {-1,  0}, {-1,  1},
                { 0, -1},           { 0,  1},
                { 1, -1}, { 1,  0}, { 1,  1}
        };

        for (int[] direction : directions) {
            int newRow = myPosition.getRow() + direction[0];
            int newColumn = myPosition.getColumn() + direction[1];
            try {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                ChessPiece piece = board.getPiece(newPosition);
                if (piece == null || piece.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, newPosition));
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid positions
            }
        }

        return moves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        moves.addAll(rookMoves(board, myPosition));
        moves.addAll(bishopMoves(board, myPosition));
        return moves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-1, -1}, {-1,  1},
                { 1, -1}, { 1,  1}
        };

        for (int[] direction : directions) {
            for (int i = 1; i < 8; i++) {
                int newRow = myPosition.getRow() + direction[0] * i;
                int newColumn = myPosition.getColumn() + direction[1] * i;
                try {
                    ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                    ChessPiece piece = board.getPiece(newPosition);
                    if (piece == null || piece.getTeamColor() != color) {
                        moves.add(new ChessMove(myPosition, newPosition));
                    }
                    if (piece != null) {
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    // Ignore invalid positions
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-2, -1}, {-2,  1},
                {-1, -2}, {-1,  2},
                { 1, -2}, { 1,  2},
                { 2, -1}, { 2,  1}
        };

        for (int[] direction : directions) {
            int newRow = myPosition.getRow() + direction[0];
            int newColumn = myPosition.getColumn() + direction[1];
            try {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                ChessPiece piece = board.getPiece(newPosition);
                if (piece == null || piece.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, newPosition));
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid positions
            }
        }

        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                     {-1,  0},
                { 0, -1}, { 0,  1},
                     { 1,  0}
        };

        for (int[] direction : directions) {
            for (int i = 1; i < 8; i++) {
                int newRow = myPosition.getRow() + direction[0] * i;
                int newColumn = myPosition.getColumn() + direction[1] * i;
                try {
                    ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                    ChessPiece piece = board.getPiece(newPosition);
                    if (piece == null || piece.getTeamColor() != color) {
                        moves.add(new ChessMove(myPosition, newPosition));
                    }
                    if (piece != null) {
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    // Ignore invalid positions
                }
            }
        }

        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int direction = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        int startRow = color == ChessGame.TeamColor.WHITE ? 2 : 7;
        int newRow = myPosition.getRow() + direction;
        int newColumn = myPosition.getColumn();
        try {
            ChessPosition newPosition = new ChessPosition(newRow, newColumn);
            ChessPiece piece = board.getPiece(newPosition);
            if (piece == null) {
                moves.add(new ChessMove(myPosition, newPosition));
                if (myPosition.getRow() == startRow) {
                    newRow = myPosition.getRow() + 2 * direction;
                    newPosition = new ChessPosition(newRow, newColumn);
                    piece = board.getPiece(newPosition);
                    if (piece == null) {
                        moves.add(new ChessMove(myPosition, newPosition));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Ignore invalid positions
        }

        int[][] attackDirections = {
                {-1,  direction},
                { 1,  direction}
        };

        for (int[] attackDirection : attackDirections) {
            newRow = myPosition.getRow() + attackDirection[0] * direction;
            newColumn = myPosition.getColumn() + attackDirection[1] * direction;
            try {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                ChessPiece piece = board.getPiece(newPosition);
                if (piece != null && piece.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, newPosition));
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid positions
            }
        }

        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}
