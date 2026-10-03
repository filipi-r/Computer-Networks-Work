package client;

import common.game.BoardView;
import common.game.TileType;
import common.game.Position;

public class BoardRenderer { 
    private static final char FOG = '*';
    private static final char WATER = '~';
    private static final char ME = 'B';
    private static final char ENEMY = 'E';
 
    public static String render(BoardView view) {
        TileType[][] tiles = view.visibleTiles();
        Position me = view.playerPosition();
        int height = tiles.length;
        int width = tiles[0].length;
 
        StringBuilder board = new StringBuilder();
 
        board.append("    ");
        for (int x = 0; x < width; x++) {
            board.append(String.format("%2d ", x));
        }
        board.append('\n');
 
        for (int x = 0; x < height; x++) {
            board.append(String.format("%2d  ", x));
            for (int y = 0; y < width; y++) {
                board.append(' ').append(symbol(tiles[x][y], x, y, me)).append(' ');
            }
            board.append('\n');
        }
 
        board.append("\n")
          .append(FOG).append(" névoa   ")
          .append(WATER).append(" água   ")
          .append(ME).append(" você   ")
          .append(ENEMY).append(" inimigo\n");
        
        return board.toString();
    }
 
    private static char symbol(TileType tile, int x, int y, Position me) {
        switch (tile) {
            case FOG:
                return FOG;
            case WATER: 
                return WATER;
            case PLAYER:
                if(x == me.x() && y == me.y()) return ME;
            default:
                return ENEMY;
        }
    }
}
