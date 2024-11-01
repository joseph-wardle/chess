import server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        var server = new Server();
        server.run(8080);

    }
}
