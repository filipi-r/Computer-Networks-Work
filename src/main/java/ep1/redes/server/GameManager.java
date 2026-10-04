package server;

import java.util.List;
import java.util.Random;

import common.game.BoardView;
import common.game.Position;
import common.game.TileType;
import common.protocol.message_types.ErrorMessage;
import common.protocol.message_types.GameOverMessage;
import common.protocol.message_types.GameStateMessage;
import common.protocol.message_types.MoveMessage;
import common.protocol.message_types.MoveResultMessage;
import common.protocol.message_types.ShootMessage;
import common.protocol.message_types.RoundMessage;
import common.protocol.message_types.ShootResultMessage;
import server.game.Game;
import server.game.InvalidActionException;

public class GameManager {
    private final GameSession session;
    private final List<ClientHandler> handlers;
    private final Game game;

    public GameManager(GameSession session, List<ClientHandler> handlers) {
        this.session = session;
        this.handlers = handlers;
        this.game = new Game(new Random());

        System.out.println("[GameManager] Posições sorteadas - P1: " + game.positionOf(0)
                + " | P2: " + game.positionOf(1));
    }

    public void processMoviment(ClientHandler client, MoveMessage moveMsg) {
        int player = handlers.indexOf(client);
        if (player < 0) return;
        int roundBefore = game.round();

        try {
            Position newPosition = game.move(player, moveMsg.dir());
            client.send(new MoveResultMessage(newPosition));
        } catch (InvalidActionException e) {
            client.send(new ErrorMessage(e.getMessage()));
            return;
        }
        afterAction(player, roundBefore);
    }

    public void processShoot(ClientHandler cliente, ShootMessage shootMsg) {
        int player = handlers.indexOf(cliente);
        if (player < 0) return;
        int roundBefore = game.round();

        try {
            Game.ShotResult result = game.shoot(player, new Position(shootMsg.x(), shootMsg.y()));
            cliente.send(new ShootResultMessage(result.target(), result.hit() ? TileType.PLAYER : TileType.WATER));
        } catch (InvalidActionException e) {
            cliente.send(new ErrorMessage(e.getMessage()));
            return;
        }
        afterAction(player, roundBefore);
    }

    private void afterAction(int actor, int roundBefore) {
        sendBoardViews();

        if (game.isOver()) {
            String winnerName = handlers.get(game.winnerIndex()).getName();
            System.out.println("[GameManager] Fim de jogo na sala " + session.getId() + ". Vencedor: " + winnerName);
            session.sendAll(new GameOverMessage(winnerName));
            session.finishGame();
        } else if (game.round() != roundBefore) {
            sendRounds();
        } else {
            sendRound(actor);
        }
    }

    private void sendBoardViews() {
        for (int i = 0; i < handlers.size(); i++) {
            handlers.get(i).send(new GameStateMessage(game.viewFor(i)));
        }
    }

    private void sendRounds() {
        for (int i = 0; i < handlers.size(); i++) {
            sendRound(i);
        }
    }

    private void sendRound(int player) {
        handlers.get(player).send(new RoundMessage(game.round(), game.canMove(player), game.canShoot(player)));
    }

    public void iniciarPartida() {
        System.out.println("[GameManager] Partida iniciada na sala " + session.getId());
        sendRounds();
    }

    public void endMatch() {
        System.out.println("[GameManager] Encerrando partida na sala " + session.getId());
    }


    //usei ternario para ficar curto, aqui n foi IA n professor
    public Position getInitialPosition(ClientHandler cliente) {
        int index = handlers.indexOf(cliente);
        return index >= 0 ? game.positionOf(index) : null;
    }

    public BoardView getBoardView(ClientHandler cliente) {
        int index = handlers.indexOf(cliente);
        return index >= 0 ? game.viewFor(index) : null;
    }
}
