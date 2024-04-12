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

    SistemaBancario sistema;

    public Servidor(Socket c) {
        this.cliente = c;
    }

    public void run() {
        try {
            teclado = new Scanner(System.in);
            saida = new PrintStream(cliente.getOutputStream());
            Scanner entrada = new Scanner(cliente.getInputStream());

            sistema = new SistemaBancario();

            boolean resposta;

            int opcao = -1;

            String numConta;
            String senha;

            while (conexao) {
                

                // Recebe mensagem do firewall e envia para o cliente
                if (entrada.hasNextLine()) {
                    opcao = entrada.nextInt();

                    //limpa o buffer
                    entrada.nextLine();
                    

                    //fazer login
                    if (opcao == 1) {
                        
                        numConta = entrada.nextLine();
                        senha = entrada.nextLine();

                        //acessar o banco de dados
                       resposta = sistema.autenticarUser(numConta, senha);
                        System.out.println("Login bem sucedido");
                        saida.println(resposta);

                    }
                    
                }


                // // Envia mensagem para o firewall
                // System.out.println("Digite uma mensagem para enviar ao firewall: ");
                // mensagem = "chavepublica";
                // if (mensagem.equals("fim")) {
                //     conexao = false;
                //     break;
                // }
                // saida.println(mensagem);
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
