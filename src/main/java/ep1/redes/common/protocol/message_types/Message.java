package ep1.redes.common.protocol.message_types;

public interface Message {
    MessageType type();
    String[] data();
}
