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

    /**
     * Constructs a ChessPiece with the specified color and type.
     *
     * @param pieceColor the color of the piece.
     * @param type       the type of the piece.
     */
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * Enum representing the various different chess piece types.
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
     * @return The team color of this chess piece.
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return The type of chess piece.
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all possible moves for this piece, excluding checks for
     * illegal moves that would leave the king in danger.
     *
     * @param board           The current state of the chessboard.
     * @param currentPosition The current position of this piece.
     * @return A collection of possible moves.
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition currentPosition) {
        return switch (type) {
            case KING -> calculateKingMoves(board, currentPosition);
            case QUEEN -> calculateQueenMoves(board, currentPosition);
            case BISHOP -> calculateBishopMoves(board, currentPosition);
            case KNIGHT -> calculateKnightMoves(board, currentPosition);
            case ROOK -> calculateRookMoves(board, currentPosition);
            case PAWN -> calculatePawnMoves(board, currentPosition);
        };
    }

    /**
     * Calculates all possible king moves from the current position.
     *
     * @param board    The current state of the chessboard.
     * @param position The current position of the king.
     * @return A collection of king moves.
     */
    private Collection<ChessMove> calculateKingMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-1, -1}, {-1,  0}, {-1,  1},
                { 0, -1},          { 0,  1},
                { 1, -1}, { 1,  0}, { 1,  1}
        };

        generateStepMoves(board, position, directions, moves);

        return moves;
    }

    /**
     * Calculates all possible queen moves from the current position.
     *
     * @param board    The current state of the chessboard.
     * @param position The current position of the queen.
     * @return A collection of queen moves.
     */
    private Collection<ChessMove> calculateQueenMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        moves.addAll(calculateRookMoves(board, position));
        moves.addAll(calculateBishopMoves(board, position));
        return moves;
    }

    /**
     * Calculates all possible bishop moves from the current position.
     *
     * @param board    The current state of the chessboard.
     * @param position The current position of the bishop.
     * @return A collection of bishop moves.
     */
    private Collection<ChessMove> calculateBishopMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-1, -1}, {-1,  1},
                { 1, -1}, { 1,  1}
        };

        generateDirectionalMoves(board, position, directions, moves);
        return moves;
    }

    /**
     * Calculates all possible knight moves from the current position.
     *
     * @param board    The current state of the chessboard.
     * @param position The current position of the knight.
     * @return A collection of knight moves.
     */
    private Collection<ChessMove> calculateKnightMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] steps = {
                {-2, -1}, {-2,  1},
                {-1, -2}, {-1,  2},
                { 1, -2}, { 1,  2},
                { 2, -1}, { 2,  1}
        };

        generateStepMoves(board, position, steps, moves);
        return moves;
    }

    /**
     * Calculates all possible rook moves from the current position.
     *
     * @param board    The current state of the chessboard.
     * @param position The current position of the rook.
     * @return A collection of rook moves.
     */
    private Collection<ChessMove> calculateRookMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {-1,  0},
                { 0, -1}, { 0,  1},
                { 1,  0}
        };

        generateDirectionalMoves(board, position, directions, moves);
        return moves;
    }

    /**
     * Calculates all possible pawn moves from the current position.
     *
     * @param board    The current state of the chessboard.
     * @param position The current position of the pawn.
     * @return A collection of pawn moves.
     */
    private Collection<ChessMove> calculatePawnMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new HashSet<>();
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;

        addPawnForwardMoves(board, position, direction, startRow, moves);
        addPawnAttackMoves(board, position, direction, moves);

        return moves;
    }

    /**
     * Generates directional moves (e.g., for bishops, rooks, queens).
     *
     * @param board         The current state of the chessboard.
     * @param currentPosition The current position of the piece.
     * @param directions    The directions to move in.
     * @param moves         The collection to add valid moves to.
     */
    private void generateDirectionalMoves(ChessBoard board, ChessPosition currentPosition, int[][] directions, Collection<ChessMove> moves) {
        for (int[] direction : directions) {
            for (int i = 1; i < 8; i++) {
                int newRow = currentPosition.getRow() + direction[0] * i;
                int newCol = currentPosition.getColumn() + direction[1] * i;
                ChessPosition newPosition;
                try {
                    newPosition = new ChessPosition(newRow, newCol);
                } catch (IllegalArgumentException e) {
                    break;
                }

                ChessPiece targetPiece = board.getPiece(newPosition);
                if (targetPiece != null && targetPiece.getTeamColor() == color) {
                    break;
                }

                moves.add(new ChessMove(currentPosition, newPosition));
                if (targetPiece != null) {
                    break;
                }
            }
        }
    }

    /**
     * Generates step moves (e.g., for kings, knights).
     *
     * @param board         The current state of the chessboard.
     * @param currentPosition The current position of the piece.
     * @param steps         The specific steps to move.
     * @param moves         The collection to add valid moves to.
     */
    private void generateStepMoves(ChessBoard board, ChessPosition currentPosition, int[][] steps, Collection<ChessMove> moves) {
        for (int[] step : steps) {
            int newRow = currentPosition.getRow() + step[0];
            int newCol = currentPosition.getColumn() + step[1];
            try {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);
                if (targetPiece == null || targetPiece.getTeamColor() != color) {
                    moves.add(new ChessMove(currentPosition, newPosition));
                }
            } catch (IllegalArgumentException e) {
                // Position is off the board, ignore
            }
        }
    }

    /**
     * Adds forward moves for a pawn, including double moves from the starting position.
     *
     * @param board      The current state of the chessboard.
     * @param position   The current position of the pawn.
     * @param direction  The direction the pawn moves in.
     * @param startRow   The starting row of the pawn.
     * @param moves      The collection to add valid moves to.
     */
    private void addPawnForwardMoves(ChessBoard board, ChessPosition position, int direction, int startRow, Collection<ChessMove> moves) {
        // Single step forward
        int forwardRow = position.getRow() + direction;
        int column = position.getColumn();
        try {
            ChessPosition forwardPosition = new ChessPosition(forwardRow, column);
            if (board.getPiece(forwardPosition) == null) {
                moves.addAll(createPromotionMoves(position, forwardPosition));

                // Double step forward from starting position
                if (position.getRow() == startRow) {
                    int doubleForwardRow = position.getRow() + 2 * direction;
                    ChessPosition doubleForwardPosition = new ChessPosition(doubleForwardRow, column);
                    if (board.getPiece(doubleForwardPosition) == null) {
                        moves.add(new ChessMove(position, doubleForwardPosition));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Position is off the board, ignore
        }
    }

    /**
     * Adds attack moves for a pawn.
     *
     * @param board      The current state of the chessboard.
     * @param position   The current position of the pawn.
     * @param direction  The direction the pawn moves in.
     * @param moves      The collection to add valid moves to.
     */
    private void addPawnAttackMoves(ChessBoard board, ChessPosition position, int direction, Collection<ChessMove> moves) {
        int[][] attackSteps = {
                {direction, -1},
                {direction,  1}
        };

        for (int[] step : attackSteps) {
            int attackRow = position.getRow() + step[0];
            int attackCol = position.getColumn() + step[1];
            try {
                ChessPosition attackPosition = new ChessPosition(attackRow, attackCol);
                ChessPiece targetPiece = board.getPiece(attackPosition);
                if (targetPiece != null && targetPiece.getTeamColor() != color) {
                    moves.addAll(createPromotionMoves(position, attackPosition));
                }
            } catch (IllegalArgumentException e) {
                // Position is off the board, ignore
            }
        }
    }

    /**
     * Creates promotion moves if the pawn reaches the promotion zone.
     *
     * @param start the starting position.
     * @param end   the ending position.
     * @return A collection of promotion moves if applicable, otherwise a single move.
     */
    private Collection<ChessMove> createPromotionMoves(ChessPosition start, ChessPosition end) {

        Collection<ChessMove> promotionMoves = new HashSet<>();
        int promotionRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;

        if (end.getRow() == promotionRow) {
            for (PieceType promotionType : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
                promotionMoves.add(new ChessMove(start, end, promotionType));
            }
        } else {
            promotionMoves.add(new ChessMove(start, end));
        }

        return promotionMoves;
    }

    /**
     * @return The symbol representing this chess piece.
     */
    public char getSymbol() {
        return switch (type) {
            case KING -> (color == ChessGame.TeamColor.WHITE) ? 'K' : 'k';
            case QUEEN -> (color == ChessGame.TeamColor.WHITE) ? 'Q' : 'q';
            case ROOK -> (color == ChessGame.TeamColor.WHITE) ? 'R' : 'r';
            case BISHOP -> (color == ChessGame.TeamColor.WHITE) ? 'B' : 'b';
            case KNIGHT -> (color == ChessGame.TeamColor.WHITE) ? 'N' : 'n';
            case PAWN -> (color == ChessGame.TeamColor.WHITE) ? 'P' : 'p';
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }
}
