package ep1.redes.common.protocol.message_types;

public record ErrorMessage(String error) implements Message{

    @Override 
    public MessageType type() {
        return MessageType.ERROR;
    }

    @Override
    public String[] data() {
        return new String[]{error};
    }

}
