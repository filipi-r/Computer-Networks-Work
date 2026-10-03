package client;

public class ClientGameState {
    public enum EstadoCliente {
        CONECTADO,
        AGUARDANDO_OK,
        SELECAO_POSICAO,
        EM_JOGO
    }

    private EstadoCliente estadoAtual = EstadoCliente.CONECTADO;

    public void atualizarEstado(String mensagemDoServidor) {
        if (mensagemDoServidor.contains("LOBBY_FULL")) {
            this.estadoAtual = EstadoCliente.AGUARDANDO_OK;
        } else if (mensagemDoServidor.contains("SELECT_POSITION")) {
            this.estadoAtual = EstadoCliente.SELECAO_POSICAO;
        } else if (mensagemDoServidor.contains("GAME_START")) {
            this.estadoAtual = EstadoCliente.EM_JOGO;
        }
    }

    public EstadoCliente getEstadoAtual() {
        return estadoAtual;
    }
}
