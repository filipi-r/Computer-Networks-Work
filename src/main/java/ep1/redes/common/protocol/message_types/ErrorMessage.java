package common.protocol.message_types;

public record ErrorMessage(String error) implements Message{
    public static final String OPPONENT_DISCONNECTED = "O outro jogador desconectou. Partida encerrada.";

    @Override 
    public MessageType type() {
        return MessageType.ERROR;
    }

    @Override
    public String[] data() {
        return new String[]{error};
    }

}
