package ui;

import chess.ChessMove;
import chess.ChessPosition;
import client.ServerFacade;
import models.AuthToken;
import models.Game;
import websocket.WebSocketFacade;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    private final UIController controller;
    private final ServerFacade serverFacade;

    public CommandHandler(UIController controller, ServerFacade serverFacade) {
        this.controller = controller;
        this.serverFacade = serverFacade;
    }

    public void register(String username, String password, String email) throws Exception {
        AuthToken auth = serverFacade.register(username, password, email);
        controller.setAuthToken(auth);
        System.out.println("Registered and logged in as " + auth.getUsername());
    }

    public void login(String username, String password) throws Exception {
        AuthToken auth = serverFacade.login(username, password);
        controller.setAuthToken(auth);
        System.out.println("Logged in as " + auth.getUsername());
    }

    public void logout() throws Exception {
        serverFacade.logout(controller.getAuthToken().getToken());
        controller.setAuthToken(null);
        System.out.println("Logged out.");
    }

    public void createGame(String gameName) throws Exception {
        var authToken = controller.getAuthToken().getToken();
        Game game = serverFacade.createGame(authToken, gameName);
        System.out.println("Created game: " + game.getGameName());
    }

    public void listGames() throws Exception {
        var authToken = controller.getAuthToken().getToken();
        List<Game> games = serverFacade.listGames(authToken);
        Map<Integer, Game> gameMap = new HashMap<>();
        System.out.println("Available Games:");
        int index = 1;
        for (Game game : games) {
            String whitePlayer = game.getWhiteUsername() != null ? game.getWhiteUsername() : "None";
            String blackPlayer = game.getBlackUsername() != null ? game.getBlackUsername() : "None";
            System.out.printf("%d. %s - Players: White(%s) vs Black(%s)%n", index, game.getGameName(), whitePlayer, blackPlayer);
            gameMap.put(index, game);
            index++;
        }
        controller.setGameMap(gameMap);
    }

    public void joinGame(String gameNumberStr, String color) throws Exception {
        int gameNumber = UIUtils.parseGameNumber(gameNumberStr, controller.getGameMap());
        Game game = controller.getGameMap().get(gameNumber);

        serverFacade.joinGame(controller.getAuthToken().getToken(), game.getGameID(), color.toLowerCase());
        System.out.println("Joined game: " + game.getGameName() + " as " + color.toUpperCase());

        controller.setCurrentPerspective(color.equalsIgnoreCase("white") ?
                chess.ChessGame.TeamColor.WHITE : chess.ChessGame.TeamColor.BLACK);
        controller.setCurrentGameID(game.getGameID());

        WebSocketFacade webSocketFacade = new WebSocketFacade(controller);
        webSocketFacade.connect(controller.getAuthToken().getToken(), controller.getCurrentGameID());
        controller.setWebSocketFacade(webSocketFacade);
    }

    public void observeGame(String gameNumberStr) throws Exception {
        int gameNumber = UIUtils.parseGameNumber(gameNumberStr, controller.getGameMap());
        Game game = controller.getGameMap().get(gameNumber);

        System.out.println("Observing game: " + game.getGameName());
        controller.setCurrentPerspective(chess.ChessGame.TeamColor.WHITE);
        controller.setCurrentGameID(game.getGameID());

        WebSocketFacade webSocketFacade = new WebSocketFacade(controller);
        webSocketFacade.connect(controller.getAuthToken().getToken(), controller.getCurrentGameID());
        controller.setWebSocketFacade(webSocketFacade);
    }

    public void makeMove(String startPosStr, String endPosStr) {
        try {
            ChessPosition start = UIUtils.parsePosition(startPosStr);
            ChessPosition end = UIUtils.parsePosition(endPosStr);
            ChessMove move = new ChessMove(start, end);

            controller.getWebSocketFacade().makeMove(controller.getAuthToken().getToken(), controller.getCurrentGameID(), move);
        } catch (Exception e) {
            System.out.println("Error making move: " + e.getMessage());
        }
    }

    public void highlightLegalMoves(String posStr) {
        var chessGame = controller.getChessGame();
        if (chessGame == null) {
            System.out.println("No game state available.");
            return;
        }
        try {
            ChessPosition position = UIUtils.parsePosition(posStr);
            Collection<ChessMove> moves = chessGame.validMoves(position);

            if (moves == null || moves.isEmpty()) {
                System.out.println("No valid moves for that piece.");
            } else {
                System.out.println("Valid moves for " + posStr + ":");
                for (ChessMove move : moves) {
                    System.out.println("  - " + UIUtils.formatPosition(move.getEndPosition()));
                }
            }
        } catch (Exception e) {
            System.out.println("Error highlighting moves: " + e.getMessage());
        }
    }

    public void leaveGame() throws Exception {
        controller.getWebSocketFacade().leave(controller.getAuthToken().getToken(), controller.getCurrentGameID());
        controller.getWebSocketFacade().session.close();

        controller.setChessGame(null);
        controller.setCurrentGameID(-1);
        controller.setCurrentPerspective(null);
        System.out.println("Left the game.");
    }

    public void resignGame() throws Exception {
        controller.getWebSocketFacade().resign(controller.getAuthToken().getToken(), controller.getCurrentGameID());
        controller.getWebSocketFacade().session.close();

        controller.setChessGame(null);
        controller.setCurrentGameID(-1);
        controller.setCurrentPerspective(null);
        System.out.println("You have resigned from the game.");
    }

    public void exitApplication() {
        System.out.println("Goodbye!");
        controller.getScanner().close();
        System.exit(0);
    }
}