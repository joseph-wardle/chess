import chess.*;
import client.GameMessageHandler;
import client.GameWebSocketClient;
import client.ServerFacade;
import models.AuthToken;
import models.Game;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Main implements GameMessageHandler {
    private static ServerFacade serverFacade;
    private static Scanner scanner;
    private static AuthToken authToken;
    private static Map<Integer, Game> gameMap = new HashMap<>();
    private static GameWebSocketClient webSocketClient;
    private static ChessGame chessGame;

    public static void main(String[] args) {
        // Initialize server facade
        serverFacade = new ServerFacade(8080); // Use the correct port
        System.out.println("Welcome to 240 chess. Type Help to get started.");

        scanner = new Scanner(System.in);
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
                if (state == State.LOGGED_OUT) {
                    state = handleLoggedOutCommand(state, command, tokens);
                } else if (state == State.LOGGED_IN) {
                    state = handleLoggedInCommand(state, command, tokens);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    enum State {
        LOGGED_OUT, IN_GAME, LOGGED_IN
    }

    private static State handleLoggedOutCommand(State state, String command, String[] tokens) throws Exception {
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
        return state;
    }

    private static State handleLoggedInCommand(State state, String command, String[] tokens) throws Exception {
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
                    // Draw the chessboard from both perspectives
                    drawChessBoard(ChessGame.TeamColor.WHITE);
                    drawChessBoard(ChessGame.TeamColor.BLACK);
                }
                break;
            case "observe":
                if (tokens.length != 2) {
                    System.out.println("Usage: observe <GAME_NUMBER>");
                } else {
                    observeGame(tokens[1]);
                    // Draw the chessboard from both perspectives
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
        return state;
    }

    private static void printPreloginHelp() {
        System.out.println("Available commands:");
        System.out.println("  register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("  login <USERNAME> <PASSWORD> - to play chess");
        System.out.println("  quit - exit the application");
        System.out.println("  help - display this help message");
    }

    private static void printPostloginHelp() {
        System.out.println("Available commands:");
        System.out.println("  create <NAME> - create a new game");
        System.out.println("  list - list available games");
        System.out.println("  join <GAME_NUMBER> [WHITE|BLACK] - join a game");
        System.out.println("  observe <GAME_NUMBER> - observe a game");
        System.out.println("  logout - log out of your account");
        System.out.println("  quit - exit the application");
        System.out.println("  help - display this help message");
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
        // Do not display Game ID to the user
        System.out.println("Created game: " + game.getGameName());
    }

    private static void listGames() throws Exception {
        List<Game> games = serverFacade.listGames(authToken.getToken());
        gameMap.clear();
        System.out.println("Games:");
        int index = 1;
        for (Game game : games) {
            System.out.printf("%d. %s - Players: %s vs %s%n", index, game.getGameName(),
                    game.getWhiteUsername() != null ? game.getWhiteUsername() : "None",
                    game.getBlackUsername() != null ? game.getBlackUsername() : "None");
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

        // Connect to WebSocket
        webSocketClient = new GameWebSocketClient(new Main());
        webSocketClient.connect("ws://localhost:8080/ws");

        // Send CONNECT command
        UserGameCommand connectCommand = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken.getToken(),
                game.getGameID()
        );
        webSocketClient.sendCommand(connectCommand);

        // Transition to gameplay UI
        gameplayUI();
    }

    private static void observeGame(String gameNumberStr) throws Exception {
        int gameNumber = Integer.parseInt(gameNumberStr);
        if (!gameMap.containsKey(gameNumber)) {
            throw new Exception("Invalid game number. Please list games again.");
        }
        Game game = gameMap.get(gameNumber);
        System.out.println("Observing game: " + game.getGameName());
    }

    // Modified drawChessBoard method to accept a perspective parameter
    private static void drawChessBoard(ChessGame.TeamColor perspective) {
        if (chessGame == null) {
            System.out.println("No game stata avalible.");
            return;
        }
        ChessBoard board = chessGame.getBoard();
        board.resetBoard();
        System.out.println("Chessboard from " + perspective + " perspective:");
        String[] columnLabels = {"a", "b", "c", "d", "e", "f", "g", "h"};
        String resetColor = EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR;

        boolean isWhitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int startRow = isWhitePerspective ? 8 : 1;
        int endRow = isWhitePerspective ? 0 : 9;
        int rowStep = isWhitePerspective ? -1 : 1;

        // Print column labels based on perspective
        System.out.print("   ");
        for (int col = 1; col <= 8; col++) {
            int displayCol = isWhitePerspective ? col : 9 - col;
            String label = columnLabels[displayCol - 1];
            System.out.print(" " + label + " ");
        }
        System.out.println();

        for (int row = startRow; row != endRow; row += rowStep) {
            System.out.print(row + " ");
            for (int col = 1; col <= 8; col++) {
                int displayCol = isWhitePerspective ? col : 9 - col;
                ChessPosition position = new ChessPosition(row, displayCol);
                ChessPiece piece = board.getPiece(position);
                boolean isLightSquare = !((row + displayCol) % 2 == 0);
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                String pieceStr = (piece != null) ? getPieceString(piece) : EscapeSequences.EMPTY;
                System.out.print(bgColor + pieceStr + resetColor);
            }
            System.out.println(" " + row);
        }

        // Print column labels based on perspective
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
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
            default:
                return EscapeSequences.EMPTY;
        }
    }

    private static void gameplayUI() {
        State state = State.IN_GAME;
        while (state == State.IN_GAME) {
            System.out.print("[" + state + "] >>> ");
            String inputLine = scanner.nextLine().trim();
            if (inputLine.isEmpty()) {
                continue;
            }

            String[] tokens = inputLine.split("\\s+");
            String command = tokens[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        printGameplayHelp();
                        break;
                    case "redraw":
                        drawChessBoard(currentPerspective);
                        break;
                    case "leave":
                        // Send LEAVE command
                        UserGameCommand leaveCommand = new UserGameCommand(
                                UserGameCommand.CommandType.LEAVE,
                                authToken.getToken(),
                                currentGameID
                        );
                        webSocketClient.sendCommand(leaveCommand);
                        // Transition back to post-login UI
                        state = State.LOGGED_IN;
                        break;
                    case "move":
                        if (tokens.length != 3) {
                            System.out.println("Usage: move <START_POSITION> <END_POSITION>");
                        } else {
                            makeMove(tokens[1], tokens[2]);
                        }
                        break;
                    case "resign":
                        // Send RESIGN command
                        UserGameCommand resignCommand = new UserGameCommand(
                                UserGameCommand.CommandType.RESIGN,
                                authToken.getToken(),
                                currentGameID
                        );
                        webSocketClient.sendCommand(resignCommand);
                        break;
                    case "highlight":
                        if (tokens.length != 2) {
                            System.out.println("Usage: highlight <POSITION>");
                        } else {
                            highlightLegalMoves(tokens[1]);
                        }
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void makeMove(String startPosStr, String endPosStr) {
        try {
            ChessPosition start = parsePosition(startPosStr);
            ChessPosition end = parsePosition(endPosStr);
            ChessMove move = new ChessMove(start, end);

            UserGameCommand moveCommand = new UserGameCommand(
                    UserGameCommand.CommandType.MAKE_MOVE,
                    authToken.getToken(),
                    currentGameID,
                    move
            );
            webSocketClient.sendCommand(moveCommand);
        } catch (Exception e) {
            System.out.println("Error making move: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String posStr) throws Exception {
        if (posStr.length() != 2) {
            throw new Exception("Invalid position format. Use format like 'e2'.");
        }
        char columnChar = posStr.charAt(0);
        int column = columnChar - 'a' + 1;
        int row = Character.getNumericValue(posStr.charAt(1));
        return new ChessPosition(row, column);
    }


    @Override
    public void handleLoadGame(LoadGameMessage message) {
        chessGame = message.getGame();
        ChessGame.TeamColor currentPerspective = ChessGame.TeamColor.BLACK; // TODO: Make this work
        drawChessBoard(currentPerspective);
    }

    @Override
    public void handleNotification(NotificationMessage message) {
        System.out.println("Notification: " + message.getMessage());
    }

    @Override
    public void handleError(ErrorMessage message) {
        System.err.println("Error: " + message.getErrorMessage());
    }
}