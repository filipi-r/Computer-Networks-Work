package server;

import common.protocol.message_types.*;
import java.util.ArrayList;
import java.util.List;

public class GameSession {

    public enum SessionState {
        WAITING_PLAYERS,
        WAITING_OK,
        SELCTING_POSITION,
        GAME_RUNNING,
        GAME_OVER
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

    public synchronized void processShoot(ClientHandler cliente, ShootMessage shootMsg) {
        if (gameManager != null) {
            gameManager.processShoot(cliente, shootMsg);
        }
    }

    public synchronized void finishGame() {
        this.state = SessionState.GAME_OVER;
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
            j.send(new GameStateMessage(gameManager.getBoardView(j)));
        }

        if (this.gameManager != null) {
            this.gameManager.iniciarPartida();
        }
    }

    public synchronized void removePlayer(ClientHandler client) {
        players.remove(client);

        // Se a partida já tinha acabado, desconectar depois é normal: não precisa avisar ninguém.
        if (state != SessionState.GAME_OVER) {
            if (state == SessionState.GAME_RUNNING) {
                state = SessionState.GAME_OVER; // quem ficou não pode mais jogar sozinho
            }
            sendAll(new ErrorMessage(ErrorMessage.OPPONENT_DISCONNECTED));
        }

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
    public boolean isFinished() { return state == SessionState.GAME_OVER; }
    public SessionState getState() { return state; }
}