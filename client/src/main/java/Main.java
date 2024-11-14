import chess.*;
import client.ServerFacade;
import models.AuthToken;
import models.Game;
import ui.EscapeSequences;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static ServerFacade serverFacade;
    private static Scanner scanner;
    private static AuthToken authToken;
    private static Map<Integer, Game> gameMap;

    public static void main(String[] args) {
        // Initialize server facade
        serverFacade = new ServerFacade(8080); // Use the correct port
        System.out.println("Welcome to 240 chess. Type Help to get started.");

        Scanner scanner = new Scanner(System.in);
        State state = State.LOGGED_OUT;

        while (true) {
            System.out.print("[" + state + "] >>> ");
            String inputLine = scanner.nextLine().trim();
            if (inputLine.isEmpty()) {
                continue;
            }

            String[] tokens = inputLine.split("\\s+");
            String command = tokens[0].toLowerCase();

            try {
                switch (state) {
                    case LOGGED_OUT:
                        switch (command) {
                            case "help":
                                printPreloginHelp();
                                break;
                            case "register":
                                if (tokens.length != 4) {
                                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                                } else {
                                    register(tokens[1], tokens[2], tokens[3]);
                                    state = State.LOGGED_IN;
                                }
                                break;
                            case "login":
                                if (tokens.length != 3) {
                                    System.out.println("Usage: login <USERNAME> <PASSWORD>");
                                } else {
                                    login(tokens[1], tokens[2]);
                                    state = State.LOGGED_IN;
                                }
                                break;
                            case "quit":
                                System.out.println("Goodbye!");
                                scanner.close();
                                System.exit(0);
                            default:
                                System.out.println("Unknown command. Type 'help' for a list of commands.");
                        }
                        break;
                    case LOGGED_IN:
                        switch (command) {
                            case "help":
                                printPostloginHelp();
                                break;
                            case "logout":
                                logout();
                                state = State.LOGGED_OUT;
                                break;
                            case "create":
                                if (tokens.length != 2) {
                                    System.out.println("Usage: create <NAME>");
                                } else {
                                    createGame(tokens[1]);
                                }
                                break;
                            case "list":
                                listGames();
                                break;
                            case "join":
                                if (tokens.length != 3) {
                                    System.out.println("Usage: join <GAME_NUMBER> [WHITE|BLACK]");
                                } else {
                                    joinGame(tokens[1], tokens[2]);
                                    // For now, just draw the initial chessboard
                                    drawChessBoard(ChessGame.TeamColor.WHITE);
                                    drawChessBoard(ChessGame.TeamColor.BLACK);
                                }
                                break;
                            case "observe":
                                if (tokens.length != 2) {
                                    System.out.println("Usage: observe <GAME_NUMBER>");
                                } else {
                                    observeGame(tokens[1]);
                                    // For now, just draw the initial chessboard
                                    drawChessBoard(ChessGame.TeamColor.WHITE);
                                    drawChessBoard(ChessGame.TeamColor.BLACK);
                                }
                                break;
                            case "quit":
                                System.out.println("Goodbye!");
                                scanner.close();
                                System.exit(0);
                            default:
                                System.out.println("Unknown command. Type 'help' for a list of commands.");
                        }
                        break;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    enum State {
        LOGGED_OUT, LOGGED_IN
    }

    private static void printPreloginHelp() {
        System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("  login <USERNAME> <PASSWORD> - to play chess");
        System.out.println("  quit - playing chess");
        System.out.println("  help - with possible commands");
    }

    private static void printPostloginHelp() {
        System.out.println("  create <NAME> - a game");
        System.out.println("  list - games");
        System.out.println("  join <ID> [WHITE|BLACK] - a game");
        System.out.println("  observe <ID> - a game");
        System.out.println("  logout - when you are done");
        System.out.println("  quit - playing chess");
        System.out.println("  help - with possible commands");
    }

    private static void register(String username, String password, String email) throws Exception {
        authToken = serverFacade.register(username, password, email);
        System.out.println("Logged in as " + authToken.getUsername());
    }

    private static void login(String username, String password) throws Exception {
        authToken = serverFacade.login(username, password);
        System.out.println("Logged in as " + authToken.getUsername());
    }

    private static void logout() throws Exception {
        serverFacade.logout(authToken.getToken());
        authToken = null;
        System.out.println("Logged out.");
    }

    private static void createGame(String gameName) throws Exception {
        Game game = serverFacade.createGame(authToken.getToken(), gameName);
        System.out.println("Created game: " + game.getGameName() + " (ID: " + game.getGameID() + ")");
    }

    private static void listGames() throws Exception {
        List<Game> games = serverFacade.listGames(authToken.getToken());
        gameMap.clear();
        System.out.println("Games:");
        int index = 1;
        for (Game game : games) {
            System.out.printf("%d. %s - Players: %s vs %s\n", index, game.getGameName(),
                    game.getWhitePlayer() != null ? game.getWhitePlayer() : "None",
                    game.getBlackPlayer() != null ? game.getBlackPlayer() : "None");
            gameMap.put(index, game);
            index++;
        }
    }

    private static void joinGame(String gameNumberStr, String color) throws Exception {
        int gameNumber = Integer.parseInt(gameNumberStr);
        if (!gameMap.containsKey(gameNumber)) {
            throw new Exception("Invalid game number. Please list games again.");
        }
        Game game = gameMap.get(gameNumber);
        serverFacade.joinGame(authToken.getToken(), game.getGameID(), color.toLowerCase());
        System.out.println("Joined game: " + game.getGameName() + " as " + color.toUpperCase());
    }

    private static void observeGame(String gameNumberStr) throws Exception {
        int gameNumber = Integer.parseInt(gameNumberStr);
        if (!gameMap.containsKey(gameNumber)) {
            throw new Exception("Invalid game number. Please list games again.");
        }
        Game game = gameMap.get(gameNumber);
        System.out.println("Observing game: " + game.getGameName());
        // For now, just draw the initial chessboard
        drawChessBoard(ChessGame.TeamColor.WHITE);
        drawChessBoard(ChessGame.TeamColor.BLACK);
    }

    private static void drawChessBoard(ChessGame.TeamColor perspective) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println("Chessboard from " + perspective + " perspective:");
        String[] columnLabels = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String resetColor = EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR;

        boolean isWhitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int startRow = isWhitePerspective ? 8 : 1;
        int endRow = isWhitePerspective ? 0 : 9;
        int rowStep = isWhitePerspective ? -1 : 1;

        for (int row = startRow; row != endRow; row += rowStep) {
            System.out.print(row + " ");
            for (int col = 1; col <= 8; col++) {
                int displayCol = isWhitePerspective ? col : 9 - col;
                ChessPosition position = new ChessPosition(row, displayCol);
                ChessPiece piece = board.getPiece(position);
                boolean isLightSquare = (row + displayCol) % 2 == 0;
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                String pieceStr = EscapeSequences.EMPTY;
                if (piece != null) {
                    pieceStr = getPieceString(piece);
                }
                System.out.print(bgColor + pieceStr + resetColor);
            }
            System.out.println(" " + row);
        }
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            int displayCol = isWhitePerspective ? col : 9 - col;
            String label = columnLabels[displayCol - 1];
            System.out.print(" " + label + " ");
        }
        System.out.println();
    }

    private static String getPieceString(ChessPiece piece) {
        switch (piece.getPieceType()) {
            case KING:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
            default:
                return EscapeSequences.EMPTY;
        }
    }
}