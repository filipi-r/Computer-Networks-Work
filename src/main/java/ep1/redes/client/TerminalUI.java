package client;

public class TerminalUI {

    public void exibirMensagem(String mensagem) {
        System.out.println("\n[SERVIDOR] " + mensagem);
        System.out.print("> "); // Prompt de digitação para o usuário
    }

    public void exibirPrompt() {
        System.out.print("> ");
    }
}
