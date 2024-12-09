package ui;

import chess.ChessPosition;
import models.Game;

import java.util.Map;

public class UIUtils {
    public static int parseGameNumber(String gameNumberStr, Map<Integer, Game> gameMap) throws Exception {
        try {
            int gameNumber = Integer.parseInt(gameNumberStr);
            if (!gameMap.containsKey(gameNumber)) {
                throw new Exception("Invalid game number. Please list games again.");
            }
            return gameNumber;
        } catch (NumberFormatException e) {
            throw new Exception("Invalid game number format.");
        }
    }

    public static ChessPosition parsePosition(String posStr) throws Exception {
        if (posStr.length() != 2) {
            throw new Exception("Invalid position format. Use format like 'e2'.");
        }
        char colChar = posStr.charAt(0);
        char rowChar = posStr.charAt(1);

        int column = colChar - 'a' + 1;
        int row = Character.getNumericValue(rowChar);

        if (column < 1 || column > 8 || row < 1 || row > 8) {
            throw new Exception("Position out of bounds. Columns a-h, rows 1-8.");
        }

        return new ChessPosition(row, column);
    }

    public static String formatPosition(ChessPosition position) {
        char column = (char) ('a' + position.getColumn() - 1);
        int row = position.getRow();
        return "" + column + row;
    }
}