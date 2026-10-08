package client;

import java.io.BufferedReader;
import java.io.IOException;

import common.game.TileType;
import common.protocol.MessageParser;
import common.protocol.message_types.ErrorMessage;
import common.protocol.message_types.GameOverMessage;
import common.protocol.message_types.GameStartMessage;
import common.protocol.message_types.GameStateMessage;
import common.protocol.message_types.Message;
import common.protocol.message_types.MoveResultMessage;
import common.protocol.message_types.RoundMessage;
import common.protocol.message_types.ShootResultMessage;

/**
 * Thread que só LÊ o servidor: atualiza o estado e mostra as mensagens.
 * Nunca lê o teclado (isso é feito pela thread principal, em Client.commandLoop).
 */
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
            String data;
            while ((data = in.readLine()) != null) {
                if (data.isBlank()) continue;
                try {
                    processMessage(MessageParser.parse(data));
                } catch (RuntimeException e) {
                    TerminalUI.println("[CLIENTE] mensagem do servidor ignorada: " + e.getMessage());
                }
            }
            connectionLost();
        } catch (IOException e) {
            connectionLost();
        }
    }

    private void connectionLost() {
        if (client.isClosing()) return; // fomos nós que fechamos (quit)
        TerminalUI.println("[SERVIDOR] conexão encerrada. Pressione Enter para sair.");
        client.getGameState().finish();
    }

    private void processMessage(Message message) {
        boolean changed = client.updateGameState(message);

        switch (message.type()) {
            case CONNECTED:
                TerminalUI.println("[SERVIDOR] Conectado com sucesso!");
                break;
            case WAITING:
                TerminalUI.println("Aguardando outro jogador entrar...");
                break;
            case LOBBY_FULL:
                TerminalUI.println("Lobby cheio! Digite OK para começar a partida:");
                break;
            case GAME_START:
                GameStartMessage start = (GameStartMessage) message;
                TerminalUI.println("Partida iniciada! Você começa em " + start.playerPosition());
                break;
            case GAME_STATE:
                if (changed) {
                    TerminalUI.boardRender(((GameStateMessage) message).boardView());
                }
                break;
            case MOVE_RESULT:
                MoveResultMessage move = (MoveResultMessage) message;
                TerminalUI.println("Você se moveu para (" + move.playerPosition().x() + ", " + move.playerPosition().y() + ").");
                break;
            case SHOOT_RESULT:
                ShootResultMessage shot = (ShootResultMessage) message;
                String where = "(" + shot.position().x() + ", " + shot.position().y() + ")";
                if (shot.tileType() == TileType.PLAYER) {
                    TerminalUI.println("ACERTOU! Você atingiu o inimigo em " + where + ".");
                } else {
                    TerminalUI.println("Tiro em " + where + ": só água. A névoa ao redor foi dissipada.");
                }
                break;
            case ROUND:
                RoundMessage round = (RoundMessage) message;
                if (round.canMove() || round.canShoot()) {
                    TerminalUI.println("Rodada " + round.round() + " - disponível: "
                            + (round.canMove() ? "[move] " : "") + (round.canShoot() ? "[shoot]" : ""));
                } else {
                    TerminalUI.println("Rodada " + round.round() + " - você já agiu; aguardando o outro jogador...");
                }
                break;
            case GAME_OVER:
                String winner = ((GameOverMessage) message).winner();
                if (winner.equals(client.getPlayerName())) {
                    TerminalUI.println("*** VOCÊ VENCEU! ***");
                } else {
                    TerminalUI.println("*** Seu barco foi afundado. Vencedor: " + winner + " ***");
                }
                TerminalUI.println("Pressione Enter para sair.");
                break;
            case ERROR:
                String error = ((ErrorMessage) message).error();
                TerminalUI.println("[ERROR] " + error);
                if (ErrorMessage.OPPONENT_DISCONNECTED.equals(error)) {
                    client.getGameState().finish();
                }
                break;
            default:
                break;
        }
    }
}
