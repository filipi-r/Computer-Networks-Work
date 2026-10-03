package common.protocol.message_types;

public record GameOverMessage(String winner) implements Message {

    @Override
    public MessageType type() {
        return MessageType.GAME_OVER;
    }

    @Override
    public String[] data() {
        return new String[] { winner };
    }
}
