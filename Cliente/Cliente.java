package Cliente;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Criptografia.CriptoRSA;

public class Cliente implements Runnable {
    private Socket cliente;
    private boolean conexaoParaTrocaDeMensagens = true;
    private boolean conexaoTrocaDeChavesPublicaRSA = true;
    private boolean conexaoParaDistribuicaoChaveAES = true;
    private PrintStream saida;
    
    private String ChavePublicafirewall;  


    private CriptoRSA criptoRSA = new CriptoRSA();

    private SecretKey chaveAESFirewall;



    private Scanner teclado;

    BigInteger eFirewall = new BigInteger("123123");
    BigInteger nFirewall = new BigInteger("123213");

    public Cliente(Socket c) {
        this.cliente = c;
    }

    public void run() {
        try {
            teclado = new Scanner(System.in);
            saida = new PrintStream(cliente.getOutputStream());
            Scanner entrada = new Scanner(cliente.getInputStream());

            String mensagem;


            //armazena as partes da chave publica do cliente
            String letraE,LetraN, LetraEeLetraN;
             


             //Troca de chaves publica do RSA entre cliente e firewall
             if (conexaoTrocaDeChavesPublicaRSA) {

                //Recebe chave publica do firewall
                //que será usada para cifrar a mensagem do cliente para o firewall
                //o firewall usará sua chave privada para decfifrar a mensagem
                ChavePublicafirewall = entrada.nextLine();


                //Pega chave do firewall
                //tira a concatenação, alterar 
                String[] array = ChavePublicafirewall.split(" ");

                String LetraEServidor = array[0];
                String LetraNServidor = array[1];

                eFirewall = new BigInteger(LetraEServidor);
                nFirewall = new BigInteger(LetraNServidor);

                //Gera chave publica do cliente
                //recebe os valores
                BigInteger e = criptoRSA.enviarE();
                BigInteger n = criptoRSA.enviarN();

                //converte para string
                letraE = e.toString();
                LetraN = n.toString();
                
                //concateno ambos
                LetraEeLetraN = letraE+ " " +LetraN;
                
                //Envia chave publica do cliente para o firewall.
                saida.println(LetraEeLetraN);

                conexaoTrocaDeChavesPublicaRSA = false;
            }


            //recebimento de chave do AES (cifrada em RSA) enviada pelo firewall
            if (conexaoParaDistribuicaoChaveAES) {
                
                String m = entrada.nextLine();
                
                String chaveDecifrada = criptoRSA.desencriptar(m, criptoRSA.enviarD(), criptoRSA.enviarN());

                byte[] chaveFinal = Base64.getDecoder().decode(chaveDecifrada);

                chaveAESFirewall = new SecretKeySpec(chaveFinal, "AES");

                conexaoParaDistribuicaoChaveAES = false;
                    
            };




            while (conexaoParaTrocaDeMensagens) {
                

                // Envia mensagem para o firewall
                









                // // Envia mensagem para o firewall
                // System.out.println("Digite uma mensagem para enviar ao firewall: ");
                // mensagem = teclado.nextLine();
                // if (mensagem.equals("fim")) {
                //     conexaoParaTrocaDeMensagens = false;
                //     break;
                // }
                // saida.println(mensagem);

                // // Recebe mensagem do firewall
                // if (entrada.hasNextLine()) {
                //     mensagem = entrada.nextLine();
                //     if (mensagem.equals("fim")) {
                //         conexaoParaTrocaDeMensagens = false;
                //         break;
                //     }
                //     System.out.println("Mensagem recebida do firewall: " + mensagem);
                // }
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
