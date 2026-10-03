package common.protocol.message_types;

public record WaitingMessage() implements Message {

    @Override
    public MessageType type() {
        return MessageType.WAITING;
    }

    @Override
    public String[] data() {
        return new String[0];
    }
}
