package websocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import models.AuthToken;
import models.Game;
import server.Server;
import services.AuthService;
import services.GameService;
import dataaccess.DataAccessException;
import dataaccess.DataAccessMySQLImpl;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

@ServerEndpoint("/ws")
public class GameWebSocket {
    private static Map<Integer, Set<Session>> gameSessions = new HashMap<>();
    private static Map<Session, Integer> sessionGameMap = new HashMap<>();
    private static Map<Session, String> sessionUserMap = new HashMap<>();
    private static Gson gson = new Gson();

    private static AuthService authService;
    private static GameService gameService;

    static {
        try {
            var dataAccess = new DataAccessMySQLImpl();
            authService = new AuthService(dataAccess);
            gameService = new GameService(dataAccess);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        Server.gameSessions.put(session, 0);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(command, session);
                    break;
                case MAKE_MOVE:
                    handleMakeMove(command, session);
                    break;
                case LEAVE:
                    handleLeave(command, session);
                    break;
                case RESIGN:
                    handleResign(command, session);
                    break;
                default:
                    sendError(session, "Invalid command type.");
            }
        } catch (Exception e) {
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, null, sessionGameMap.get(session));
        handleLeave(command, session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error on session " + session.getId() + ": " + throwable.getMessage());
    }

    private void handleConnect(UserGameCommand command, Session session) {
        try {
            AuthToken auth = authService.authenticate(command.getAuthToken());
            int gameId = command.getGameID();

            // Add session to gameSessions
            gameSessions.computeIfAbsent(gameId, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
            sessionGameMap.put(session, gameId);
            sessionUserMap.put(session, auth.getUsername());

            // Send LOAD_GAME message to root client
            Game game = gameService.getGame(gameId);
            ChessGame chessGame = new ChessGame(); // Load actual game state
            LoadGameMessage loadGameMessage = new LoadGameMessage(chessGame);
            session.getBasicRemote().sendText(gson.toJson(loadGameMessage));

            // Notify others
            String notificationText = auth.getUsername() + " connected to the game as " +
                    (game.getWhiteUsername().equals(auth.getUsername()) ? "white" : "black");
            NotificationMessage notification = new NotificationMessage(notificationText);
            broadcastToGame(gameId, notification, session);
        } catch (Exception e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleResign(UserGameCommand command, Session session) {
        try {
            AuthToken auth = authService.authenticate(command.getAuthToken());
            int gameId = command.getGameID();

            // Retrieve the current ChessGame state
            ChessGame chessGame = getChessGameFromStorage(gameId);
            if (chessGame == null) {
                sendError(session, "Game state not found.");
                return;
            }

            // Mark the game as over
            chessGame.setGameOver(true); // You need to add a 'gameOver' field in ChessGame class

            // Update game state in storage
            saveChessGameToStorage(gameId, chessGame);

            // Notify all clients
            String notificationText = auth.getUsername() + " has resigned.";
            NotificationMessage notification = new NotificationMessage(notificationText);
            broadcastToGame(gameId, notification, null);
        } catch (Exception e) {
            sendError(session, "Error processing resign: " + e.getMessage());
        }
    }

    private void handleLeave(UserGameCommand command, Session session) {
        try {
            AuthToken auth = authService.authenticate(command.getAuthToken());
            int gameId = command.getGameID();

            // Remove session from gameSessions
            Set<Session> sessions = gameSessions.get(gameId);
            if (sessions != null) {
                sessions.remove(session);
            }
            sessionGameMap.remove(session);
            sessionUserMap.remove(session);

            Game game = gameService.getGame(gameId);

            // If the user is a player, remove them from the game
            boolean isPlayer = false;
            if (game.getWhiteUsername() != null && game.getWhiteUsername().equals(auth.getUsername())) {
                game.setWhiteUsername(null);
                isPlayer = true;
            }
            if (game.getBlackUsername() != null && game.getBlackUsername().equals(auth.getUsername())) {
                game.setBlackUsername(null);
                isPlayer = true;
            }
            if (isPlayer) {
                gameService.updateGame(game);
            }

            // Notify others
            String notificationText = auth.getUsername() + " left the game.";
            NotificationMessage notification = new NotificationMessage(notificationText);
            broadcastToGame(gameId, notification, session);
        } catch (Exception e) {
            sendError(session, "Error processing leave: " + e.getMessage());
        }
    }

    private void handleMakeMove(UserGameCommand command, Session session) {
        try {
            AuthToken auth = authService.authenticate(command.getAuthToken());
            int gameId = command.getGameID();
            ChessMove move = command.getMove();

            Game game = gameService.getGame(gameId);
            if (game == null) {
                sendError(session, "Game not found.");
                return;
            }

            ChessGame chessGame = getChessGameFromStorage(gameId);
            if (chessGame == null) {
                sendError(session, "Game state not found.");
                return;
            }

            String currentPlayer = chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE ? game.getWhiteUsername() : game.getBlackUsername();
            if (!auth.getUsername().equals(currentPlayer)) {
                sendError(session, "It's not your turn.");
                return;
            }

            chessGame.makeMove(move);

            saveChessGameToStorage(gameId, chessGame);

            LoadGameMessage loadGameMessage = new LoadGameMessage(chessGame);
            broadcastToGame(gameId, loadGameMessage, null);

            String moveDescription = auth.getUsername() + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
            NotificationMessage notification = new NotificationMessage(moveDescription);
            broadcastToGame(gameId, notification, session);

            if (chessGame.isInCheck(chessGame.getTeamTurn())) {
                NotificationMessage checkNotification = new NotificationMessage(currentPlayer + " is in check.");
                broadcastToGame(gameId, checkNotification, null);
            }
            if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
                NotificationMessage checkmateNotification = new NotificationMessage(currentPlayer + " is in checkmate.");
                broadcastToGame(gameId, checkmateNotification, null);
            }
            if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
                NotificationMessage stalemateNotification = new NotificationMessage("Game is in stalemate.");
                broadcastToGame(gameId, stalemateNotification, null);
            }
        } catch (Exception e) {
            sendError(session, "Error processing move: " + e.getMessage());

        }
    }


    // Helper methods to get and save ChessGame state
    private ChessGame getChessGameFromStorage(int gameId) {
        // TODO
        return null;
    }

    private void saveChessGameToStorage(int gameId, ChessGame chessGame) {
        // TODO
    }

    private void sendError(Session session, String errorMessage) {
        ErrorMessage error = new ErrorMessage(errorMessage);
        try {
            session.getBasicRemote().sendText(gson.toJson(error));
        } catch (Exception e) {
            System.err.println("Failed to send error message to session " + session.getId() + ": " + e.getMessage());
        }
    }

    private void broadcastToGame(int gameId, ServerMessage message, Session excludeSession) {
        Set<Session> sessions = gameSessions.get(gameId);
        if (sessions != null) {
            String jsonMessage = gson.toJson(message);
            for (Session s : sessions) {
                if (excludeSession == null || !s.equals(excludeSession)) {
                    try {
                        s.getBasicRemote().sendText(jsonMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
