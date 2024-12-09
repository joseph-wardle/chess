package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import ui.EscapeSequences;

public class BoardRenderer {
    public static void drawChessBoard(ChessGame chessGame, ChessGame.TeamColor perspective) {
        if (chessGame == null) {
            System.out.println("Game state is not available.");
            return;
        }

        ChessBoard board = chessGame.getBoard();
        String[] columnLabels = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String resetColor = EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR;

        boolean isWhitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int startRow = isWhitePerspective ? 8 : 1;
        int endRow = isWhitePerspective ? 0 : 9;
        int rowStep = isWhitePerspective ? -1 : 1;

        // Column labels
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            int displayCol = isWhitePerspective ? col : 9 - col;
            System.out.print(" " + columnLabels[displayCol - 1] + " ");
        }
        System.out.println();

        // Board rows
        for (int row = startRow; row != endRow; row += rowStep) {
            System.out.print(row + " ");
            for (int col = 1; col <= 8; col++) {
                int displayCol = isWhitePerspective ? col : 9 - col;
                var position = new chess.ChessPosition(row, displayCol);
                ChessPiece piece = board.getPiece(position);
                boolean isLightSquare = (row + displayCol) % 2 != 0;
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                String pieceStr = getPieceString(piece);
                System.out.print(bgColor + pieceStr + resetColor);
            }
            System.out.println(" " + row);
        }

        // Column labels
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            int displayCol = isWhitePerspective ? col : 9 - col;
            System.out.print(" " + columnLabels[displayCol - 1] + " ");
        }
        System.out.println();
    }

    private static String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }
}