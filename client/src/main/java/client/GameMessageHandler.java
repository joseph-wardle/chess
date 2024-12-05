package client;

import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

public interface GameMessageHandler {
    void handleLoadGame(LoadGameMessage message);
    void handleNotification(NotificationMessage message);
    void handleError(ErrorMessage message);
}