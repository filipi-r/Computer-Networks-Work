package client;

import java.util.Arrays;

import common.game.BoardView;
import common.protocol.message_types.GameStateMessage;
import common.protocol.message_types.Message;
import common.protocol.message_types.RoundMessage;
import common.protocol.message_types.ConnectedMessage;

/**
 * Estado do jogo do ponto de vista do cliente.
 * É escrito pela thread que lê o servidor e lido pela thread principal (loop de comandos),
 * por isso os métodos são synchronized.
 */
public class ClientGameState {
    public enum ClientState {
        CONNECTING,
        LOBBY_FULL,
        INGAME_PLAYING, // partida em andamento e o jogador ainda pode agir nesta rodada (mover e/ou atirar)
        INGAME_WAITING, // partida em andamento, mas o jogador já agiu nesta rodada: espera o outro terminar
        FINISHED
    }

    private ClientState currentState = ClientState.CONNECTING;
    private BoardView board = null;
    private int myPlayerIndex = -1;

    public synchronized ClientState getState() { return currentState; }
    public synchronized BoardView getBoard() { return board; }

    public synchronized int getMyPlayerIndex() { return myPlayerIndex; }

    public synchronized boolean isInGame() {
        return currentState == ClientState.INGAME_PLAYING || currentState == ClientState.INGAME_WAITING;
    }

    /** Encerra o cliente (partida acabou ou a conexão caiu). */
    public synchronized void finish() {
        this.currentState = ClientState.FINISHED;
    }

    /**
     * Atualiza o estado a partir de uma mensagem do servidor.
     * @return false quando a visão do tabuleiro não mudou.
     */
    public synchronized boolean updateState(Message message) {
        if (currentState == ClientState.FINISHED) return true;

        switch (message.type()) {
            case CONNECTED:
                ConnectedMessage conn = (ConnectedMessage) message;
                // playerNumber vem 1 ou 2; convertemos para índice 0 ou 1
                this.myPlayerIndex = conn.playerNumber() - 1;
                break;
            case LOBBY_FULL:
                this.currentState = ClientState.LOBBY_FULL;
                break;
            case GAME_START:
                this.currentState = ClientState.INGAME_PLAYING; // a rodada 1 começa com as duas ações livres
                break;
            case ROUND:
                RoundMessage round = (RoundMessage) message;
                if (round.canMove() || round.canShoot()) {
                    this.currentState = ClientState.INGAME_PLAYING;
                } else {
                    this.currentState = ClientState.INGAME_WAITING;
                }
                break;
            case GAME_OVER:
                finish();
                break;
            case GAME_STATE:
                BoardView newBoard = ((GameStateMessage) message).boardView();
                boolean changed;
                if (board == null) {
                    changed = true;
                } else {
                    boolean positionChanged = !board.playerPosition().equals(newBoard.playerPosition());
                    boolean visibleTilesChanged = !Arrays.deepEquals(board.visibleTiles(), newBoard.visibleTiles());
                    changed = positionChanged || visibleTilesChanged;
                }
                this.board = newBoard;
                return changed;
            default:
                break;
        }
        return true;
    }
}
