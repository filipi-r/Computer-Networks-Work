package client;

import java.util.Scanner;

import common.game.BoardView;
import common.game.Position;
import common.game.TileType;

public class TerminalUI {

    private static final Scanner stdin = new Scanner(System.in);

    // Duas threads escrevem no terminal (principal e leitora do servidor). Tudo passa por println(),
    // que usa esta trava, e promptShown diz se o "> " está na tela esperando o jogador digitar.
    private static final Object LOCK = new Object();
    private static final String PROMPT = "> ";
    private static boolean promptShown = false;

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
        
        println(board.toString());
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

    /**
     * Escreve uma mensagem de forma segura entre as threads. Se o prompt estiver na tela, ele é apagado
     * para a mensagem ocupar o lugar dele e é reimpresso DEPOIS dela, então o "> " fica sempre por último.
     * (Texto já digitado e ainda não enviado com Enter pode ficar visualmente bagunçado.)
     */
    public static void println(String text) {
        synchronized (LOCK) {
            if (promptShown) System.out.print("\r  \r");
            System.out.println(text);
            if (promptShown) System.out.print(PROMPT);
            System.out.flush();
        }
    }

    /** Mostra o prompt e lê um comando já separado em palavras. Retorna null se a entrada acabou. */
    public static String[] getPrompt() {
        synchronized (LOCK) {
            System.out.print(PROMPT);
            System.out.flush();
            promptShown = true;
        }

        boolean hasLine = stdin.hasNextLine(); // bloqueia fora da trava, senão a thread leitora travaria junto
        String line = hasLine ? stdin.nextLine() : null;

        synchronized (LOCK) {
            promptShown = false; // o Enter do jogador já levou o cursor para a próxima linha
        }
        return hasLine ? line.trim().toLowerCase().split("\\s+") : null;
    }

    /** Pergunta algo e devolve a linha digitada (usa o mesmo Scanner do resto, para não perder entrada). */
    public static String readLine(String question) {
        synchronized (LOCK) {
            System.out.print(question);
            System.out.flush();
        }
        return stdin.hasNextLine() ? stdin.nextLine() : "";
    }

    public static boolean hasNextLine() {
        return stdin.hasNextLine();
    }

    public static void printHelp() {
        println("""
                Comandos:
                  ok               confirma que está pronto (quando o lobby encher)
                  move <up|down|right|left>   move o barco uma casa (cima, baixo, direita, esquerda)
                  shoot <x> <y>    atira na casa (x = linha, y = coluna)
                  Os dois jogadores agem ao mesmo tempo. Cada rodada permite 1 MOVE e 1 SHOOT por jogador
                  e só termina quando os dois usarem as duas ações.
                  board            redesenha o tabuleiro
                  help             mostra os comandos
                  quit             fecha o programa
                """.stripTrailing());
    }
}
