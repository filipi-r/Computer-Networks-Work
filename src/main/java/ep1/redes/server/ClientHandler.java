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
    private String name = "Jogador";

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
                    gameSession.processMovement(this, (MoveMessage) msg);
                } else {
                    sendGameNotRunning();
                }
            }
            case SHOOT -> {
                if (gameSession.isEmJogo()) {
                    gameSession.processShoot(this, (ShootMessage) msg);
                } else {
                    sendGameNotRunning();
                }
            }
            default -> send(new ErrorMessage("Comando não suportado no momento: " + msg.type()));
        }
    }


    //esse ternario foi eu mesmo professor, os da classe TerminalUI pode até n ter sido, mas esse e uns outros ai fui eu mesmo
    private void sendGameNotRunning() {
        send(new ErrorMessage(gameSession.isFinished() ? "A partida já terminou." : "A partida ainda não começou!"));
    }

    public void send(Message msg) {
        output.println(MessageSerializer.serialize(msg));
    }

    public boolean isReady() { return ready; }
    public String getName() { return name; }
    public void setGameSession(GameSession session) { this.gameSession = session; }
    public GameSession getGameSession() { return gameSession; }
}