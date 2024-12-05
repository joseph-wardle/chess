package websocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

import chess.ChessGame;
import com.google.gson.Gson;
import models.AuthToken;
import models.Game;
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
        // No action needed on open
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
        handleLeave(session);
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

    private void sendError(Session session, String errorMessage) {
        ErrorMessage error = new ErrorMessage(errorMessage);
        try {
            session.getBasicRemote().sendText(gson.toJson(error));
        } catch (Exception e) {
            System.err.println("Failed to send error message to session " + session.getId() + ": " + e.getMessage());
        }
    }
}
