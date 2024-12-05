package handlers;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;
import spark.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

import java.io.IOException;

@WebSocket
public class WebsocketHandler {

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        Server.gameSessions.put(session, 0);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
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

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Server.gameSessions.remove(session);
    }

    private void handleConnect(UserGameCommand command, Session session) throws IOException {
        // TODO
    }

    private void handleMakeMove(UserGameCommand command, Session session) throws IOException {
        // TODO
    }

    private void handleLeave(UserGameCommand command, Session session) throws IOException {
        // TODO
    }

    private void handleResign(UserGameCommand command, Session session) throws IOException {
        // TODO
    }

    private void sendError(javax.websocket.Session session, String errorMessage) {
        ErrorMessage error = new ErrorMessage(errorMessage);
        try {
            session.getBasicRemote().sendText(new Gson().toJson(error));
        } catch (Exception e) {
            System.err.println("Failed to send error message to session " + session.getId() + ": " + e.getMessage());
        }
    }
}
