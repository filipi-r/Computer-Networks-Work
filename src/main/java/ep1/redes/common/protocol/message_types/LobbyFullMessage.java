package common.protocol.message_types;

public record LobbyFullMessage() implements Message {
    @Override 
    public MessageType type() {
        return MessageType.LOBBY_FULL;
    }

    @Override
    public String[] data() {
        return new String[0];
    }
}
