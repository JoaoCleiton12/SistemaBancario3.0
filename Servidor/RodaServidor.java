package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RodaServidor {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(124);

        while (true) {
            Socket cliente = serverSocket.accept();
            Servidor servidor = new Servidor(cliente);
            Thread t = new Thread(servidor);
            t.start();
        }
    }
}
