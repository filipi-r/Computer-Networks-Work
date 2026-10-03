package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ServerConnection implements Runnable {
    private final Socket socket;
    private final Scanner in;
    private final ClientGameState gameState;
    private final TerminalUI ui;

    public ServerConnection(Socket socket, ClientGameState gameState, TerminalUI ui) throws IOException {
        this.socket = socket;
        this.in = new Scanner(socket.getInputStream());
        this.gameState = gameState;
        this.ui = ui;
    }

    @Override
    public void run() {
        try {
            while (in.hasNextLine()) {
                String mensagem = in.nextLine();

                // Atualiza o estado interno com base nas mensagens do protocolo
                gameState.atualizarEstado(mensagem);

                // Exibe a mensagem/mapa no terminal
                ui.exibirMensagem(mensagem);
            }
        } catch (Exception e) {
            ui.exibirMensagem("Conexão com o servidor perdida.");
        }
    }
}
