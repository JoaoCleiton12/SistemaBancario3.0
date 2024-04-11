package Cliente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class RodaCliente {
    
    public static void main(String[] args) throws IOException , UnknownHostException{
        Socket socketFirewall = new Socket("127.0.0.1", 123); // Conecta-se ao firewall
        InetAddress inet = socketFirewall.getInetAddress();



       Cliente c = new Cliente(socketFirewall);
       Thread t = new Thread(c);
       t.start();
    }
}