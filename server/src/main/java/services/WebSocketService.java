package services;

import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketService {
    private static AuthService authService;
    private static GameService gameService;

    private static final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    private static final Map<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionUserMap = new ConcurrentHashMap<>();

    public static void initialize(AuthService auth, GameService game) {
        authService = auth;
        gameService = game;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket opened: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketMessage

}
