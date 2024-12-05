package server;

import dataaccess.DataAccessException;
import dataaccess.DataAccessMySQLImpl;
import handlers.UserHandler;
import handlers.GameHandler;
import handlers.ErrorHandler;
import services.UserService;
import services.GameService;
import services.AuthService;
import dataaccess.DataAccess;
import dataaccess.DataAccessImpl;
import com.google.gson.Gson;
import spark.Session;
import spark.Spark;
import websocket.GameWebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main server class that initializes and runs the Spark server.
 */
public class Server {
    private final DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;
    private final AuthService authService;
    private final UserHandler userHandler;
    private final GameHandler gameHandler;
    private final ErrorHandler errorHandler;
    private final Gson gson = new Gson();
    public static ConcurrentHashMap<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    public Server() {
        try {
            this.dataAccess = new DataAccessMySQLImpl();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize DataAccess layer: " + e.getMessage(), e);
        }
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.authService = new AuthService(dataAccess);
        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService, authService);
        this.errorHandler = new ErrorHandler();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.exception(Exception.class, errorHandler::handleException);

        Spark.notFound(errorHandler::handleNotFound);

        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);

        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        Spark.delete("/db", (req, res) -> {
            try {
                userService.clearData();
                res.status(200);
                res.type("application/json");
                return gson.toJson(Map.of("message", "All data cleared"));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
            }
        });

        Spark.webSocket("/connect", GameWebSocket.class);

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}