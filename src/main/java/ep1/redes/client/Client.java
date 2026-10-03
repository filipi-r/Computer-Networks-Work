package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 58901;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner teclado = new Scanner(System.in);

            ClientGameState gameState = new ClientGameState();
            TerminalUI ui = new TerminalUI();

            // 1. Inicia a Thread para escutar o servidor em segundo plano
            ServerConnection conexao = new ServerConnection(socket, gameState, ui);
            new Thread(conexao).start();

            // 2. Loop principal da Thread MAIN: lê o teclado e envia pro servidor
            while (teclado.hasNextLine()) {
                String comando = teclado.nextLine();
                out.println(comando); // Envia a String diretamente pelo Socket
            }

        } catch (IOException e) {
            System.err.println("Não foi possível conectar ao servidor: " + e.getMessage());
        }
    }
}
