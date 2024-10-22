package handlers;

import dataaccess.UserAlreadyExistsException;
import services.UserService;
import models.User;
import models.AuthToken;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Handles user-related HTTP requests.
 */
public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     */
    public Object register(Request req, Response res) {
        try {
            User user = gson.fromJson(req.body(), User.class);
            AuthToken auth = userService.register(user);
            res.status(200);
            res.type("application/json");
            return gson.toJson(Map.of("username", user.getUsername(), "authToken", auth.getToken()));
        } catch (UserAlreadyExistsException e) {
            res.status(403);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Logs in an existing user.
     */
    public Object login(Request req, Response res) {
        try {
            User loginRequest = gson.fromJson(req.body(), User.class);
            AuthToken auth = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
            res.status(200);
            res.type("application/json");
            return gson.toJson(Map.of("username", loginRequest.getUsername(), "authToken", auth.getToken()));
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Logs out a user.
     */
    public Object logout(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            userService.logout(authToken);
            res.status(200);
            res.type("application/json");
            return gson.toJson(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }
}