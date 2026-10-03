package client;

import java.util.Scanner;

public class TerminalUI {

    private final Scanner stdin;

    public TerminalUI() {
        stdin = new Scanner(System.in);
    }

    public String[] getPrompt() {
        System.out.print("> ");
        return stdin.nextLine().trim().toLowerCase().split("\\s+");
    }

    public boolean hasNextLine() {
        return stdin.hasNextLine();
    }

    public void printHelp() {
        System.out.println("""
                Comandos:
                  ok               confirma que está pronto (quando o lobby encher)
                  move <up|down|right|left>   move o barco uma casa (cima, baixo, direita, esquerda)
                  shoot <x> <y>    atira na casa (x = linha, y = coluna)
                  board            redesenha o tabuleiro
                  help             mostra os comandos
                  quit             fecha o programa
                """);
    }
}
