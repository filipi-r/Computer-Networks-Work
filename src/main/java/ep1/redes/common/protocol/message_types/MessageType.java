package common.protocol.message_types;

public enum MessageType {

    CONNECTED,
    WAITING,
    READY,
    LOBBY_FULL,
    GAME_START,

    MOVE,
    SHOOT,

    MOVE_RESULT,
    SHOOT_RESULT,

    GAME_STATE,
    GAME_OVER,

    ERROR
}