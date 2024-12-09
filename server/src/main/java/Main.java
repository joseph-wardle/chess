package server;

import server.Server;
import services.WebSocketService;

public class Main {
    public static void main(String[] args) throws Exception {
        var server = new Server();
        server.run(8080);

        WebSocketService.initialize(server.authService, server.gameService);

        System.out.println("Server started on port 8080");
    }
}
