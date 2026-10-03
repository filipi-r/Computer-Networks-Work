package ep1.redes.common.game;
/*Class that contains the board details that the player know of.
Server -> send a board view
Client -> receives board view*/
public class BoardView {
    private TileType[][] visibleTiles;
    private Position playerPosition;
}
