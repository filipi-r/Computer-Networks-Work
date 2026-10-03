package common.protocol.message_types;

import common.game.Position;

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
