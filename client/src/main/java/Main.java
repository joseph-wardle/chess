import chess.*;
import client.ServerFacade;
import models.AuthToken;
import models.Game;
import ui.EscapeSequences;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.*;

public class Main implements GameMessageHandler {
    private ServerFacade serverFacade;
    private Scanner scanner;
    private AuthToken authToken;
    private Map<Integer, Game> gameMap = new HashMap<>();
    private GameWebSocketClient webSocketClient;
    private ChessGame chessGame;
    private ChessGame.TeamColor currentPerspective;
    private int currentGameID;

    public static void main(String[] args) {
        Main mainApp = new Main();
        mainApp.run();
    }

    public void run() {
        serverFacade = new ServerFacade(8080);
        scanner = new Scanner(System.in);
        System.out.println("Welcome to 240 Chess. Type 'help' to get started.");

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
                        state = handleLoggedOutCommand(command, tokens);
                        break;
                    case LOGGED_IN:
                        state = handleLoggedInCommand(command, tokens);
                        break;
                    case IN_GAME:
                        state = handleInGameCommand(command, tokens);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private enum State {
        LOGGED_OUT, LOGGED_IN, IN_GAME
    }

    private State handleLoggedOutCommand(String command, String[] tokens) throws Exception {
        switch (command) {
            case "help":
                printPreLoginHelp();
                break;
            case "register":
                if (tokens.length != 4) {
                    System.out.println("Usage: register <username> <password> <email>");
                } else {
                    register(tokens[1], tokens[2], tokens[3]);
                    return State.LOGGED_IN;
                }
                break;
            case "login":
                if (tokens.length != 3) {
                    System.out.println("Usage: login <username> <password>");
                } else {
                    login(tokens[1], tokens[2]);
                    return State.LOGGED_IN;
                }
                break;
            case "quit":
                exitApplication();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
        return State.LOGGED_OUT;
    }

    private State handleLoggedInCommand(String command, String[] tokens) throws Exception {
        switch (command) {
            case "help":
                printPostLoginHelp();
                break;
            case "logout":
                logout();
                return State.LOGGED_OUT;
            case "create":
                if (tokens.length != 2) {
                    System.out.println("Usage: create <game_name>");
                } else {
                    createGame(tokens[1]);
                }
                break;
            case "list":
                listGames();
                break;
            case "join":
                if (tokens.length != 3) {
                    System.out.println("Usage: join <game_number> <white|black>");
                } else {
                    joinGame(tokens[1], tokens[2]);
                    return State.IN_GAME;
                }
                break;
            case "observe":
                if (tokens.length != 2) {
                    System.out.println("Usage: observe <game_number>");
                } else {
                    observeGame(tokens[1]);
                    return State.IN_GAME;
                }
                break;
            case "quit":
                exitApplication();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
        return State.LOGGED_IN;
    }

    private State handleInGameCommand(String command, String[] tokens) throws Exception {
        switch (command) {
            case "help":
                printInGameHelp();
                break;
            case "move":
                if (tokens.length != 3) {
                    System.out.println("Usage: move <start_position> <end_position>");
                } else {
                    makeMove(tokens[1], tokens[2]);
                }
                break;
            case "highlight":
                if (tokens.length != 2) {
                    System.out.println("Usage: highlight <position>");
                } else {
                    highlightLegalMoves(tokens[1]);
                }
                break;
            case "redraw":
                drawChessBoard(currentPerspective);
                break;
            case "leave":
                leaveGame();
                return State.LOGGED_IN;
            case "resign":
                resignGame();
                return State.LOGGED_IN;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
        return State.IN_GAME;
    }

    private void register(String username, String password, String email) throws Exception {
        authToken = serverFacade.register(username, password, email);
        System.out.println("Registered and logged in as " + authToken.getUsername());
    }

    private void login(String username, String password) throws Exception {
        authToken = serverFacade.login(username, password);
        System.out.println("Logged in as " + authToken.getUsername());
    }

    private void logout() throws Exception {
        serverFacade.logout(authToken.getToken());
        authToken = null;
        System.out.println("Logged out.");
    }

    private void createGame(String gameName) throws Exception {
        Game game = serverFacade.createGame(authToken.getToken(), gameName);
        System.out.println("Created game: " + game.getGameName());
    }

    private void listGames() throws Exception {
        List<Game> games = serverFacade.listGames(authToken.getToken());
        gameMap.clear();
        System.out.println("Available Games:");
        int index = 1;
        for (Game game : games) {
            String whitePlayer = game.getWhiteUsername() != null ? game.getWhiteUsername() : "None";
            String blackPlayer = game.getBlackUsername() != null ? game.getBlackUsername() : "None";
            System.out.printf("%d. %s - Players: White(%s) vs Black(%s)%n", index, game.getGameName(), whitePlayer, blackPlayer);
            gameMap.put(index, game);
            index++;
        }
    }

    private void joinGame(String gameNumberStr, String color) throws Exception {
        int gameNumber = parseGameNumber(gameNumberStr);
        Game game = gameMap.get(gameNumber);

        serverFacade.joinGame(authToken.getToken(), game.getGameID(), color.toLowerCase());
        System.out.println("Joined game: " + game.getGameName() + " as " + color.toUpperCase());

        currentPerspective = color.equalsIgnoreCase("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        currentGameID = game.getGameID();

        connectToWebSocket();

        // Send CONNECT command
        UserGameCommand connectCommand = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken.getToken(),
                currentGameID
        );
        webSocketClient.sendCommand(connectCommand);
    }

    private void observeGame(String gameNumberStr) throws Exception {
        int gameNumber = parseGameNumber(gameNumberStr);
        Game game = gameMap.get(gameNumber);

        System.out.println("Observing game: " + game.getGameName());
        currentPerspective = ChessGame.TeamColor.WHITE; // Default perspective
        currentGameID = game.getGameID();

        connectToWebSocket();

        // Send CONNECT command
        UserGameCommand connectCommand = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken.getToken(),
                currentGameID
        );
        webSocketClient.sendCommand(connectCommand);
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

    private void highlightLegalMoves(String posStr) {
        if (chessGame == null) {
            System.out.println("No game state available.");
            return;
        }
        try {
            ChessPosition position = parsePosition(posStr);
            Collection<ChessMove> moves = chessGame.validMoves(position);

            if (moves == null || moves.isEmpty()) {
                System.out.println("No valid moves for that piece.");
            } else {
                System.out.println("Valid moves for " + posStr + ":");
                for (ChessMove move : moves) {
                    System.out.println("  - " + formatPosition(move.getEndPosition()));
                }
            }
        } catch (Exception e) {
            System.out.println("Error highlighting moves: " + e.getMessage());
        }
    }

    private void leaveGame() throws Exception {
        UserGameCommand leaveCommand = new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                authToken.getToken(),
                currentGameID
        );
        webSocketClient.sendCommand(leaveCommand);
        webSocketClient.close();
        chessGame = null;
        currentGameID = -1;
        currentPerspective = null;
        System.out.println("Left the game.");
    }

    private void resignGame() throws Exception {
        UserGameCommand resignCommand = new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                authToken.getToken(),
                currentGameID
        );
        webSocketClient.sendCommand(resignCommand);
        webSocketClient.close();
        chessGame = null;
        currentGameID = -1;
        currentPerspective = null;
        System.out.println("You have resigned from the game.");
    }

    private void connectToWebSocket() {
        webSocketClient = new GameWebSocketClient(this);
        webSocketClient.connect("ws://localhost:8080/ws");
    }

    private int parseGameNumber(String gameNumberStr) throws Exception {
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

    private ChessPosition parsePosition(String posStr) throws Exception {
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

    private String formatPosition(ChessPosition position) {
        char column = (char) ('a' + position.getColumn() - 1);
        int row = position.getRow();
        return "" + column + row;
    }

    private void exitApplication() {
        System.out.println("Goodbye!");
        scanner.close();
        System.exit(0);
    }


    private void printPreLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  register <username> <password> <email> - Create a new account");
        System.out.println("  login <username> <password> - Log in to your account");
        System.out.println("  quit - Exit the application");
        System.out.println("  help - Show this help message");
    }

    private void printPostLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  create <game_name> - Create a new game");
        System.out.println("  list - List available games");
        System.out.println("  join <game_number> <white|black> - Join a game as a player");
        System.out.println("  observe <game_number> - Observe a game");
        System.out.println("  logout - Log out of your account");
        System.out.println("  quit - Exit the application");
        System.out.println("  help - Show this help message");
    }

    private void printInGameHelp() {
        System.out.println("Commands:");
        System.out.println("  move <start_position> <end_position> - Make a move (e.g., move e2 e4)");
        System.out.println("  highlight <position> - Show legal moves from a position (e.g., highlight e2)");
        System.out.println("  redraw - Redraw the chessboard");
        System.out.println("  leave - Leave the game");
        System.out.println("  resign - Resign from the game");
        System.out.println("  help - Show this help message");
    }

    private void drawChessBoard(ChessGame.TeamColor perspective) {
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
                ChessPosition position = new ChessPosition(row, displayCol);
                ChessPiece piece = board.getPiece(position);
                boolean isLightSquare = (row + displayCol) % 2 != 0;
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                String pieceStr = piece != null ? getPieceString(piece) : EscapeSequences.EMPTY;
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

    private String getPieceString(ChessPiece piece) {
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

    @Override
    public void handleLoadGame(LoadGameMessage message) {
        chessGame = message.getGame();
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