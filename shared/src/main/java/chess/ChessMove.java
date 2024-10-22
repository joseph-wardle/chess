package chess;

import java.util.Objects;

/**
 * Represents a move of chess piece from one position to another.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessMove {
    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    /**
     * Constructs a ChessMove without a promotion.
     *
     * @param startPosition the starting position of the move.
     * @param endPosition   the ending position of the move.
     */
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition) {
        this(startPosition, endPosition, null);
    }

    /**
     * Constructs a ChessMove with a promotion.
     *
     * @param startPosition   the starting position of the move.
     * @param endPosition     the ending position of the move.
     * @param promotionPiece  the piece type to promote to, or null if no promotion.
     */
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }


    /**
     * @return The starting position of the move
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return The ending position of the move
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * @return The type of piece to promote a pawn to, or null if no promotion.
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPosition, chessMove.startPosition) &&
                Objects.equals(endPosition, chessMove.endPosition) &&
                promotionPiece == chessMove.promotionPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    @Override
    public String toString() {
        return "ChessMove{" +
                "startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", promotionPiece=" + promotionPiece +
                '}';
    }
}
