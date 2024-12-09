package ui;

import chess.ChessGame;
import client.ServerFacade;
import models.AuthToken;
import models.Game;
import websocket.GameMessageHandler;
import websocket.WebSocketFacade;
import websocket.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UIController implements GameMessageHandler {
    enum State {
        LOGGED_OUT, LOGGED_IN, IN_GAME
    }

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade serverFacade;

    private AuthToken authToken;
    private Map<Integer, Game> gameMap = new HashMap<>();
    private ChessGame chessGame;
    private ChessGame.TeamColor currentPerspective;
    private int currentGameID = -1;

    private WebSocketFacade webSocketFacade;
    private State currentState = State.LOGGED_OUT;

    private final CommandHandler commandHandler;

    public UIController() {
        this.serverFacade = new ServerFacade(8080);
        this.commandHandler = new CommandHandler(this, serverFacade);
    }

    public void run() {
        System.out.println("Welcome to CS240 Chess! Type 'help' to get started.");

        while (true) {
            System.out.print("[" + currentState + "] >>> ");
            String inputLine = Scanner.nextLine().trim();
            if (inputLine.isEmpty()) {
                continue;
            }

            String[] tokens = inputLine.split("\\s+");
            String command = tokens[0].toLowerCase();

            try {
                switch (currentState) {
                    case LOGGED_OUT -> handleLoggedOutCommand(command, tokens);
                    case LOGGED_IN -> handleLoggedInCommand(command, tokens);
                    case IN_GAME -> handleInGameCommand(command, tokens);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ----- State Handling -----

    private void HandleLoggedOutCommand(String command, String[] tokens) throws Exception {
        switch (command) {
            case "help" -> helpPrinter.printPreLoginHelp();
            case "register" -> {
                if (tokens.length == 4) {
                    commandHandler.register(tokens[1], tokens[2], tokens[3]);
                    currentState = State.LOGGED_IN;
                } else {
                    System.out.println("Usage: register <username> <password> <email>");
                }
            }
            case "login" -> {
                if (tokens.length == 3) {
                    commandHandler.login(tokens[1], tokens[2]);
                    currentState = State.LOGGED_IN;
                } else {
                    System.out.println("Usage: login <username> <password>");
                }
            }
            case "quit" -> commandHandler.exitApplication();
            default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
        }
    }

    private void handleLoggedInCommand(String command, String[] tokens) throws Exception {
        switch (command) {
            case "help" -> HelpPrinter.printPostLoginHelp();
            case "logout" -> {
                commandHandler.logout();
                currentState = State.LOGGED_OUT;
            }
            case "create" -> {
                if (tokens.length == 2) {
                    commandHandler.createGame(tokens[1]);
                } else {
                    System.out.println("Usage: create <game_name>");
                }
            }
            case "list" -> commandHandler.listGames();
            case "join" -> {
                if (tokens.length == 3) {
                    commandHandler.joinGame(tokens[1], tokens[2]);
                    currentState = State.IN_GAME;
                } else {
                    System.out.println("Usage: join <game_number> <white|black>");
                }
            }
            case "observe" -> {
                if (tokens.length == 2) {
                    commandHandler.observeGame(tokens[1]);
                    currentState = State.IN_GAME;
                } else {
                    System.out.println("Usage: observe <game_number>");
                }
            }
            case "quit" -> commandHandler.exitApplication();
            default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private void handleInGameCommand(String command, String[] tokens) throws Exception {
        switch (command) {
            case "help" -> HelpPrinter.printInGameHelp();
            case "move" -> {
                if (tokens.length == 3) {
                    commandHandler.makeMove(tokens[1], tokens[2]);
                } else {
                    System.out.println("Usage: move <start_position> <end_position>");
                }
            }
            case "highlight" -> {
                if (tokens.length == 2) {
                    commandHandler.highlightLegalMoves(tokens[1]);
                } else {
                    System.out.println("Usage: highlight <position>");
                }
            }
            case "redraw" -> BoardRenderer.drawChessBoard(chessGame, currentPerspective);
            case "leave" -> {
                commandHandler.leaveGame();
                currentState = State.LOGGED_IN;
            }
            case "resign" -> {
                commandHandler.resignGame();
                currentState = State.LOGGED_IN;
            }
            default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    // ----- GameMessageHandler methods -----

    @Override
    public void loadGame(LoadGameMessage message) {
        chessGame = message.getGame();
        BoardRenderer.drawChessBoard(chessGame, currentPerspective);
    }

    @Override
    public void notify(NotificationMessage message) {
        System.out.println("Notification: " + message.getMessage());
    }

    @Override
    public void error(ErrorMessage message) {
        System.err.println("Error: " + message.getErrorMessage());
    }

    // ----- Getter/Setter Methods for State -----

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setGameMap(Map<Integer, Game> gameMap) {
        this.gameMap = gameMap;
    }

    public Map<Integer, Game> getGameMap() {
        return gameMap;
    }

    public void setChessGame(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public ChessGame getChessGame() {
        return chessGame;
    }

    public void setCurrentPerspective(ChessGame.TeamColor perspective) {
        this.currentPerspective = perspective;
    }

    public ChessGame.TeamColor getCurrentPerspective() {
        return currentPerspective;
    }

    public void setCurrentGameID(int gameID) {
        this.currentGameID = gameID;
    }

    public int getCurrentGameID() {
        return currentGameID;
    }

    public void setWebSocketFacade(WebSocketFacade facade) {
        this.webSocketFacade = facade;
    }

    public WebSocketFacade getWebSocketFacade() {
        return webSocketFacade;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public ServerFacade getServerFacade() {
        return serverFacade;
    }
}
