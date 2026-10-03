package ep1.redes.common.protocol.message_types;

public record ShootMessage(int x, int y) implements Message {

    @Override
    public MessageType type() {
        return MessageType.SHOOT;
    }

    @Override
    public String[] data() {
        return new String[] { String.valueOf(x), String.valueOf(y) };
    }
}
