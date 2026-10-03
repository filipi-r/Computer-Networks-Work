package client;

import common.protocol.message_types.Message;

public class ClientGameState {
    public enum ClientState {
        CONNECTING,
        LOBBY_FULL,
        RUNNING
    }

    private ClientState currentState = ClientState.CONNECTING;

    public ClientState getState() {
        return currentState;
    }

    public void updateState(Message message) {
        switch(message.type()) {
            case LOBBY_FULL:
                this.currentState = ClientState.LOBBY_FULL;
            case GAME_START:
                this.currentState = ClientState.RUNNING;
            default:
                break;
        }
    }
}
