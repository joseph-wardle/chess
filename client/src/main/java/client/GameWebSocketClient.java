package client;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

@ClientEndpoint
public class GameWebSocketClient {
    private Session session;
    private Gson gson = new Gson();
    private GameMessageHandler messageHandler;

    public GameWebSocketClient(GameMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void connect(String uri) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, new URI(uri));
        } catch (Exception e) {
            System.err.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
                messageHandler.handleLoadGame(loadGameMessage);
                break;
            case NOTIFICATION:
                NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                messageHandler.handleNotification(notificationMessage);
                break;
            case ERROR:
                ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                messageHandler.handleError(errorMessage);
                break;
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("Disconnected from WebSocket server.");
    }

    @OnError
    public void onError(Throwable t) {
        System.err.println("WebSocket error: " + t.getMessage());
    }

    public void close() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                System.err.println("Failed to close WebSocket session: " + e.getMessage());
            }
        }
    }

    public void sendCommand(websocket.commands.UserGameCommand command) {
        try {
            String message = gson.toJson(command);
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            System.err.println("Failed to send command: " + e.getMessage());
        }
    }
}