package Servidor;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Servidor implements Runnable {
    private Socket cliente;
    private boolean conexao = true;
    private PrintStream saida;
    private Scanner teclado;

    public Servidor(Socket c) {
        this.cliente = c;
    }

    public void run() {
        try {
            teclado = new Scanner(System.in);
            saida = new PrintStream(cliente.getOutputStream());
            Scanner entrada = new Scanner(cliente.getInputStream());

            String mensagem;

            while (conexao) {
                

                // Envia mensagem para o firewall
                System.out.println("Digite uma mensagem para enviar ao firewall: ");
                mensagem = "chavepublica";
                if (mensagem.equals("fim")) {
                    conexao = false;
                    break;
                }
                saida.println(mensagem);

                // Recebe mensagem do firewall e envia para o cliente
                if (entrada.hasNextLine()) {
                    mensagem = entrada.nextLine();
                    if (mensagem.equals("fim")) {
                        conexao = false;
                        break;
                    }
                    System.out.println("Mensagem recebida do firewall: " + mensagem);
                }
            }

            // Fechar conexões
            entrada.close();
            saida.close();
            cliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
