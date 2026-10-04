package server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.game.Board;
import common.game.BoardView;
import common.game.Direction;
import common.game.Position;
import common.game.TileType;

/**
 * Regras e estado da partida. Não conhece sockets: recebe ações, valida e atualiza o estado.
 * Quem fala com a rede é o GameManager.
 *
 * Como funciona uma rodada:
 *  - Os dois jogadores agem ao mesmo tempo: cada um pode fazer 1 MOVE e 1 SHOOT, em qualquer ordem.
 *  - Cada ação vale na hora em que o servidor a processa (as ações chegam uma de cada vez, na ordem de chegada).
 *  - A rodada só fecha quando OS DOIS usaram as duas ações. Quem termina antes espera o outro.
 *  - A névoa dissipada por um tiro dura REVEAL_ROUNDS rodadas contando a rodada em que o tiro foi dado.
 *    Só quem atirou enxerga a área revelada.
 */
public class Game {
    public static final int ROWS = 10;
    public static final int COLS = 10;
    public static final int REVEAL_ROUNDS = 3;
    private static final int NUM_PLAYERS = 2;

    public record ShotResult(Position target, boolean hit) {}

    private final Player[] players = new Player[NUM_PLAYERS];

    private int round = 1;
    private Player winner = null;

    public Game(Random random) {
        Position p1 = randomPosition(random);
        Position p2;
        do {
            p2 = randomPosition(random);
        } while (p2.equals(p1));

        players[0] = new Player(0, p1);
        players[1] = new Player(1, p2);
    }

    // ---------------------------------------------------------------- ações

    /** Move o barco uma casa. Se a casa for a do inimigo, o barco o abalroa e ele morre. */
    public Position move(int playerIndex, Direction dir) throws InvalidActionException {
        Player me = checkCanAct(playerIndex);
        if (me.hasMoved()) {
            throw new InvalidActionException("Você já se moveu nesta rodada.");
        }

        Position destination = step(me.position(), dir);
        if (!contains(destination)) {
            throw new InvalidActionException("Movimento inválido: você sairia do tabuleiro.");
        }

        me.moveTo(destination);
        me.markMoved();

        // Recusar o movimento por "casa ocupada" revelaria onde o inimigo está, então abalroar o mata.
        Player enemy = enemyOf(me);
        if (enemy.position().equals(destination)) {
            enemy.kill();
            winner = me;
        }

        afterAction();
        return destination;
    }

    /** Atira em qualquer casa do tabuleiro: dissipa a névoa em cruz e mata o inimigo se ele estiver no centro. */
    public ShotResult shoot(int playerIndex, Position target) throws InvalidActionException {
        Player me = checkCanAct(playerIndex);
        if (me.hasShot()) {
            throw new InvalidActionException("Você já atirou nesta rodada.");
        }
        if (!contains(target)) {
            throw new InvalidActionException("Tiro inválido: use linha de 0 a " + (ROWS - 1)
                    + " e coluna de 0 a " + (COLS - 1) + ".");
        }

        me.markShot();
        me.visibility().reveal(cross(target), REVEAL_ROUNDS);

        Player enemy = enemyOf(me);
        boolean hit = enemy.position().equals(target);
        if (hit) {
            enemy.kill();
            winner = me;
        }

        afterAction();
        return new ShotResult(target, hit);
    }

    private Player checkCanAct(int playerIndex) throws InvalidActionException {
        if (isOver()) {
            throw new InvalidActionException("A partida já terminou.");
        }
        return players[playerIndex];
    }

    private void afterAction() {
        if (isOver()) return;
        for (Player p : players) {
            if (!p.hasMoved() || !p.hasShot()) return;
        }

        round++;
        for (Player p : players) {
            p.visibility().endRound();
            p.startRound();
        }
    }

    public Board viewFor(int playerIndex) {
        Player me = players[playerIndex];
        Player enemy = enemyOf(me);

        Board board = new Board(ROWS, COLS, me.position());
        for (Position cell : me.visibility().revealedCells()) {
            board.set(cell, TileType.WATER);
        }
        if (me.visibility().isRevealed(enemy.position())) {
            board.set(enemy.position(), TileType.PLAYER);
        }
        return board;
    }

    public Position positionOf(int playerIndex) { return players[playerIndex].position(); }
    public boolean canMove(int playerIndex) { return !isOver() && !players[playerIndex].hasMoved(); }
    public boolean canShoot(int playerIndex) { return !isOver() && !players[playerIndex].hasShot(); }
    public int round() { return round; }
    public boolean isOver() { return winner != null; }
    public int winnerIndex() { return winner == null ? -1 : winner.index(); }


    private Player enemyOf(Player p) {
        return players[(p.index() + 1) % NUM_PLAYERS];
    }

    private static Position randomPosition(Random random) {
        return new Position(random.nextInt(ROWS), random.nextInt(COLS));
    }

    private static boolean contains(Position p) {
        return p.x() >= 0 && p.x() < ROWS && p.y() >= 0 && p.y() < COLS;
    }

    private static Position step(Position p, Direction dir) {
        return switch (dir) {
            case UP -> new Position(p.x() - 1, p.y());
            case DOWN -> new Position(p.x() + 1, p.y());
            case LEFT -> new Position(p.x(), p.y() - 1);
            case RIGHT -> new Position(p.x(), p.y() + 1);
        };
    }

    private static List<Position> cross(Position center) {
        List<Position> cells = new ArrayList<>();
        cells.add(center);
        for (Direction dir : Direction.values()) {
            Position neighbor = step(center, dir);
            if (contains(neighbor)) cells.add(neighbor);
        }
        return cells;
    }
}
