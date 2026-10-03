package client;

import java.io.BufferedReader;
import java.io.IOException;

import common.protocol.MessageParser;
import common.protocol.message_types.GameStartMessage;
import common.protocol.message_types.GameStateMessage;
import common.protocol.message_types.Message;

public class ServerConnection implements Runnable {
    private final BufferedReader in;
    private final Client client;

    public ServerConnection(BufferedReader in, Client client) {
        this.in = in;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            String data;;
            while ((data = in.readLine()) != null) {
                if(data.isBlank()) continue;
                processMessage(MessageParser.parse(data));
            }
        } catch (IOException e) {
            System.out.println("\n[SERVIDOR] conexão perdida, encerrando processo...");
        }
    }

    private void processMessage(Message message) {
        switch (message.type()) {
            case CONNECTED:
                System.out.println("[SERVIDOR] Conectado com sucesso!");
                break;
            case WAITING:
                System.out.println("Aguardando outro jogador entrar...");
                break;
            case LOBBY_FULL:
                client.updateGameState(message);
                System.out.println("Digite OK para começar a partida:");
                client.ready();
                break;
            case GAME_START:
                client.updateGameState(message);
                GameStartMessage start = (GameStartMessage) message;
                System.out.println("Partida iniciada! Você começa em " + start.playerPosition());
                break;
            case GAME_STATE:
                GameStateMessage state = (GameStateMessage)message;
                TerminalUI.boardRender(state.boardView());
                client.gameLoop();
                break;
                    
            case ERROR:
                String[] data = message.data();
                System.out.println("[ERROR] " +  data[0]);
                break;
            default:
                break;
        }
    }
}
