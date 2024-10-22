package handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Handles errors and not-found routes.
 */
public class ErrorHandler {
    private final Gson gson = new Gson();

    /**
     * Handles general exceptions.
     */
    public Object handleException(Exception e, Request req, Response res) {
        res.type("application/json");
        res.status(500);
        return generateErrorResponse("Error: " + e.getMessage());
    }

    /**
     * Handles 404 Not Found errors.
     */
    public Object handleNotFound(Request req, Response res) {
        res.type("application/json");
        res.status(404);
        String message = String.format("[%s] %s not found", req.requestMethod(), req.pathInfo());
        return generateErrorResponse("Error: " + message);
    }

    /**
     * Generates a standardized error response.
     */
    private String generateErrorResponse(String message) {
        return gson.toJson(Map.of("message", message, "success", false));
    }
}