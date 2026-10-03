package common.game;

/*Class that contains the board details that the player know of.
Server -> send a board view
Client -> receives board view*/
public record BoardView(Position playerPosition, TileType[][] visibleTiles) {
    
}
