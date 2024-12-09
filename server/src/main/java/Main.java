package server;

public class Main {
    public static void main(String[] args) throws Exception {
        var server = new server.Server();
        server.run(8080);
    }
}
