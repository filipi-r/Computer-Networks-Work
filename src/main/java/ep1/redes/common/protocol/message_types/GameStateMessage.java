package ep1.redes.common.protocol.message_types;

import ep1.redes.common.game.Position;
import ep1.redes.common.game.TileType;

public record GameStateMessage(Position playerPosition, TileType[][] visibleTiles) implements Message {

    @Override
    public MessageType type() {
        return MessageType.GAME_STATE;
    }

    @Override
    public String[] data() {
        int height = visibleTiles.length;
        int width = height == 0 ? 0 : visibleTiles[0].length;
        String[] data = new String[4 + height * width];

        data[0] = String.valueOf(playerPosition.x());
        data[1] = String.valueOf(playerPosition.y());
        data[2] = String.valueOf(width);
        data[3] = String.valueOf(height);

        int index = 4;
        for (TileType[] row : visibleTiles) {
            for (TileType tile : row) {
                data[index++] = tile.name();
            }
        }

        return data;
    }
}
