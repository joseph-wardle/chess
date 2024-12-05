package websocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
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

    // TODO: Handler methods
}
