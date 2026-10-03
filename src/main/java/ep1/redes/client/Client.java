package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int DEFAULT_PORT = 58901;

    private static void main() throws IOException{
        final Socket socket = new Socket("localhost", DEFAULT_PORT);

        TerminalUI terminalUI = new TerminalUI();
    }
}
