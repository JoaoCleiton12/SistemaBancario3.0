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

            //armazena valor inteiro que foi convertido de um texto
             int escolha = -1;

            //armazena valor que sera usado nas operações
            double valor = 0;

            //variavel de confirmação
            //caso seja -1, entao 
            int confimacao = -1;

            //armazena número da conta do cliente
            String conta = "";

            //armazena senha da conta do cliente
            String senha = "";

            int bloquear = 0;

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

                    //limoa o buffer da comunicação com o cliente
                    scannerCliente.nextLine();

                    
                    //cliente quer tentar fazer login
                   if (cabecalho == 1) {
                    
                    System.out.println("Operação de login..");
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

                                                        

                                                        //enviar para servidor e ver se o login bate 

                                                        System.out.println(cabecalho);

                                                        saidaServidor.println(cabecalho);
                                                        saidaServidor.println(conta);
                                                        saidaServidor.println(senha);

                                                        boolean respostaBol = scannerServidor.nextBoolean();


                                                        System.out.println(respostaBol);

                                                        if (respostaBol) {
                                                            //envia para o servidor o login e a senha para ele verificar se consta na base de dados dele.
                                                                //envia um código informando a operação fazer login, e envia o login e senha

                                                                //se fizer login, zera a variável que controla se o cliente deve ser desconectado
                                                                //bloquear = 0;

                                                                System.out.println("Login feito..");

                                                               

                                                               
                                                               

                                                                // if (scannerServidor.hasNext()) {
                                                                //      //limpa o buffer da comunicação com o servidor
                                                                //     scannerServidor.nextLine();
                                                                // }

                                                                if (respostaBol) {
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

                                                                            escolha = -1;

                                                                            while (escolha != 6) {
                                                                                
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
                                                                
                                                                                                                    escolha = Integer.parseInt(decifraAESdaMensagem);
                                                                                                                    
                                                                                                                    
                                                                                                                } catch (Exception e) {
                                                                                                                    
                                                                                                                    e.printStackTrace();
                                                                                                                }
                                                                                                        
                                                                                                    }

                                                                                        //******************************************************************************************************

                                                                                        System.out.println(escolha);

                                                                                        //saque
                                                                                        if (escolha == 1) {

                                                                                            System.out.println("Operação de saque...");
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
                                                                        
                                                                                                                            valor = Double.parseDouble(decifraAESdaMensagem);
                                                                                                                            
                                                                                                                        
                                                                                                                        } catch (Exception e) {
                                                                                                                            
                                                                                                                            e.printStackTrace();
                                                                                                                        }
                                                                                                                
                                                                                                            }

                                                                                                //******************************************************************************************************

                                                                                                //envia para servidor operação e os atributos necessários para fazer aquela operação
                                                                                                saidaServidor.println(escolha); 
                                                                                                saidaServidor.println(valor);

                                                                                                String respostaSaque = scannerServidor.nextLine();

                                                                                                

                                                                                                //envia resposta ao cliente
                                                                                                
                                                                                                    //ENVIAR
                                                                                                    //número para confimar que o login foi bem sucedido.
                                                                                                    //-------------------------------------------------------------------------------------------------
                                                                                                        //Envia para cliente mensagem cifrada em AES
                                                                                                       

                                                                                                        try {
                                                                                                            cifrado = criptoAES.cifrar(" Saque realizado\n"+respostaSaque, chaveAES);
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

                                                                                        //depósito
                                                                                        if (escolha == 2) {
                                                                                            
                                                                                            System.out.println("Operação de depósito..");
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
                                                                        
                                                                                                                            valor = Double.parseDouble(decifraAESdaMensagem);
                                                                                                                            
                                                                                                                        
                                                                                                                        } catch (Exception e) {
                                                                                                                            
                                                                                                                            e.printStackTrace();
                                                                                                                        }
                                                                                                                
                                                                                                            }

                                                                                                //******************************************************************************************************

                                                                                                //envia para servidor operação e os atributos necessários para fazer aquela operação
                                                                                                saidaServidor.println(escolha);
                                                                                                saidaServidor.println(valor+"");

                                                                                                //recebe resposta do servidor
                                                                                                String respostaDeposito = scannerServidor.nextLine();
                                                                                        
                                                                                                

                                                                                                //envia resposta ao cliente
                                                                                                
                                                                                                    //ENVIAR
                                                                                                    //número para confimar que o login foi bem sucedido.
                                                                                                    //-------------------------------------------------------------------------------------------------
                                                                                                        //Envia para cliente mensagem cifrada em AES
                                                                                                       

                                                                                                        try {
                                                                                                            cifrado = criptoAES.cifrar(" Depósito realizado\n"+respostaDeposito, chaveAES);
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

                                                                                        //transferencia
                                                                                        if (escolha == 3) {

                                                                                            System.out.println("Operação de transferencia..");
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

                                                                                                                String contaDestinoValor = "";
                                                                                                                

                                                                                                                //verifica se os hash são iguais
                                                                                                                if (resultadoDoHash.equals(decifraRSAdaMensagem)) {
                                                                                                                    //como os hash bateram, entao agora eu posso decifrar a mensagem AES e usa-la
                                                                                                                    
                                                                                                                        //Decifra AES
                                                                                                                            try {
                                                                                                                                decifraAESdaMensagem = criptoAES.decifrar(MensagemAES, chaveAES);
                                                                            
                                                                                                                                contaDestinoValor = decifraAESdaMensagem;
                                                                                                                                
                                                                                                                                
                                                                                                                            
                                                                                                                            } catch (Exception e) {
                                                                                                                                
                                                                                                                                e.printStackTrace();
                                                                                                                            }
                                                                                                                    
                                                                                                                }

                                                                                                    //******************************************************************************************************

                                                                                                    saidaServidor.println(escolha);
                                                                                                    saidaServidor.println(contaDestinoValor);

                                                                                                    String respostaTransfe = scannerServidor.nextLine();

                                                                                                    

                                                                                                    //ENVIAR
                                                                                                    //número para confimar que o login foi bem sucedido.
                                                                                                    //-------------------------------------------------------------------------------------------------
                                                                                                        //Envia para cliente mensagem cifrada em AES
                                                                                                       

                                                                                                        try {
                                                                                                            cifrado = criptoAES.cifrar(respostaTransfe, chaveAES);
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

                                                                                        //saldo
                                                                                        if (escolha == 4) {
                                                                                            
                                                                                            System.out.println("Operação de saldo..");
                                                                                            
                                                                                                //envia para o servidor
                                                                                                saidaServidor.println(escolha);

                                                                                                
                                                                                                String respostaSaldo = scannerServidor.nextLine();

                                                                                            

                                                                                                 //envia resposta ao cliente
                                                                                                
                                                                                                    //ENVIAR
                                                                                                    //número para confimar que o login foi bem sucedido.
                                                                                                    //-------------------------------------------------------------------------------------------------
                                                                                                        //Envia para cliente mensagem cifrada em AES
                                                                                                       

                                                                                                        try {
                                                                                                            cifrado = criptoAES.cifrar(respostaSaldo, chaveAES);
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

                                                                                        //investimentos
                                                                                        if (escolha == 5) {

                                                                                            System.out.println("Operação de investimentos..");
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
                                                                        
                                                                                                                            valor = Double.parseDouble(decifraAESdaMensagem);
                                                                                                                            
                                                                                                                        
                                                                                                                        } catch (Exception e) {
                                                                                                                            
                                                                                                                            e.printStackTrace();
                                                                                                                        }
                                                                                                                
                                                                                                            }

                                                                                                //******************************************************************************************************

                                                                                                //envia para servidor operação e os atributos necessários para fazer aquela operação
                                                                                                saidaServidor.println(escolha);
                                                                                                saidaServidor.println(valor+"");
                                                                                            
                                                                                                //recebe resposta do servidor
                                                                                                String resposta1 = scannerServidor.nextLine();
                                                                                                String resposta2 = scannerServidor.nextLine();
                                                                                                String resposta3 = scannerServidor.nextLine();
                                                                                                String resposta4 = scannerServidor.nextLine();
                                                                                                String respostaFinal = resposta1+"\n"+resposta2+"\n"+resposta3+"\n"+resposta4+"\n";
                                                                      
                                                                                                //envia resposta ao cliente
                                                                                                
                                                                                                    //ENVIAR
                                                                                                    //número para confimar que o login foi bem sucedido.
                                                                                                    //-------------------------------------------------------------------------------------------------
                                                                                                        //Envia para cliente mensagem cifrada em AES
                                                                                                       

                                                                                                        try {
                                                                                                            cifrado = criptoAES.cifrar(respostaFinal, chaveAES);
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

                                                                                        if (escolha == 6) {
                                                                                            saidaServidor.println(escolha);
                                                                                        }

                                                                            }

                                                                            System.out.println("Cliente desconectado");

                                                                            respostaBol = false;

                                                                }
                                                            
                                                        }
                                                        //Login falhou
                                                        else{
                                                            confimacao = 2;

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
                   }

                   if (cabecalho == 2) {

                    System.out.println("Operação de criar conta..");
                                                                                                String mensagem = "";
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
                                                                        
                                                                                                                            mensagem = decifraAESdaMensagem;
                                                                                                                        
                                                                                                                        } catch (Exception e) {
                                                                                                                            
                                                                                                                            e.printStackTrace();
                                                                                                                        }
                                                                                                                
                                                                                                            }

                                                                                                //******************************************************************************************************

                                                                                                saidaServidor.println(cabecalho);
                                                                                                saidaServidor.println(mensagem);

                                                                                                String resposta = scannerServidor.nextLine();

                                                                                                //ENVIAR
                                                                                                    //número para confimar que o login foi bem sucedido.
                                                                                                    //-------------------------------------------------------------------------------------------------
                                                                                                        //Envia para cliente mensagem cifrada em AES
                                                                                                       

                                                                                                        try {
                                                                                                            cifrado = criptoAES.cifrar(resposta, chaveAES);
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
                   //se n for nem fazer login nem criar conta ele vai para essa opcao
                   if (cabecalho == 3) {
                    
                   }
                   
                   //se for qualquer outro valor o firewall irá bloquear
                   if(cabecalho == 1010){
                    System.out.println("Acesso negado");

                    
                   }

                   if (cabecalho == 1011) {
                    saidaServidor.println(cabecalho);
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
