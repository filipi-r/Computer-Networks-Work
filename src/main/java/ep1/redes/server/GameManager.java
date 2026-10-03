package server;

import ep1.redes.common.game.Position;
import java.util.List;

public class GameManager {
    private final GameSession session;
    private final List<ClientHandler> handlers;

    // Array com índice 0 para o Player 1 e índice 1 para o Player 2
    private final Position[] posicoesIniciais = new Position[2];

    private static final int MAP_WIDTH = 10;
    private static final int MAP_HEIGHT = 10;

    public GameManager(GameSession session, List<ClientHandler> handlers) {
        this.session = session;
        this.handlers = handlers;
    }

    public synchronized void processarEscolhaPosicao(ClientHandler cliente, int x, int y) {
        // 1. Validação de limites do mapa
        if (x < 0 || x >= MAP_WIDTH || y < 0 || y >= MAP_HEIGHT) {
            cliente.send("ERROR: Posição fora do mapa (0 a " + (MAP_WIDTH - 1) + ").");
            return;
        }

        // Identifica se é o Jogador 0 ou Jogador 1 baseado na ordem da lista
        int indexAtual = handlers.indexOf(cliente);
        int indexAdversario = (indexAtual == 0) ? 1 : 0;

        Position novaPosicao = new Position(x, y);
        Position posAdversario = posicoesIniciais[indexAdversario];

        // 2. Comparação direta sem 'for' loop (usa o equals automático do record Position)
        if (posAdversario != null && posAdversario.equals(novaPosicao)) {
            cliente.send("ERROR: Esta posição já foi escolhida pelo outro jogador!");
            return;
        }

        // 3. Salva a posição na variável do jogador correspondente
        posicoesIniciais[indexAtual] = novaPosicao;
        cliente.send("POSITION_CONFIRMED: Posição (" + x + ", " + y + ") registrada!");

        // 4. Se ambos já tiverem valor salvo, inicia o jogo
        if (posicoesIniciais[0] != null && posicoesIniciais[1] != null) {
            session.iniciarJogoOficialmente();
        } else {
            cliente.send("WAITING: Aguardando o outro jogador...");
        }
    }

    public void processarMovimento(ClientHandler cliente, String msg) {
        cliente.send("MOVE_OK: " + msg);
    }

    public void iniciarPartida() {
        System.out.println("[GameManager] Partida iniciada na sala " + session.getId());
    }

    public void encerrarPartida() {
        System.out.println("[GameManager] Encerrando partida na sala " + session.getId());
    }
}
