package server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 58901;
    private final LobbyManager lobbyManager = new LobbyManager();

    public void start() throws Exception {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("[SERVER] Rodando na porta " + PORT);
        while (true) {
            Socket socket = listener.accept();

            ClientHandler handler = new ClientHandler(socket, lobbyManager);
            Thread thread = new Thread(handler);
            thread.start();
        }

    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
