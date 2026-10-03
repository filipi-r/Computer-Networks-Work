package server;

import common.game.BoardView;
import common.game.TileType;
import common.protocol.message_types.*;
import java.util.ArrayList;
import java.util.List;

public class GameSession {

    public enum SessionState {
        WAITING_PLAYERS,
        WAITING_OK,
        SELCTING_POSITION,
        GAME_RUNNING
    }

    private final String id;
    private final LobbyManager manager;
    private final List<ClientHandler> players = new ArrayList<>();
    private static final int MAX_PLAYERS = 2;
    private GameManager gameManager;
    private SessionState state = SessionState.WAITING_PLAYERS;

    public GameSession(String id, LobbyManager manager) {
        this.id = id;
        this.manager = manager;
    }

    // Trecho atual em GameSession.java
    public synchronized boolean addPlayer(ClientHandler client) {
        if (players.size() < MAX_PLAYERS) {
            players.add(client);
            client.send(new ConnectedMessage(id, players.size()));

            if (isFull()) {
                state = SessionState.WAITING_OK;
                sendAll(new LobbyFullMessage()); // <-- ENVIA PARA O P1 E P2
            } else {
                client.send(new WaitingMessage());    // <-- ENVIA PARA O P1 QUANDO ENTRA SOZINHO
            }
            return true;
        }
        return false;
    }

    public synchronized void processMovement(ClientHandler cliente, MoveMessage moveMsg) {
        if (gameManager != null) {
            gameManager.processMoviment(cliente, moveMsg);
        }
    }

    public synchronized void checkReadiness() {
        if (state == SessionState.WAITING_OK && isFull() && players.stream().allMatch(ClientHandler::isReady)) {
            this.gameManager = new GameManager(this, new ArrayList<>(players));
            gameStart();
        }
    }

    public synchronized void gameStart() {
        this.state = SessionState.GAME_RUNNING;
        
        for (ClientHandler j : players) {
            var initialPosition = gameManager.getInitialPosition(j);
            j.send(new GameStartMessage(initialPosition));
            j.send(new GameStateMessage(new BoardView(initialPosition, new TileType[12][12])));
        }

        if (this.gameManager != null) {
            this.gameManager.iniciarPartida();
        }
    }

    public synchronized void removePlayer(ClientHandler client) {
        players.remove(client);
        sendAll(new ErrorMessage("O outro jogador desconectou. Partida encerrada."));

        if (gameManager != null) {
            gameManager.endMatch();
        }

        manager.removeSession(this.id);
    }

    public synchronized void sendAll(Message mensagem) {
        for (ClientHandler j : players) {
            j.send(mensagem);
        }
    }

    public boolean isFull() { return players.size() >= MAX_PLAYERS; }
    public String getId() { return id; }
    public GameManager getGameManager() { return gameManager; }
    public boolean isEmFaseSelecao() { return state == SessionState.SELCTING_POSITION; }
    public boolean isEmJogo() { return state == SessionState.GAME_RUNNING; }
    public SessionState getState() { return state; }
}