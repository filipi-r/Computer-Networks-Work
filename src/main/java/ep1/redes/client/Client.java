package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import client.ClientGameState.ClientState;
import common.protocol.MessageSerializer;
import common.protocol.message_types.Message;
import common.protocol.message_types.ReadyMessage;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 58901;

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String playerName;
    private final ClientGameState gameState;
    TerminalUI terminal;

    public Client(String playerName, TerminalUI terminal) throws IOException{
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        this.gameState = new ClientGameState();
        this.playerName = playerName;
    }

    public void updateGameState(Message message) {
        gameState.updateState(message);
    }

    public void start() {
        ServerConnection server = new ServerConnection(in, this);
        Thread thread = new Thread(server);
        thread.start();
    }

    public void send(Message message) {
        out.println(MessageSerializer.serialize(message));
        if(out.checkError()) {
            System.out.println("\n[CLIENTE] conexão perdida, encerrando processo...");
        }
    }

    public void gameLoop() {
        terminal.printHelp();
        while (gameState.getState() == ClientState.RUNNING && terminal.hasNextLine()) {
            String[] command = terminal.getPrompt();
            if (command[0].isEmpty()) continue;

            switch (command[0].toLowerCase()) {
                case "ok":
                    handleOk();
                    break;
            }
        }
    }

    private void handleOk() {
        if (gameState.getState() != ClientState.LOBBY_FULL) {
            System.out.println("Não há nada para confirmar agora.");
            return;
        }

        send(new ReadyMessage(playerName));
        System.out.println("Confirmado! Aguardando o servidor iniciar a partida...");
    }

    public static void main(String[] args) throws IOException{
        TerminalUI terminal = new TerminalUI();

        System.out.println("Digite seu apelido:");
        String name = new Scanner(System.in).nextLine().trim();

        //tirando virgulas pq o protocolo separa as coisas por virgula e deu merda
        name = name.replace(",", "").replace("\\", "");
        if (name.isEmpty()) name = "jogador";

        new Client(name, terminal).start();
    }
}
