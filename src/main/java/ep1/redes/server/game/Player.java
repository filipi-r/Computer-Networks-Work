package server.game;

import common.game.Position;

//estado do jogador e não o jogador
public class Player {
    private final int index;
    private final Visibility visibility = new Visibility();

    private Position position;
    private boolean alive = true;

    private boolean moved = false;
    private boolean shot = false;

    public Player(int index, Position startPosition) {
        this.index = index;
        this.position = startPosition;
    }

    public int index() { return index; }
    public Position position() { return position; }
    public Visibility visibility() { return visibility; }
    public boolean isAlive() { return alive; }

    public void moveTo(Position newPosition) { this.position = newPosition; }
    public void kill() { this.alive = false; }

    public boolean hasMoved() { return moved; }
    public boolean hasShot() { return shot; }
    public void markMoved() { this.moved = true; }
    public void markShot() { this.shot = true; }

    public void startRound() {
        this.moved = false;
        this.shot = false;
    }
}
