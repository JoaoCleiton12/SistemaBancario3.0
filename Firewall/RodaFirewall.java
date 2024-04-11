package Firewall;

import java.net.ServerSocket;
import java.net.Socket;

public class RodaFirewall {
    public static void main(String[] args) throws Exception {
        ServerSocket socketFire = new ServerSocket(123);

        while (true) {
            Socket cliente = socketFire.accept();
            Socket servidor = new Socket("127.0.0.1", 124); // Conecta-se ao servidor
            Firewall firewall = new Firewall(cliente, servidor);
            Thread t = new Thread(firewall);
            t.start();
        }
    }
}
