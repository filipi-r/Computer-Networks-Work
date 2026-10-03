package common.protocol.message_types;

public record JoinMessage(String playerName) implements Message {

    @Override
    public MessageType type() {
        return MessageType.JOIN;
    }

    @Override
    public String[] data() {
        return new String[] {playerName};
    }
}
