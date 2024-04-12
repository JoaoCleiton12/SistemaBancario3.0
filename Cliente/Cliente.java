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
import Criptografia.CriptografiaAES;
import Criptografia.ImplSHA3;

public class Cliente implements Runnable {
    private Socket cliente;
    private boolean conexaoParaTrocaDeMensagens = true;
    private boolean conexaoTrocaDeChavesPublicaRSA = true;
    private boolean conexaoParaDistribuicaoChaveAES = true;
    private PrintStream saida;
    
    private String algoritmoHash;
    private String resultadoDoHash;
    private String hashCifradaComRSA;

    private String ChavePublicafirewall;  

    private CriptografiaAES criptoAES;

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


            //algoritmo hash usado
            algoritmoHash = "SHA-256";

            //Armazena os bytes do hash do texto cifrado do algoritmo AES
            byte[] hashDoTextoCifradoAES;

            //armazena numeros inteiros em modo texto
            String inteiroParaTexto;

            //armazena as partes da chave publica do cliente
            String letraE,LetraN, LetraEeLetraN;

            //armazena a mensagem enviada pelo servidorr, cifrada em AES
            String mensagemCifradaAES;

            //armazena a mensagem enviada pelo servidor, contendo o hash do AES cifrado em RSA
            String hashDoAESCifradoRSA;

            //armazena hash do AES decifrado
            String hashDoAESDecifrado;
             
            //armazena mensagem AES decifrada
            String decifraAESDaMensagem = "";

            //armazena o resultado da confirmação de login do servidor
            int confirmarLogin = -1;


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
                

                int menu = 0;

                String cifrado = "";

                while (menu != 3) {
                    
                    System.out.println("|********************************|");
                    System.out.println("|--------------------------------|");
                    System.out.println("|###Escolha o que deseja fazer###|");
                    System.out.println("|--------------------------------|");
                    System.out.println("|Fazer Login - 1                 |");
                    System.out.println("|Criar conta - 2                 |");
                    System.out.println("|Sair        - 3                 |");
                    System.out.print(" Digite: ");
                    menu = teclado.nextInt();
                    System.out.println("|********************************|");
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println("/////////////////////////////////////////////");
                    System.out.println();
                    System.out.println();
                    System.out.println();


                    //Cliente vai enviar um código(Cabeçalho) para o firewall ver oque o cliente deseja
                    //exemplo
                    //cliente envia o código 1 que é o codigo para fazer login, e em sequencia envia
                    //envia cabeçalho informando para o firewall qual tipo de operação que será realizada pela proxima mensagem 
                    saida.println(menu);



                    //fazer login
                    if (menu == 1) {
                            
                        System.out.println("|--------------------------------|");
                        System.out.println("|############ Login #############|");
                        System.out.println("|--------------------------------|");
                        System.out.print("|Numero da conta: ");
                        teclado.nextLine();
                        String numConta = teclado.nextLine();
                        
                        System.out.print("|Senha: ");
                        String senha = teclado.nextLine();
                        
            
                        //Concatena as mensagens
                        String numContaEsenha = numConta+ " " +senha;      


                                            
                                        //ENVIAR
                                        //----------------------------------------------------------------------------
                                            //Envia AES
                                                //cifrar e enviar
                                                try {
                                                    cifrado = criptoAES.cifrar(numContaEsenha, chaveAESFirewall);
                                                    System.out.println("mensagem aes cifrada: "+cifrado);
                                                    
                                                } catch (Exception e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }
                                                saida.println(cifrado);
                                                saida.flush();
                                                
                                        
                                            //Envia RSA com hash
                                                //cifrar e enviar
                                                    
                                                    //Faz o hash do texto cifrado AES
                                                    hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash); //Ver possibilidade de mudar algoritmo hash
                                                    resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                    //cifra Hash com RSA/assina o hash com a chave privada do remetente
                                                    hashCifradaComRSA = criptoRSA.encriptar(resultadoDoHash, criptoRSA.enviarD(), criptoRSA.enviarN());
                                                    saida.println(hashCifradaComRSA);
                                        //----------------------------------------------------------------------------


                                        //RECEBER
                                        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                                            //Recebe confirmação do servidor
                                            mensagemCifradaAES = entrada.nextLine();
                                            
                                            hashDoAESCifradoRSA = entrada.nextLine();

                                            //decifra o RSA do hash
                                            hashDoAESDecifrado = criptoRSA.desencriptar(hashDoAESCifradoRSA, eFirewall, nFirewall);

                                            //faz o hash da mensagem cifrada em AES recebida
                                            hashDoTextoCifradoAES = ImplSHA3.resumo(mensagemCifradaAES.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                            resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                            
                                            //verifica se os hash são iguais
                                            if (resultadoDoHash.equals(hashDoAESDecifrado)) {
                                                //como o hash bateu, entao eu posso fazer a decifragem da mensagem AES e usa-la
                                                try {
                                                    decifraAESDaMensagem = criptoAES.decifrar(mensagemCifradaAES, chaveAESFirewall);

                                                    confirmarLogin = Integer.parseInt(decifraAESDaMensagem);
                                                } catch (Exception e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }
                                            }
                                        //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                                        //caso o login seja válido
                                        if (confirmarLogin == 1) {
                                            System.out.println();
                                            System.out.println();
                                            System.out.println();
                                            System.out.println("/////////////////////////////////////////////");
                                            System.out.println();
                                            System.out.println();
                                            System.out.println();


                                            System.out.println("|********************************|");
                                            System.out.println("|#####  Login bem sucedido ######|");


                                            int escolha = 0;

                                            while (escolha != 6) {
                                                    System.out.println("|********************************|");
                                                    System.out.println("|--------------------------------|");
                                                    System.out.println("|Saque ........................ 1|");
                                                    System.out.println("|Depósito ..................... 2|");
                                                    System.out.println("|Transferência ................ 3|");
                                                    System.out.println("|Saldo ........................ 4|");
                                                    System.out.println("|Investimento ................. 5|");
                                                    System.out.println("|Sair ......................... 6|");
                                
                                                    System.out.print("|Digite: ");
                                                    escolha = teclado.nextInt();
                                
                                                    System.out.println("|--------------------------------|");
                                                    System.out.println("|********************************|");
                                                    System.out.println();
                                                    System.out.println();
                                                    System.out.println("////////////////////////////////////////////////");
                                                    System.out.println();
                                                    System.out.println();
                                            }
                                            
                                        }
                                        //caso o login não seja válido
                                        else{

                                            System.out.println();
                                            System.out.println();
                                            System.out.println();
                                            System.out.println("/////////////////////////////////////////////");
                                            System.out.println();
                                            System.out.println();
                                            System.out.println();
                                            System.out.println("|********************************|");
                                            System.out.println("|--------------------------------|");
                                            System.out.println("|#### Credenciais inválidas #####|");
                                            System.out.println("|--------------------------------|");
                                            System.out.println("|********************************|");
                                            System.out.println();
                                            System.out.println();
                                            System.out.println("////////////////////////////////////////////////");
                                            System.out.println();
                                            System.out.println();
                                        }


                    }


                }






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
