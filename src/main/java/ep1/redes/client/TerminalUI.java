package client;

import java.util.Scanner;

public class TerminalUI {
        public TerminalUI() {
            System.out.print("Seu nome: ");
        String name = new Scanner(System.in).nextLine().trim();
        
        // remove virgula pois o protocolo separa por virgula e ia dar uma merda (experiencia) se nao removesse
        name = name.replace(",", "").replace("\\", "");
        if (name.isEmpty()) name = "jogador";
        }
}
