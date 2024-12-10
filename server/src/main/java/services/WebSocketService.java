package services;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import models.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketService {
    private static AuthService authService;
    private static GameService gameService;

    private static final Map<Integer, ChessGame> chessGames = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Session, String>> gameSessions = new ConcurrentHashMap<>();
    private static final Map<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

    private enum Role { WHITE, BLACK, OBSERVER }
    private static final Map<Session, Role> sessionRoles = new ConcurrentHashMap<>();

    public static void initialize(AuthService auth, GameService game) {
        authService = auth;
        gameService = game;
    }

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        Integer gameID = sessionToGame.remove(user);
        if (gameID != null) {
            String username = gameSessions.getOrDefault(gameID, Collections.emptyMap()).remove(user);
            Role role = sessionRoles.remove(user);

            if (username != null) {
                broadcastNotification(gameID, username + " left the game", null);
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session userSession, String message) {
        Gson gson = new Gson();
        UserGameCommand cmd = gson.fromJson(message, UserGameCommand.class);

        try {
            switch (cmd.getCommandType()) {
                case CONNECT -> handleConnectCommand(userSession, gson.fromJson(message, ConnectCommand.class));
                case MAKE_MOVE -> handleMakeMoveCommand(userSession, gson.fromJson(message, MakeMoveCommand.class));
                case LEAVE -> handleLeaveCommand(userSession, gson.fromJson(message, LeaveCommand.class));
                case RESIGN -> handleResignCommand(userSession, gson.fromJson(message, ResignCommand.class));
            }
        } catch (Exception e) {
            // Catch all exceptions and send ERROR message
            sendError(userSession, "Error: " + e.getMessage());
        }
    }

    @OnWebSocketError
    public void onError(Session user, Throwable t) {
    }

    private void handleResignCommand(Session userSession, ResignCommand resignCommand) throws DataAccessException {
        String authToken = resignCommand.getAuthToken();
        int gameID = resignCommand.getGameID();

        AuthToken auth = authService.authenticate(authToken);
        String username = auth.getUsername();

        Game game = gameService.getGame(gameID);
        ChessGame chessGame = chessGames.get(gameID);
        if (chessGame == null) {
            sendError(userSession, "Error: Game not found");
            return;
        }

        // Mark game as over
        chessGame.setGameOver(true);
        broadcastNotification(gameID, username + " resigned the game", null);

        // User stays connected as per the specs (?). Actually, it says "Does not cause the user to leave the game."
        // The user can still be in the session, just the game is over. They can still leave after if they want.
    }

    private void handleLeaveCommand(Session userSession, LeaveCommand leaveCommand) throws DataAccessException {
        String authToken = leaveCommand.getAuthToken();
        int gameID = leaveCommand.getGameID();

        AuthToken auth = authService.authenticate(authToken);
        String username = auth.getUsername();

        Game game = gameService.getGame(gameID);
        ChessGame chessGame = chessGames.get(gameID);
        if (chessGame == null) {
            sendError(userSession, "Error: Game not found");
            return;
        }

        Map<Session, String> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(userSession);
        }
        sessionToGame.remove(userSession);
        Role role = sessionRoles.remove(userSession);

        gameService.removePlayerFromGame(gameID, username);

        broadcastNotification(gameID, username + " left the game", null);
        userSession.close();
    }

    private void handleMakeMoveCommand(Session userSession, MakeMoveCommand makeMoveCommand) throws DataAccessException {
        String authToken = makeMoveCommand.getAuthToken();
        int gameID = makeMoveCommand.getGameID();
        ChessMove move = makeMoveCommand.getMove();

        AuthToken auth = authService.authenticate(authToken);
        String username = auth.getUsername();

        Game game = gameService.getGame(gameID);
        ChessGame chessGame = chessGames.get(gameID);
        if (chessGame == null) {
            sendError(userSession, "Error: Game not found");
            return;
        }

        // Check if user is a player in that game and if it's their turn
        Role role = sessionRoles.get(userSession);
        if (role == null || role == Role.OBSERVER) {
            sendError(userSession, "Error: Observers cannot make moves");
            return;
        }

        ChessGame.TeamColor turnColor = chessGame.getTeamTurn();
        boolean userIsWhite = (role == Role.WHITE);
        boolean userIsBlack = (role == Role.BLACK);

        if (userIsWhite && turnColor != ChessGame.TeamColor.WHITE) {
            sendError(userSession, "Error: It's not your turn (WHITE)");
            return;
        }

        if (userIsBlack && turnColor != ChessGame.TeamColor.BLACK) {
            sendError(userSession, "Error: It's not your turn (BLACK)");
            return;
        }

        // Attempt the move
        try {
            chessGame.makeMove(move);
        } catch (InvalidMoveException e) {
            sendError(userSession, "Error: " + e.getMessage());
            return;
        }

        // Move successful, update database game if needed
        // (For testing, may not need to persist moves to DB)

        // Send LOAD_GAME to all
        broadcastToAll(gameID, new LoadGameMessage(chessGame));

        // Notify others about the move
        broadcastNotification(gameID, username + " made a move: " + formatMove(move), null);

        // Check for check, checkmate, stalemate
        ChessGame.TeamColor opponentColor = (turnColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        if (chessGame.isInCheck(opponentColor)) {
            if (chessGame.isInCheckmate(opponentColor)) {
                // Checkmate
                broadcastNotification(gameID, "Checkmate! " + username + " has won!", null);
                chessGame.setGameOver(true);
            } else if (chessGame.isInStalemate(opponentColor)) {
                // Stalemate
                broadcastNotification(gameID, "Stalemate! The game is drawn.", null);
                chessGame.setGameOver(true);
            } else {
                // Just check
                String checkedPlayer = (opponentColor == ChessGame.TeamColor.WHITE) ? game.getWhiteUsername() : game.getBlackUsername();
                broadcastNotification(gameID, checkedPlayer + " is in check", null);
            }
        } else if (chessGame.isInStalemate(opponentColor)) {
            // Stalemate with no check
            broadcastNotification(gameID, "Stalemate! The game is drawn.", null);
            chessGame.setGameOver(true);
        }
    }

    private void handleConnectCommand(Session userSession, ConnectCommand connectCommand) throws DataAccessException {
        String authToken = connectCommand.getAuthToken();
        int gameID = connectCommand.getGameID();

        // Authenticate
        AuthToken auth = authService.authenticate(authToken);
        String username = auth.getUsername();

        Game game = gameService.getGame(gameID); // Throws DataAccessException if not found

        // Determine role
        Role role;
        boolean isWhite = username.equals(game.getWhiteUsername());
        boolean isBlack = username.equals(game.getBlackUsername());
        if (isWhite) {
            role = Role.WHITE;
        } else if (isBlack) {
            role = Role.BLACK;
        } else {
            role = Role.OBSERVER;
        }

        // Load or create ChessGame in memory
        ChessGame chessGame = chessGames.get(gameID);
        if (chessGame == null) {
            // Create a new chess game if not exists
            chessGame = new ChessGame();
            chessGames.put(gameID, chessGame);
        }

        // Add session to maps
        gameSessions.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(userSession, username);
        sessionToGame.put(userSession, gameID);
        sessionRoles.put(userSession, role);

        // Send LOAD_GAME to this user
        sendMessage(userSession, new LoadGameMessage(chessGame));

        // Notify others that a user connected
        if (role == Role.WHITE) {
            broadcastNotification(gameID, username + " connected as white", userSession);
        } else if (role == Role.BLACK) {
            broadcastNotification(gameID, username + " connected as black", userSession);
        } else {
            broadcastNotification(gameID, username + " connected as an observer", userSession);
        }
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(new Gson().toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError(Session session, String errorMsg) {
        sendMessage(session, new ErrorMessage(errorMsg));
    }

    private void broadcastToAll(int gameID, ServerMessage message) {
        Map<Session, String> sessions = gameSessions.get(gameID);
        if (sessions == null) return;
        for (Session s : sessions.keySet()) {
            sendMessage(s, message);
        }
    }

    private void broadcastNotification(int gameID, String notifMessage, Session exclude) {
        Map<Session, String> sessions = gameSessions.get(gameID);
        if (sessions == null) return;
        NotificationMessage notification = new NotificationMessage(notifMessage);
        for (Session s : sessions.keySet()) {
            if (s != exclude && s.isOpen()) {
                sendMessage(s, notification);
            }
        }
    }

    private String formatMove(ChessMove move) {
        return posToStr(move.getStartPosition()) + " -> " + posToStr(move.getEndPosition());
    }

    private String posToStr(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        int row = pos.getRow();
        return "" + col + row;
    }
}