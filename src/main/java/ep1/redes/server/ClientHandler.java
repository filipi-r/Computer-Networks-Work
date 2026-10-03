package server;

import common.protocol.MessageParser;
import common.protocol.MessageSerializer;
import common.protocol.message_types.*;

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
    private String name = "Jogador"; // <-- VARIÁVEL ADICIONADA AQUI

    public ClientHandler(Socket socket, LobbyManager lobbyManager) throws IOException {
        this.socket = socket;
        this.lobbyManager = lobbyManager;
        this.input = new Scanner(socket.getInputStream());
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try (socket) {
            lobbyManager.enterLobby(this);

            while (input.hasNextLine()) {
                String rawMsg = input.nextLine();
                if (rawMsg.isBlank()) continue;

                try {
                    Message message = MessageParser.parse(rawMsg);
                    processarMensagem(message);
                } catch (Exception e) {
                    send(new ErrorMessage("Mensagem inválida ou malformada: " + e.getMessage()));
                }
            }
        } catch (Exception e) {
            System.out.println("[ClientHandler] Cliente desconectado: " + name);
        } finally {
            if (gameSession != null) {
                gameSession.removePlayer(this);
            }
        }
    }

    private void processarMensagem(Message msg) {
        switch (msg.type()) {
            case JOIN -> {
                JoinMessage joinMsg = (JoinMessage) msg;
                this.name = joinMsg.playerName();
                System.out.println("[ClientHandler] Jogador nomeado: " + this.name);
            }
            case READY -> {
                if (!this.ready) {
                    this.ready = true;
                    System.out.println("[ClientHandler] " + name + " está pronto");
                    gameSession.checkReadiness();
                }
            }
            case MOVE -> {
                if (gameSession.isEmJogo()) {
                    MoveMessage moveMsg = (MoveMessage) msg;
                    gameSession.processMovement(this, moveMsg);
                } else {
                    send(new ErrorMessage("A partida ainda não começou!"));
                }
            }
            case SHOOT -> {
                if (gameSession.isEmJogo()) {
                    ShootMessage shootMsg = (ShootMessage) msg;
                    // Lógica para processar o tiro via GameManager
                }
            }
            default -> send(new ErrorMessage("Comando não suportado no momento: " + msg.type()));
        }
    }

    public void send(Message msg) {
        output.println(MessageSerializer.serialize(msg));
    }

    public boolean isReady() { return ready; }
    public String getName() { return name; }
    public void setGameSession(GameSession session) { this.gameSession = session; }
    public GameSession getGameSession() { return gameSession; }
}