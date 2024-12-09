package websocket;


import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.*;


import javax.websocket.*;
import java.net.URI;

public class WebSocketFacade extends Endpoint{
    public Session session;

    public WebSocketFacade(GameMessageHandler gameHandler) throws Exception {
        URI uri = new URI("ws://localhost:8080/connect");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                switch (serverMessage.getServerMessageType()) {
                    case NOTIFICATION -> gameHandler.notify(new Gson().fromJson(message, NotificationMessage.class));
                    case LOAD_GAME -> gameHandler.loadGame(new Gson().fromJson(message, LoadGameMessage.class));
                    case ERROR -> gameHandler.error(new Gson().fromJson(message, ErrorMessage.class));
                }
            }
        });
    }

    @Override
    public void onOpen(javax.websocket.Session session, EndpointConfig endpointConfig) {}

    public void connect(String authToken, int gameID) throws Exception {
        var command = new ConnectCommand(authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        var command = new MakeMoveCommand(authToken, gameID, move);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void leave(String authToken, int gameID) throws Exception {
        var command = new LeaveCommand(authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    public void resign(String authToken, int gameID) throws Exception {
        var command = new ResignCommand(authToken, gameID);
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }
}
