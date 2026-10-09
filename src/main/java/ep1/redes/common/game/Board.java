package common.game;

import java.util.Arrays;

/*
Server -> monta um Board para cada jogador e envia
Client -> recebe o Board */
public class Board implements BoardView {
    private final Position playerPosition;
    private final TileType[][] tiles;
    public Board(int rows, int cols, Position playerPosition) {
        this.playerPosition = playerPosition;
        this.tiles = new TileType[rows][cols];
        for (TileType[] row : tiles) {
            Arrays.fill(row, TileType.FOG);
        }
        tiles[playerPosition.x()][playerPosition.y()] = TileType.PLAYER;
    }

    //usado pelo cliente
    public Board(Position playerPosition, TileType[][] tiles) {
        this.playerPosition = playerPosition;
        this.tiles = tiles;
    }

    public void set(Position p, TileType tile) {
        if (p.equals(playerPosition)) return;
        tiles[p.x()][p.y()] = tile;
    }

    public TileType get(Position p) { return tiles[p.x()][p.y()]; }
    public int rows() { return tiles.length; }
    public int cols() { return tiles[0].length; }

    @Override
    public Position playerPosition() { return playerPosition; }

    @Override
    public TileType[][] visibleTiles() { return tiles; }
}
