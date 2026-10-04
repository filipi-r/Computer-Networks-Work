package common.game;

/*Contrato do que um jogador sabe do tabuleiro.
Server -> monta um BoardView para cada jogador e envia
Client -> recebe o BoardView*/
public interface BoardView {
    Position playerPosition();

    /** Matriz [linha][coluna] com o que o jogador enxerga (FOG onde ele não vê nada). */
    TileType[][] visibleTiles();
}
