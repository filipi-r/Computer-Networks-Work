package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 58901;
    private final LobbyManager lobbyManager = new LobbyManager();

    public void start() throws Exception {
        try (var listener = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Rodando na porta " + PORT);

            try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    Socket socket = listener.accept();
                    // Instancia o Handler repassando o gerenciador de lobbies
                    ClientHandler handler = new ClientHandler(socket, lobbyManager);
                    pool.execute(handler); // cria thread para jogador enviar e receber msgs
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().start();
    }
}
