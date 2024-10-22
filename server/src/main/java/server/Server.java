package server;

import handlers.UserHandler;
import handlers.GameHandler;
import handlers.ErrorHandler;
import services.UserService;
import services.GameService;
import services.AuthService;
import dataaccess.DataAccess;
import dataaccess.DataAccessImpl;
import com.google.gson.Gson;
import spark.Spark;

import java.util.Map;

public class Server {
    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private AuthService authService;
    private UserHandler userHandler;
    private GameHandler gameHandler;
    private ErrorHandler errorHandler;
    private Gson gson = new Gson();

    public Server() {
        this.dataAccess = new DataAccessImpl();
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.authService = new AuthService(dataAccess);
        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService, authService);
        this.errorHandler = new ErrorHandler();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        // Serve static files from "public" directory
        Spark.staticFiles.location("web");

        // Register exception handler
        Spark.exception(Exception.class, errorHandler::handleException);

        // Register not found handler
        Spark.notFound(errorHandler::handleNotFound);

        // User endpoints
        Spark.post("/user", userHandler::register); // Registration
        Spark.post("/session", userHandler::login); // Login
        Spark.delete("/session", userHandler::logout); // Logout

        // Game endpoints
        Spark.get("/game", gameHandler::listGames); // List Games
        Spark.post("/game", gameHandler::createGame); // Create Game
        Spark.put("/game", gameHandler::joinGame); // Join Game

        // Clear application data
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

        // Initialize Spark
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}