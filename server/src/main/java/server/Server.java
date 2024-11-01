package server;

import handlers.UserHandler;
import handlers.GameHandler;
import handlers.ErrorHandler;
import services.UserService;
import services.GameService;
import services.AuthService;
import dataaccess.DataAccess;
import dataaccess.DataAccessImpl;
import dataaccess.DataAccessMySQLImpl;
import com.google.gson.Gson;
import spark.Spark;

import java.util.Map;

/**
 * Main server class that initializes and runs the Spark server.
 */
public class Server {
    private final DataAccess dataAccess;
    private final  UserService userService;
    private final  GameService gameService;
    private final  AuthService authService;
    private final  UserHandler userHandler;
    private final  GameHandler gameHandler;
    private final  ErrorHandler errorHandler;
    private final  Gson gson = new Gson();

    public Server() throws Exception {
        this.dataAccess = new DataAccessMySQLImpl();
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

        Spark.exception(Exception.class, (e, req, res) -> {
            Object response = errorHandler.handleException(e, req, res);
            res.body(response.toString());
        });

        Spark.notFound((req, res) -> {
            Object response = errorHandler.handleNotFound(req, res);
            res.body(response.toString());
            return res.body();
        });

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

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}