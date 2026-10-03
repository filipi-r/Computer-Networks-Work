package common.protocol.message_types;

public record ConnectedMessage(String lobbyId, int playerNumber) implements Message {

    @Override
    public MessageType type() {
        return MessageType.CONNECTED;
    }

    @Override
    public String[] data() {
        return new String[] {
            lobbyId,
            String.valueOf(playerNumber)
        };
    }
}
