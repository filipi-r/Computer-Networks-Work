package common.protocol;

import common.protocol.message_types.Message;
import common.protocol.message_types.MessageType;

public class MessageSerializer {

    public static String serialize(Message message) {
        MessageType type = message.type();
        String[] data = message.data();
        StringBuilder serializedMessage = new StringBuilder(type.name());
        serializedMessage.append(",");
        for (String field : data) {
            serializedMessage.append(field);
            serializedMessage.append(",");
        }

        serializedMessage.append("\\");

        return serializedMessage.toString();
    }
}
