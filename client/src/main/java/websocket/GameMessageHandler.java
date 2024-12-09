package websocket;

import websocket.messages.*;

public interface GameMessageHandler {
    void notify(NotificationMessage message);
    void error(ErrorMessage message);
    void loadGame(LoadGameMessage message);
}
