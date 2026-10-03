package ep1.redes.common.protocol.message_types;

import ep1.redes.common.game.Position;
import ep1.redes.common.game.TileType;

public record ShootResultMessage(Position position, TileType tileType) implements Message {

    @Override
    public MessageType type() {
        return MessageType.SHOOT_RESULT;
    }

    @Override
    public String[] data() {
        return new String[] {
            String.valueOf(position.x()),
            String.valueOf(position.y()),
            tileType.name()
        };
    }
}
