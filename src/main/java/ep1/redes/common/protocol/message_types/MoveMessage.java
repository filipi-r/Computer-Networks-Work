package ep1.redes.common.protocol.message_types;

import ep1.redes.common.game.Direction;

public record MoveMessage(Direction dir) implements Message {

    @Override
    public MessageType type() {
        return MessageType.MOVE;
    }

    @Override
    public String[] data() {
        return new String[] { dir.name() };
    }
}
