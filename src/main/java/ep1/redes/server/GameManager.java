package server;

import common.game.Position;
import common.protocol.message_types.MoveMessage;
import common.protocol.message_types.MoveResultMessage;
import java.util.List;
import java.util.Random;

public class GameManager {
    private final GameSession session;
    private final List<ClientHandler> handlers;

    private final Position[] initialPositions = new Position[2];

    private static final int MAP_WIDTH = 10;
    private static final int MAP_HEIGHT = 10;

    public GameManager(GameSession session, List<ClientHandler> handlers) {
        this.session = session;
        this.handlers = handlers;
        randomizeInitialPositions();
    }

    private void randomizeInitialPositions() {
        initialPositions[0] = sortearPosicao();

        do {
            initialPositions[1] = sortearPosicao();
        } while (initialPositions[1].equals(initialPositions[0]));

        System.out.println("[GameManager] Posições sorteadas - P1: " + initialPositions[0] + " | P2: " + initialPositions[1]);
    }

    private Position sortearPosicao() {
        Random random = new Random();
        return new Position(random.nextInt(MAP_WIDTH), random.nextInt(MAP_HEIGHT));
    }

    public void processMoviment(ClientHandler cliente, MoveMessage moveMsg) {
        Position posAtual = getInitialPosition(cliente);
        // Exemplo simples: confirma o movimento enviando a posição atualizada via MoveResultMessage
        cliente.send(new MoveResultMessage(posAtual));
    }

    public void iniciarPartida() {
        System.out.println("[GameManager] Partida iniciada na sala " + session.getId());
    }

    public void endMatch() {
        System.out.println("[GameManager] Encerrando partida na sala " + session.getId());
    }

    public Position getInitialPosition(ClientHandler cliente) {
        int index = handlers.indexOf(cliente);
        return (index >= 0 && index < initialPositions.length) ? initialPositions[index] : null;
    }
}