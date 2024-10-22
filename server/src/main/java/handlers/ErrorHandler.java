package handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.Map;

public class ErrorHandler {
    private Gson gson = new Gson();

    public Object handleException(Exception e, Request req, Response res) {
        res.type("application/json");
        res.status(500);
        return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
    }

    public Object handleNotFound(Request req, Response res) {
        res.type("application/json");
        res.status(404);
        String message = String.format("[%s] %s not found", req.requestMethod(), req.pathInfo());
        return gson.toJson(Map.of("message", "Error: " + message, "success", false));
    }
}