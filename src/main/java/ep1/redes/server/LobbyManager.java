package server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {
    private final Map<String, GameSession> sessoesAtivas = new ConcurrentHashMap<>();
    private GameSession lobbyPendente = null;

    // Sincronizado para evitar race conditions ao alocar jogadores nas salas
    // synchronized é tipo um mutex
    public synchronized void entrarNoLobby(ClientHandler client) {
        if (lobbyPendente == null || lobbyPendente.isCheio()) {
            String idSala = "LOBBY-" + UUID.randomUUID().toString().substring(0, 4);
            lobbyPendente = new GameSession(idSala, this);
            sessoesAtivas.put(idSala, lobbyPendente);
            System.out.println("[LobbyManager] Criado novo lobby: " + idSala);
        }

        // Conecta o handler ao lobby específico
        lobbyPendente.adicionarJogador(client);
        client.setGameSession(lobbyPendente);

        if (lobbyPendente.isCheio()) {
            System.out.println("[LobbyManager] " + lobbyPendente.getId() + " está cheio!");
            lobbyPendente = null; // Próximo cliente vai criar outro lobby
        }
    }

    public synchronized void removerSessao(String sessionId) {
        sessoesAtivas.remove(sessionId);
        System.out.println("[LobbyManager] Lobby " + sessionId + " encerrado.");
    }
}
