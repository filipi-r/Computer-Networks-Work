package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Scanner input;
    private final PrintWriter output;
    private final LobbyManager lobbyManager;

    private GameSession gameSession;
    private boolean ready = false;

    public ClientHandler(Socket socket, LobbyManager lobbyManager) throws IOException {
        this.socket = socket;
        this.lobbyManager = lobbyManager;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try (socket) {
            lobbyManager.entrarNoLobby(this);

            while (input.hasNextLine()) {
                String mensagem = input.nextLine();
                processarMensagem(mensagem);
            }
        } catch (Exception e) {
            System.out.println("[ClientHandler] Cliente desconectado.");
        } finally {
            if (gameSession != null) {
                gameSession.removerJogador(this);
            }
        }
    }

    private void processarMensagem(String msg) {
        // FASE 1: Aguardando 'OK' de ambos no Lobby
        if (msg.equalsIgnoreCase("OK")) {
            if (!this.ready) {
                this.ready = true;

                // Dispara a checagem primeiro
                gameSession.verificarProntidao();

                // Só envia a mensagem de espera se AINDA NÃO tiver entrado na fase de seleção
                if (!gameSession.isEmFaseSelecao()) {
                    send("YOU_ARE_READY: Aguardando o outro jogador...");
                }
            }
        }
        // FASE 2: Ambos já deram 'OK' -> Escolha de Posição
        else if (msg.startsWith("SELECT ") && gameSession.isEmFaseSelecao()) {
            String[] partes = msg.split(" ");
            int x = Integer.parseInt(partes[1]);
            int y = Integer.parseInt(partes[2]);

            gameSession.processarEscolhaPosicao(this, x, y);
        }
        // FASE 3: Partida em Andamento -> Movimentos e Ações
        else if (msg.startsWith("MOVE ") && gameSession.isEmJogo()) {
            gameSession.processarMovimento(this, msg);
        }
    }

    public void send(String msg) { output.println(msg); }
    public boolean isReady() { return ready; }
    public void setGameSession(GameSession session) { this.gameSession = session; }
    public GameSession getGameSession() { return gameSession; }
}
