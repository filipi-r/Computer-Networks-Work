package common.protocol.message_types;

public record GameOverMessage(int winnerIndex, String winnerName) implements Message {

    @Override
    public MessageType type() {
        return MessageType.GAME_OVER;
    }

    @Override
    public String[] data() {
        return new String[] { String.valueOf(winnerIndex), winnerName };
    }
}
