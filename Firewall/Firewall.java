package Firewall;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKey;

import Criptografia.CriptoRSA;
import Criptografia.CriptografiaAES;
import Criptografia.ImplSHA3;

public class Firewall implements Runnable {
    private Socket socketCliente;
    private Socket socketServidor;
    private boolean conexaoTrocaDeMensagens = true;
    private boolean conexaoTrocaDeChavesRSA = true;
    private boolean conexaoEnvioChaveAES = true;
    private boolean loginCorreto = true;

    private BigInteger eChavefirewall;
    private BigInteger nChavefirewall;
    private BigInteger dChavefirewall;
    private String ChavePublicaCliente;

    private String algoritmoHash;

    private CriptoRSA criptoRSA = new CriptoRSA();

    private CriptografiaAES criptoAES;
    private SecretKey chaveAES;

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

            //variável que recebe a mensagem enviada pelo cliente, cifrada em AES
            String MensagemAES;

            int cabecalho = -1;

            //armazena o valor inteiro que foi convertido de um texto
            //vai comçar como -1, pois se for não for nenhum valor conhecido, irei retornar erro
            int opcao = -1;

            //variável que receebe a mensagem enviada pelo cliente, o hash da mensagem AES cifrado com RSA
            String MensagemRSAComHash;

            //Armazena os bytes do hash do texto cifrado do algoritmo AES
            byte[] hashDoTextoCifradoAES;
            
            //resultado no formado string do hash do texto cifrado AES
            String resultadoDoHash;

            //variável que armazena a mensagem aes enviada pelo servidor, decifrada.
            String decifraAESdaMensagem;

            //armazena o hash cifrado com RSA
            String hashCifradaComRSA;

            //texto cifrado em AES
            String cifrado = "";

            //armazena numero inteiro no formato texto
            String inteiroParaTexto;

            //variavel de confirmação
            //caso seja -1, entao 
            int confimacao = -1;

            //armazena número da conta do cliente
            String conta = "";

            //armazena senha da conta do cliente
            String senha = "";

            //array que armazena numero da conta e senha concatenados
            String[] contaESenha;

            //algoritmo hash usado
            algoritmoHash = "SHA-256";

            String decifraRSAdaMensagem;

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


