package client;

import java.util.Scanner;

import common.game.BoardView;
import common.game.Position;
import common.game.TileType;

public class TerminalUI {

    private static final Scanner stdin = new Scanner(System.in);
    private static final String PROMPT = "> ";
    private static boolean promptShown = false;

    private static final char FOG = '*';
    private static final char WATER = '~';
    private static final char ME = 'B';
    private static final char ENEMY = 'E';

    public static synchronized void boardRender(BoardView view) {
        boardRender(view, "");
    }

    public static synchronized void boardRender(BoardView view, String roundStatus) {
        TileType[][] tiles = view.visibleTiles();
        Position me = view.playerPosition();
        int height = tiles.length;
        int width = tiles[0].length;

        StringBuilder board = new StringBuilder();

        board.append("\033[H\033[2J");

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
            .append(ENEMY).append(" inimigo\n\n");

        if (roundStatus != null && !roundStatus.isEmpty()) {
            board.append(roundStatus).append("\n");
        }

        if (promptShown) {
            System.out.print("\r  \r");
        }
        System.out.print(board.toString());
        if (promptShown) {
            System.out.print(PROMPT);
        }
        System.out.flush();
    }

    private static char symbol(TileType tile, int x, int y, Position me) {
        switch (tile) {
            case FOG:
                return FOG;
            case WATER:
                return WATER;
            case PLAYER:
                if (x == me.x() && y == me.y()) {
                    return ME;
                }
                return ENEMY;
            default:
                return ENEMY;
        }
    }

    public static synchronized void println(String text) {
        if (promptShown) {
            System.out.print("\r  \r");
        }
        System.out.println(text);
        if (promptShown) {
            System.out.print(PROMPT);
        }
        System.out.flush();
    }

    public static String[] getPrompt() {
        synchronized (TerminalUI.class) {
            System.out.print(PROMPT);
            System.out.flush();
            promptShown = true;
        }

        boolean hasLine = stdin.hasNextLine();
        String line = hasLine ? stdin.nextLine() : null;

        synchronized (TerminalUI.class) {
            promptShown = false;
        }

        if (hasLine) {
            return line.trim().toLowerCase().split("\\s+");
        }
        return null;
    }

    public static synchronized String readLine(String question) {
        System.out.print(question);
        System.out.flush();
        if (stdin.hasNextLine()) {
            return stdin.nextLine();
        }
        return "";
    }

    public static boolean hasNextLine() {
        return stdin.hasNextLine();
    }

    public static void printHelp() {
        println("Comandos:\n" +
                "  ok               confirma que está pronto (quando o lobby encher)\n" +
                "  move <up|down|right|left>   move o barco uma casa (cima, baixo, direita, esquerda)\n" +
                "  shoot <x> <y>    atira na casa (x = linha, y = coluna)\n" +
                "  Os dois jogadores agem ao mesmo tempo. Cada rodada permite 1 MOVE e 1 SHOOT por jogador\n" +
                "  e só termina quando os dois usarem as duas ações.\n" +
                "  board            redesenha o tabuleiro\n" +
                "  help             mostra os comandos\n" +
                "  quit             fecha o programa");
    }
}
