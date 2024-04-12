package Servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RodaServidor {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(124);

        //dessa forma todos os clientes vao ter acesso ao mesmo banco de dados
        SistemaBancario sistema = new SistemaBancario();
        while (true) {
            Socket cliente = serverSocket.accept();
            Servidor servidor = new Servidor(cliente, sistema);
            Thread t = new Thread(servidor);
            t.start();
        }
    }
}
