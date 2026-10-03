package common.protocol.message_types;

import java.util.ArrayList;
import java.util.List;

import common.game.BoardView;
import common.game.TileType;

public record GameStateMessage(BoardView boardView) implements Message {

    @Override
    public MessageType type() {
        return MessageType.GAME_STATE;
    }

    /*Transforma o array em trios com coordenadas x e y e o valor (agua ou jogador)
     */
    @Override
    public String[] data() {
        TileType[][] visibleTiles = boardView.visibleTiles();
        var playerPosition = boardView.playerPosition();
        int height = visibleTiles.length;
        int width = visibleTiles[0].length;
        List<String> data = new ArrayList<>();

        data.add(String.valueOf(playerPosition.x()));
        data.add(String.valueOf(playerPosition.y()));
        data.add(String.valueOf(width));
        data.add(String.valueOf(height));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileType tile = visibleTiles[y][x];
                if (tile != TileType.FOG) {
                    data.add(String.valueOf(x));
                    data.add(String.valueOf(y));
                    data.add(tile.name());
                }
            }
        }

        return data.toArray(new String[data.size()]);
    }
}
