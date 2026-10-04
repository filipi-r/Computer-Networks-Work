package common.protocol.message_types;


//o espaço entre os tipos não representa nada (agrupamento ou algo do tipo) eu fiz pq sou maluco
public enum MessageType {

    JOIN,
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
    ROUND,
    GAME_OVER,

    ERROR
}