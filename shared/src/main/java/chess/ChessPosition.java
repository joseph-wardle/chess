package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int column;

    /**
     * Constructs a ChessPosition with the given row and column.
     *
     * @param row    the row number (1-8).
     * @param column the column number (1-8).
     * @throws IllegalArgumentException if the row or column is out of bounds.
     */
    public ChessPosition(int row, int column) {
        if (row < 1 || row > 8 || column < 1 || column > 8) {
            throw new IllegalArgumentException("Invalid position: row and column must be between 1 and 8.");
        }
        this.row = row;
        this.column = column;
    }

    /**
     * @return the row number of this position (1-8).
     */
    public int getRow() {
        return row;
    }


    /**
     * @return the column number of this position (1-8).
     */
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
