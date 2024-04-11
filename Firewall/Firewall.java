package Firewall;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import Criptografia.CriptoRSA;

public class Firewall implements Runnable {
    private Socket socketCliente;
    private Socket socketServidor;
    private boolean conexao = true;
    private boolean conexaoTrocaDeChavesRSA = true;

    private BigInteger eChavefirewall;
    private BigInteger nChavefirewall;
    private BigInteger dChavefirewall;
    private String ChavePublicaCliente;

    private CriptoRSA criptoRSA = new CriptoRSA();

    public Firewall(Socket cliente, Socket servidor) {
        this.socketCliente = cliente;
        this.socketServidor = servidor;
    }

    public void run() {
        try {
            Scanner scannerCliente = new Scanner(socketCliente.getInputStream());
            PrintStream saidaServidor = new PrintStream(socketServidor.getOutputStream());
            Scanner scannerServidor = new Scanner(socketServidor.getInputStream());
            PrintStream saidaCliente = new PrintStream(socketCliente.getOutputStream());

            String mensagem;

            BigInteger eCliente = new BigInteger("123123");
            BigInteger nCliente = new BigInteger("123213");

            //armazena as partes da chave publica do cliente
            String letraE,LetraN, LetraEeLetraN;

            if (conexaoTrocaDeChavesRSA) {

                //gera chave do firewall
                //recebe os valores
                eChavefirewall = criptoRSA.enviarE();
                nChavefirewall = criptoRSA.enviarN();
                dChavefirewall = criptoRSA.enviarD();

                //converte para string
                letraE = eChavefirewall.toString();
                LetraN = nChavefirewall.toString();
                
                //concateno ambos
                LetraEeLetraN = letraE+ " " +LetraN;

                //envia a chave publica do firewall ao cliente
                saidaCliente.println(LetraEeLetraN);
                saidaCliente.flush();



                //Pega chave do cliente
                //recebe a chave do cliente
                ChavePublicaCliente = scannerCliente.nextLine();
                
                //tira a concatenação
                String[] array = ChavePublicaCliente.split(" ");

                String LetraECliente = array[0];
                String LetraNCliente = array[1];


                eCliente = new BigInteger(LetraECliente);
                nCliente = new BigInteger(LetraNCliente);
                    
                conexaoTrocaDeChavesRSA = false;

            }


            while (conexao) {
                // Recebe mensagem do cliente e envia para o servidor
                if (scannerCliente.hasNextLine()) {
                    mensagem = scannerCliente.nextLine();

                    System.out.println(mensagem);
                    if (mensagem.equals("fim")) {
                        conexao = false;
                        break;
                    }
                    saidaServidor.println(mensagem);
                }

                // Recebe mensagem do servidor e envia para o cliente
                if (scannerServidor.hasNextLine()) {
                    mensagem = scannerServidor.nextLine();
                    System.out.println(mensagem);
                    if (mensagem.equals("fim")) {
                        conexao = false;
                        break;
                    }
                    saidaCliente.println(mensagem);
                }
            }

            // Fechar conexões
            scannerCliente.close();
            scannerServidor.close();
            saidaCliente.close();
            saidaServidor.close();
            socketCliente.close();
            socketServidor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
