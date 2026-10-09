package common.game;

public interface BoardView {
    Position playerPosition();
    TileType[][] visibleTiles();
}
