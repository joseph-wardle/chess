package ui;

public class HelpPrinter {
    public static void printPreLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  register <username> <password> <email> - Create a new account");
        System.out.println("  login <username> <password> - Log in to your account");
        System.out.println("  quit - Exit the application");
        System.out.println("  help - Show this help message");
    }

    public static void printPostLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  create <game_name> - Create a new game");
        System.out.println("  list - List available games");
        System.out.println("  join <game_number> <white|black> - Join a game as a player");
        System.out.println("  observe <game_number> - Observe a game");
        System.out.println("  logout - Log out of your account");
        System.out.println("  quit - Exit the application");
        System.out.println("  help - Show this help message");
    }

    public static void printInGameHelp() {
        System.out.println("Commands:");
        System.out.println("  move <start_position> <end_position> - Make a move (e.g., move e2 e4)");
        System.out.println("  highlight <position> - Show legal moves from a position (e.g., highlight e2)");
        System.out.println("  redraw - Redraw the chessboard");
        System.out.println("  leave - Leave the game");
        System.out.println("  resign - Resign from the game");
        System.out.println("  help - Show this help message");
    }
}