            //envio(usando RSA) da chave AES para o cliente 
            if (conexaoEnvioChaveAES) {

                //gerar chave AES
                try {
                    chaveAES = criptoAES.gerarChave();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                byte[] chaveemBytes = chaveAES.getEncoded();

                String chaveEmString = Base64.getEncoder().encodeToString(chaveemBytes);

                String ChaveCifrada = criptoRSA.encriptar(chaveEmString, eCliente, nCliente);

                saidaCliente.println(ChaveCifrada);
            

                conexaoEnvioChaveAES = false;
                
            }


            while (conexaoTrocaDeMensagens) {
               
               
               
               //se o cliente enviar uma mensagem
                //if (scannerCliente.hasNextLine()) 

                    cabecalho = scannerCliente.nextInt();

                    //limoa o buffer
                    scannerCliente.nextLine();

                    System.out.println(cabecalho);
                    //cliente quer tentar fazer login
                   if (cabecalho == 1) {
                    
                                                //Rotina de tratamento para fazer login
                                                //RECEBER
                                                        //*******************************************************************************************************
                                                            //Código para receber mensagens do cliente
                                                        
                                                                //recebe mensagem AES
                                                                MensagemAES = scannerCliente.nextLine();

                                                                //recebe mensagem hash do aes cifrada com RSA
                                                                MensagemRSAComHash = scannerCliente.nextLine();


                                                                    //Decifra RSA
                                                                    //hash da mensagem
                                                                    decifraRSAdaMensagem = criptoRSA.desencriptar(MensagemRSAComHash, eCliente, nCliente);
                                                                
                                                                    

                                                                    //faz o hash da mensagem recebida AES
                                                                    hashDoTextoCifradoAES = ImplSHA3.resumo(MensagemAES.getBytes(ImplSHA3.UTF_8), algoritmoHash); //Ver possibilidade de mudar algoritmo hash

                                                                    //armazena o resultado do hash no formato String
                                                                    resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                   

                                                                    //verifica se os hash são iguais
                                                                    if (resultadoDoHash.equals(decifraRSAdaMensagem)) {
                                                                        //como os hash bateram, entao agora eu posso decifrar a mensagem AES e usa-la
                                                                        
                                                                            //Decifra AES
                                                                                try {
                                                                                    decifraAESdaMensagem = criptoAES.decifrar(MensagemAES, chaveAES);
                                
                                                                                    if (decifraAESdaMensagem.contains(" ")) {
                                                                                        contaESenha = decifraAESdaMensagem.split(" ");
                                
                                                                                        System.out.println("passou aq");
                                                                                        conta = contaESenha[0];
                                                                                        senha = contaESenha[1];
                                                                                    }else{
                                                                                        loginCorreto = false;
                                                                                    }
                                                                                    
                                                                                    
                                                                                } catch (Exception e) {
                                                                                    
                                                                                    e.printStackTrace();
                                                                                }
                                                                        
                                                                    }

                                                        //******************************************************************************************************

                                                        if (loginCorreto) {
                                                            //envia para o servidor o login e a senha para ele verificar se consta na base de dados dele.
                                                                //envia um código informando a operação fazer login, e envia o login e senha

                                                            
                                                                saidaServidor.println(cabecalho);
                                                                saidaServidor.println(conta);
                                                                saidaServidor.println(senha);

                                                                boolean resposta = scannerServidor.nextBoolean();

                                                                if (resposta) {
                                                                    //cifra e envia uma resposta para o cliente, informando que o login foi validado.


                                                                    confimacao = 1;
                                                                        //ENVIAR
                                                                            //número para confimar que o login foi bem sucedido.
                                                                            //-------------------------------------------------------------------------------------------------
                                                                                //Envia para cliente mensagem cifrada em AES
                                                                                inteiroParaTexto = confimacao+"";

                                                                                try {
                                                                                    cifrado = criptoAES.cifrar(inteiroParaTexto, chaveAES);
                                                                                } catch (Exception e) {
                                                                                    // TODO Auto-generated catch block
                                                                                    e.printStackTrace();
                                                                                }

                                                                                saidaCliente.println(cifrado);
                                                
                                                                            
                                                                                //Envia para cliente o hash cifrado em RSA, da mensagem cifrada em AES

                                                                                    //faz o hash do texto cifrado em AES
                                                                                    hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                    resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                    //cifra o hash com RSA
                                                                                    hashCifradaComRSA = criptoRSA.encriptar(resultadoDoHash, dChavefirewall, nChavefirewall);
                                                                                    saidaCliente.println(hashCifradaComRSA);
                                                                            //-------------------------------------------------------------------------------------------------
                                                                }
                                                            
                                                        }else{

                                                        }


                   }

                   if (cabecalho == 2) {
                    
                   }
                   //se n for nem fazer login nem criar conta ele vai para essa opcao
                   else{

                   }


                
               
               
               
               
               
               

                        
                
                
                
                
                
                
                
                // if (scannerCliente.hasNextLine()) {
                //     mensagem = scannerCliente.nextLine();

                //     System.out.println(mensagem);
                //     if (mensagem.equals("fim")) {
                //         conexao = false;
                //         break;
                //     }
                //     saidaServidor.println(mensagem);
                // }

                // // Recebe mensagem do servidor e envia para o cliente
                // if (scannerServidor.hasNextLine()) {
                //     mensagem = scannerServidor.nextLine();
                //     System.out.println(mensagem);
                //     if (mensagem.equals("fim")) {
                //         conexao = false;
                //         break;
                //     }
                //     saidaCliente.println(mensagem);
                // }
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
