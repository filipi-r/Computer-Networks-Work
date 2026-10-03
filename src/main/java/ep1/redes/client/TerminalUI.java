package client;

import java.util.Scanner;

import common.game.BoardView;
import common.game.Position;
import common.game.TileType;

public class TerminalUI {

    private static final Scanner stdin = new Scanner(System.in);

    private static final char FOG = '*';
    private static final char WATER = '~';
    private static final char ME = 'B';
    private static final char ENEMY = 'E';
 
    public static void boardRender(BoardView view) {
        TileType[][] tiles = view.visibleTiles();
        Position me = view.playerPosition();
        int height = tiles.length;
        int width = tiles[0].length;
 
        StringBuilder board = new StringBuilder();
 
        board.append("    ");
        for (int x = 0; x < width; x++) {
            board.append(String.format("%2d ", x));
        }
        board.append('\n');
 
        for (int x = 0; x < height; x++) {
            board.append(String.format("%2d  ", x));
            for (int y = 0; y < width; y++) {
                board.append(' ').append(symbol(tiles[x][y], x, y, me)).append(' ');
            }
            board.append('\n');
        }
 
        board.append("\n")
          .append(FOG).append(" névoa   ")
          .append(WATER).append(" água   ")
          .append(ME).append(" você   ")
          .append(ENEMY).append(" inimigo\n");
        
        System.out.println(board.toString());
    }
 
    private static char symbol(TileType tile, int x, int y, Position me) {
        switch (tile) {
            case FOG:
                return FOG;
            case WATER: 
                return WATER;
            case PLAYER:
                if(x == me.x() && y == me.y()) return ME;
            default:
                return ENEMY;
        }
    }

    public static String[] getPrompt() {
        System.out.print("> ");
        return stdin.nextLine().trim().toLowerCase().split("\\s+");
    }

    public static boolean hasNextLine() {
        return stdin.hasNextLine();
    }

    public static void printHelp() {
        System.out.println("""
                Comandos:
                  ok               confirma que está pronto (quando o lobby encher)
                  move <up|down|right|left>   move o barco uma casa (cima, baixo, direita, esquerda)
                  shoot <x> <y>    atira na casa (x = linha, y = coluna)
                  Cada rodada permite 1 MOVE e 1 SHOOT por jogador.
                  board            redesenha o tabuleiro
                  help             mostra os comandos
                  quit             fecha o programa
                """);
    }
}
