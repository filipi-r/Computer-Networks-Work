package ep1.redes.common.protocol;

import ep1.redes.common.game.Direction;
import ep1.redes.common.game.Position;
import ep1.redes.common.game.TileType;
import ep1.redes.common.protocol.message_types.ErrorMessage;
import ep1.redes.common.protocol.message_types.GameOverMessage;
import ep1.redes.common.protocol.message_types.GameStartMessage;
import ep1.redes.common.protocol.message_types.GameStateMessage;
import ep1.redes.common.protocol.message_types.JoinMessage;
import ep1.redes.common.protocol.message_types.Message;
import ep1.redes.common.protocol.message_types.MessageType;
import ep1.redes.common.protocol.message_types.MoveMessage;
import ep1.redes.common.protocol.message_types.MoveResultMessage;
import ep1.redes.common.protocol.message_types.ShootMessage;
import ep1.redes.common.protocol.message_types.ShootResultMessage;
import ep1.redes.common.protocol.message_types.WaitingMessage;

public class MessageParser {

    public static Message parse(String serializedMessage) {
        String[] parts = serializedMessage.split(",", -1);
        MessageType type = MessageType.valueOf(parts[0]);

        int dataLength = parts.length - 1;
        if (parts[parts.length - 1].equals("\\")) {
            dataLength--;
        }

        String[] data = new String[dataLength];
        System.arraycopy(parts, 1, data, 0, dataLength);

        return switch (type) {
            case JOIN -> new JoinMessage(data[0]);
            case WAITING -> new WaitingMessage();
            case GAME_START -> new GameStartMessage(position(data, 0));
            case MOVE -> new MoveMessage(Direction.valueOf(data[0]));
            case SHOOT -> new ShootMessage(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
            case MOVE_RESULT -> new MoveResultMessage(position(data, 0));
            case SHOOT_RESULT -> new ShootResultMessage(
                    position(data, 0),
                    TileType.valueOf(data[2]));
            case GAME_STATE -> gameState(data);
            case GAME_OVER -> new GameOverMessage(data[0]);
            case ERROR -> new ErrorMessage(data[0]);
        };
    }

    private static Position position(String[] data, int offset) {
        return new Position(
                Integer.parseInt(data[offset]),
                Integer.parseInt(data[offset + 1]));
    }

    private static GameStateMessage gameState(String[] data) {
        Position playerPosition = position(data, 0);
        int width = Integer.parseInt(data[2]);
        int height = Integer.parseInt(data[3]);
        TileType[][] visibleTiles = new TileType[height][width];

        int index = 4;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                visibleTiles[y][x] = TileType.valueOf(data[index++]);
            }
        }

        return new GameStateMessage(playerPosition, visibleTiles);
    }
}
