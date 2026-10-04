package server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {
    private final Map<String, GameSession> currentSessions = new ConcurrentHashMap<>();
    private GameSession pendingLobby = null;

    // Sincronizado para evitar race conditions ao alocar jogadores nas salas
    // synchronized é tipo um mutex
    public synchronized void enterLobby(ClientHandler client) {
        if (pendingLobby == null || pendingLobby.isFull()) {
            String lobbyId = "LOBBY-" + UUID.randomUUID().toString().substring(0, 4);
            pendingLobby = new GameSession(lobbyId, this);
            currentSessions.put(lobbyId, pendingLobby);
            System.out.println("[LobbyManager] Criado novo lobby: " + lobbyId);
        }
        
        // Conecta o handler ao lobby específico
        pendingLobby.addPlayer(client);
        client.setGameSession(pendingLobby);
        
        if (pendingLobby.isFull()) {
            System.out.println("[LobbyManager] " + pendingLobby.getId() + " está cheio!");
            pendingLobby = null; // Próximo cliente vai criar outro lobby
        }
    }

    public synchronized void removeSession(String sessionId) {
        currentSessions.remove(sessionId);
        System.out.println("[LobbyManager] Lobby " + sessionId + " encerrado.");
    }
}
