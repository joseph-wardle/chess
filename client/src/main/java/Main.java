import chess.*;
import client.ServerFacade;
import models.AuthToken;
import models.Game;

import java.util.Map;
import java.util.Scanner;

public class Main {
    private static ServerFacade serverFacade;
    private static Scanner scanner;
    private static AuthToken authToken;
    private static Map<Integer, Game> gameMap;

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
    }
}