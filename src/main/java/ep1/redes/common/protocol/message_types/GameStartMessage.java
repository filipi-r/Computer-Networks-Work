package ep1.redes.common.protocol.message_types;

import ep1.redes.common.game.Position;

public record GameStartMessage(Position playerPosition) implements Message {

    @Override
    public MessageType type() {
        return MessageType.GAME_START;
    }

    @Override
    public String[] data() {
        return new String[] {
            String.valueOf(playerPosition.x()),
            String.valueOf(playerPosition.y())
        };
    }
}
