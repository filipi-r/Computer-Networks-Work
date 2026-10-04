package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import client.ClientGameState.ClientState;
import common.game.BoardView;
import common.game.Direction;
import common.protocol.MessageSerializer;
import common.protocol.message_types.JoinMessage;
import common.protocol.message_types.Message;
import common.protocol.message_types.MoveMessage;
import common.protocol.message_types.ReadyMessage;
import common.protocol.message_types.ShootMessage;

/**
 * Duas threads:
 *  - principal: loop de comandos do teclado ({@link #commandLoop()})
 *  - leitora: {@link ServerConnection}, que recebe e mostra o que o servidor manda
 */
public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 58901;

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final String playerName;
    private final ClientGameState gameState;

    private volatile boolean closing = false;
    private boolean readySent = false; // só usada pela thread principal

    public Client(String playerName) throws IOException{
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        this.gameState = new ClientGameState();
        this.playerName = playerName;
    }

    public boolean updateGameState(Message message) {
        return gameState.updateState(message);
    }

    public void start() {
        send(new JoinMessage(playerName));
        Thread thread = new Thread(new ServerConnection(in, this), "server-reader");
        thread.setDaemon(true); // não segura o programa aberto depois que o loop de comandos termina
        thread.start();
    }

    public void send(Message message) {
        out.println(MessageSerializer.serialize(message));
        if(out.checkError()) {
            TerminalUI.println("[CLIENTE] conexão perdida, encerrando processo...");
            gameState.finish();
        }
    }

    /** Loop de comandos do jogador. Roda na thread principal até a partida acabar, a entrada fechar ou 'quit'. */
    public void commandLoop() {
        TerminalUI.printHelp();

        String[] command;
        while ((command = TerminalUI.getPrompt()) != null) {
            if (gameState.getState() == ClientState.FINISHED) break;
            if (command[0].isEmpty()) continue;

            switch (command[0]) {
                case "ok" -> sendReady();
                case "move" -> sendMove(command);
                case "shoot" -> sendShoot(command);
                case "board" -> showBoard();
                case "help" -> TerminalUI.printHelp();
                case "quit" -> {
                    close();
                    return;
                }
                default -> TerminalUI.println("Comando desconhecido. Digite 'help' para ver os comandos.");
            }
        }
        close();
    }

    private void sendReady() {
        if (gameState.getState() != ClientState.LOBBY_FULL) {
            TerminalUI.println("Ainda não dá para confirmar: espere o lobby encher.");
        } else if (readySent) {
            TerminalUI.println("Você já confirmou. Aguardando o outro jogador...");
        } else {
            readySent = true;
            send(new ReadyMessage());
            TerminalUI.println("Confirmado! Aguardando o servidor iniciar a partida...");
        }
    }

    private void sendMove(String[] command) {
        if (!canAct()) return;
        if (command.length != 2) {
            TerminalUI.println("Uso: move <up|down|right|left>");
            return;
        }
        try {
            send(new MoveMessage(Direction.valueOf(command[1].toUpperCase())));
        } catch (IllegalArgumentException e) {
            TerminalUI.println("Direção inválida. Uso: move <up|down|right|left>");
        }
    }

    private void sendShoot(String[] command) {
        if (!canAct()) return;
        if (command.length != 3) {
            TerminalUI.println("Uso: shoot <x> <y>   (x = linha, y = coluna)");
            return;
        }
        try {
            send(new ShootMessage(Integer.parseInt(command[1]), Integer.parseInt(command[2])));
        } catch (NumberFormatException e) {
            TerminalUI.println("x e y precisam ser números. Uso: shoot <x> <y>");
        }
    }

    private void showBoard() {
        BoardView board = gameState.getBoard();
        if (board == null) {
            TerminalUI.println("Ainda não há tabuleiro para mostrar.");
        } else {
            TerminalUI.boardRender(board);
        }
    }

    /** Confere (localmente) se o jogador pode mandar MOVE/SHOOT agora. O servidor confere de novo. */
    private boolean canAct() {
        switch (gameState.getState()) {
            case INGAME_PLAYING:
                return true;
            case INGAME_WAITING:
                TerminalUI.println("Você já agiu nesta rodada. Aguardando o outro jogador...");
                return false;
            default:
                TerminalUI.println("A partida não está em andamento.");
                return false;
        }
    }

    private void close() {
        closing = true;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public boolean isClosing() { return closing; }
    public String getPlayerName() { return playerName; }
    public ClientGameState getGameState() { return gameState; }

    public static void main(String[] args) throws IOException{

        String name = TerminalUI.readLine("> Digite seu apelido: ").trim();

        //tirando virgulas pq o protocolo separa as coisas por virgula e deu merda
        name = name.replace(",", "").replace("\\", "");
        if (name.isEmpty()) name = "jogador";

        Client client = new Client(name);
        client.start();
        client.commandLoop(); // thread principal: teclado
    }
}
