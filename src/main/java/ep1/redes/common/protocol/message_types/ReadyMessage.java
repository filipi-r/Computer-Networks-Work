package common.protocol.message_types;

public record ReadyMessage(String playerName) implements Message {

    @Override
    public MessageType type() {
        return MessageType.READY;
    }

    @Override
    public String[] data() {
        return new String[] { playerName };
    }
}
