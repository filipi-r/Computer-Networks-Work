package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.protocol.MessageParser;
import common.protocol.MessageSerializer;
import common.protocol.message_types.ConnectedMessage;

public class GameSession {

    public enum EstadoSessao {
        AGUARDANDO_JOGADORES,
        AGUARDANDO_OK,
        SELECAO_POSICAO,
        EM_JOGO
    }

    private final String id;
    private final LobbyManager manager;
    private final List<ClientHandler> jogadores = new ArrayList<>();
    private static final int MAX_JOGADORES = 2;
    private GameManager gameManager;
    private EstadoSessao estado = EstadoSessao.AGUARDANDO_JOGADORES;

    private final Map<ClientHandler, int[]> posicoesEscolhidas = new HashMap<>();

    public GameSession(String id, LobbyManager manager) {
        this.id = id;
        this.manager = manager;
    }

    public synchronized boolean adicionarJogador(ClientHandler client) {
        if (jogadores.size() < MAX_JOGADORES) {
            jogadores.add(client);
            client.send(MessageSerializer.serialize(new ConnectedMessage(id, jogadores.size())));

            if (isCheio()) {
                estado = EstadoSessao.AGUARDANDO_OK;
                notificarTodos("LOBBY_FULL Envie 'OK' para confirmar prontidão.");
            } else {
                client.send("WAITING Aguardando segundo jogador entrar...");
            }
            return true;
        }
        return false;
    }

    public synchronized void processarEscolhaPosicao(ClientHandler cliente, int x, int y) {
        // 1. Verifica se a posição já foi escolhida pelo adversário
        for (Map.Entry<ClientHandler, int[]> entry : posicoesEscolhidas.entrySet()) {
            if (!entry.getKey().equals(cliente)) {
                int[] posAdversario = entry.getValue();
                if (posAdversario[0] == x && posAdversario[1] == y) {
                    cliente.send("ERROR: Esta posição (" + x + "," + y + ") já foi escolhida pelo outro jogador! Escolha outra.");
                    return;
                }
            }
        }

        // 2. Registra a posição do jogador atual
        posicoesEscolhidas.put(cliente, new int[]{x, y});
        cliente.send("POSITION_CONFIRMED: Posição (" + x + "," + y + ") registrada! Aguardando o outro jogador...");

        // 3. Se ambos escolheram, inicia a partida
        if (posicoesEscolhidas.size() == MAX_JOGADORES) {
            iniciarJogoOficialmente();
        }
    }

    public synchronized void processarMovimento(ClientHandler cliente, String msg) {
        if (gameManager != null) {
            gameManager.processarMovimento(cliente, msg);
        }
    }

    public synchronized void verificarProntidao() {
        if (estado == EstadoSessao.AGUARDANDO_OK && isCheio() && jogadores.stream().allMatch(ClientHandler::isReady)) {
            estado = EstadoSessao.SELECAO_POSICAO;
            this.gameManager = new GameManager(this, new ArrayList<>(jogadores));
            notificarTodos("SELECT_POSITION Escolha sua posição inicial no mapa usando 'SELECT X Y'.");
        }
    }

    public synchronized void iniciarJogoOficialmente() {
        this.estado = EstadoSessao.EM_JOGO;
        notificarTodos("GAME_START Partida iniciada!");
        if (this.gameManager != null) {
            this.gameManager.iniciarPartida();
        }
    }

    public synchronized void removerJogador(ClientHandler client) {
        jogadores.remove(client);
        posicoesEscolhidas.remove(client);
        notificarTodos("PLAYER_DISCONNECTED O outro jogador desconectou.");

        if (gameManager != null) {
            gameManager.encerrarPartida();
        }

        manager.removerSessao(this.id);
    }

    public synchronized void notificarTodos(String mensagem) {
        for (ClientHandler j : jogadores) {
            j.send(mensagem);
        }
    }

    public boolean isCheio() { return jogadores.size() >= MAX_JOGADORES; }
    public String getId() { return id; }
    public GameManager getGameManager() { return gameManager; }
    public boolean isEmFaseSelecao() { return estado == EstadoSessao.SELECAO_POSICAO; }
    public boolean isEmJogo() { return estado == EstadoSessao.EM_JOGO; }
    public EstadoSessao getEstado() { return estado; }
}
