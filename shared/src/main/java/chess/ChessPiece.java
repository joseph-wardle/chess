package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece types.
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
     * @return The type of chess piece (e.g., KING, QUEEN)
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the valid moves a chess piece can make, excluding checks for
     * illegal moves that would leave the king in danger.
     *
     * @return A collection of valid moves.
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

    /**
     * Generates moves in a given direction until a piece is encountered or the edge of the board is reached.
     *
     * @param directions The directions to generate moves. Numbers should either be `1` or `0`
     */
    private void generateDirectionalMoves(ChessBoard board, ChessPosition myPosition, int[][] directions, Collection<ChessMove> moves) {
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
    }

    /**
     * Generates moves at fixed steps.
     *
     * @param steps The steps to generate moves.
     */

    private void generateStepMoves(ChessBoard board, ChessPosition myPosition, int[][] steps, Collection<ChessMove> moves) {
        for (int[] step : steps) {
            int newRow = myPosition.getRow() + step[0];
            int newColumn = myPosition.getColumn() + step[1];
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
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-1, -1}, {-1,  0}, {-1,  1},
                { 0, -1},           { 0,  1},
                { 1, -1}, { 1,  0}, { 1,  1}
        };

        generateStepMoves(board, myPosition, directions, moves);

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

        generateDirectionalMoves(board, myPosition, directions, moves);

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

        generateStepMoves(board, myPosition, directions, moves);

        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                     {-1,  0},
                { 0, -1}, { 0,  1},
                     { 1,  0}
        };

        generateDirectionalMoves(board, myPosition, directions, moves);

        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        int direction = color == ChessGame.TeamColor.WHITE ? 1 : -1;
        int startRow = color == ChessGame.TeamColor.WHITE ? 2 : 7;

        addPawnForwardMoves(board, myPosition, direction, startRow, moves);
        AddPawnAttackMoves(board, myPosition, direction, moves);

        return moves;
    }

    private void AddPawnAttackMoves(ChessBoard board, ChessPosition myPosition, int direction, Collection<ChessMove> moves) {
        int[][] attackDirections = {
                {direction, -1},
                {direction,  1}
        };

        for (int[] attackDirection : attackDirections) {
            int newRow = myPosition.getRow() + attackDirection[0];
            int newColumn = myPosition.getColumn() + attackDirection[1];
            try {
                ChessPosition newPosition = new ChessPosition(newRow, newColumn);
                ChessPiece piece = board.getPiece(newPosition);
                if (piece != null && piece.getTeamColor() != color) {
                    moves.addAll(pawnPromotionCheck(new ChessMove(myPosition, newPosition)));
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid positions
            }
        }
    }

    private void addPawnForwardMoves(ChessBoard board, ChessPosition myPosition, int direction, int startRow, Collection<ChessMove> moves) {
        int newRow = myPosition.getRow() + direction;
        int newColumn = myPosition.getColumn();
        try {
            ChessPosition newPosition = new ChessPosition(newRow, newColumn);
            ChessPiece piece = board.getPiece(newPosition);
            if (piece == null) {
                moves.addAll(pawnPromotionCheck(new ChessMove(myPosition, newPosition)));
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
    }

    /**
     * Checks if a pawn move is a promotion move and returns all possible promotion moves.
     * If the move is not a promotion move, it returns a set with the move itself.
     *
     * @param move The move to check
     * @return A set of promotion moves if the move is a promotion move, otherwise a set with the move itself
     */
    private Collection<ChessMove> pawnPromotionCheck(ChessMove move) {
        Collection<ChessMove> moves = new HashSet<>();
        int promotionZone = color == ChessGame.TeamColor.WHITE ? 8 : 1;
        if (move.getEndPosition().getRow() == promotionZone) {
            ChessPosition startPosition = move.getStartPosition();
            ChessPosition endPosition = move.getEndPosition();
            moves.add(new ChessMove(startPosition, endPosition, PieceType.QUEEN));
            moves.add(new ChessMove(startPosition, endPosition, PieceType.ROOK));
            moves.add(new ChessMove(startPosition, endPosition, PieceType.BISHOP));
            moves.add(new ChessMove(startPosition, endPosition, PieceType.KNIGHT));
        } else {
            moves.add(move);
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
