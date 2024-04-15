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
    private boolean desbloqueado = true;
    private PrintStream saida;

    final long TEMPO_BLOQUEIO = 10000; // 10 segundos em milissegundos
    
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

            //armazena numero decimal, referente ao valor
            String doubleParaTexto;

            int bloquear = 0;

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
                

                int contadorSenha = 0;
                // Envia mensagem para o firewall
                

                int menu = 0;

                String cifrado = "";
                

                while ((menu != 3) ) {
                    
                   

                    if (contadorSenha == 3) {
                        desbloqueado = false;
                    }
                    
                    if (desbloqueado == false) {
                        System.out.println("Você excedeu o número máximo de tentativas. Aguarde 10 segundos e tente novamente.");
                        long inicioBloqueio = System.currentTimeMillis();
    
                        while (System.currentTimeMillis() < inicioBloqueio + TEMPO_BLOQUEIO) {
                            long tempoRestante = (inicioBloqueio + TEMPO_BLOQUEIO - System.currentTimeMillis()) / 1000;
                            System.out.print(tempoRestante + "... ");
                            try {
                                Thread.sleep(1000); // Espera 1 segundo
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                    }
                    System.out.println("\nTente novamente após o tempo de bloqueio.");
                    desbloqueado = true;
                    contadorSenha = 0;
                    }



                    //O firewall irá desconectar o cliente se ele receber dois bloqueios seguidos do firewall
                    if (bloquear == 2) {

                        System.out.println(" Cliente desconectado");
                        menu = 3;
                        conexaoParaTrocaDeMensagens = false;
                        desbloqueado = false;
                    }

                    if(desbloqueado == true){


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
                    



                    //fazer login
                    if (menu == 1) {

                        saida.println(menu);
                            
                        //se fizer login, zera a variável que controla se o cliente deve ser desconectado
                       bloquear = 0;

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

                                            //se o login for bem sucedido, zerar o contador
                                            contadorSenha = 0;

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


                                                                    //ENVIAR
                                                                    //operação o cliente vai fazer
                                                                    //----------------------------------------------------------------------------
                                                                                    //Envia AES
                                                                                        //cifrar e enviar

                                                                                        inteiroParaTexto = escolha+"";

                                                                                        try {
                                                                                            cifrado = criptoAES.cifrar(inteiroParaTexto, chaveAESFirewall);
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                        saida.println(cifrado);
                                                                                
                                                                                    //Envia RSA com hash
                                                                                        //cifrar e enviar
                                                                                            
                                                                                            //Faz o hash do texto cifrado AES
                                                                                            hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                            resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                            //cifra Hash com RSA
                                                                                            hashCifradaComRSA = criptoRSA.encriptar(resultadoDoHash, criptoRSA.enviarD(), criptoRSA.enviarN());
                                                                                            saida.println(hashCifradaComRSA);
                                                                                //----------------------------------------------------------------------------

                                                                                

                                
                                                    System.out.println("|--------------------------------|");
                                                    System.out.println("|********************************|");
                                                    System.out.println();
                                                    System.out.println();
                                                    System.out.println("////////////////////////////////////////////////");
                                                    System.out.println();
                                                    System.out.println();


                                                    //caso o usuário escolha fazer um saque
                                                    if (escolha == 1) {
                                                        System.out.println("|********************************|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|############# Saque ############|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.print("|Valor: ");
                                                        double saque = teclado.nextDouble();
                                                                    //ENVIAR
                                                                    //----------------------------------------------------------------------------
                                                                                    //Envia AES
                                                                                        //cifrar e enviar

                                                                                        doubleParaTexto = saque+"";

                                                                                        try {
                                                                                            cifrado = criptoAES.cifrar(doubleParaTexto, chaveAESFirewall);
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                        saida.println(cifrado);
                                                                                
                                                                                    //Envia RSA com hash
                                                                                        //cifrar e enviar
                                                                                            
                                                                                            //Faz o hash do texto cifrado AES
                                                                                            hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                            resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                            //cifra Hash com RSA
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

                                                                                    String resposta = "";

                                                                                    //verifica se os hash são iguais
                                                                                    if (resultadoDoHash.equals(hashDoAESDecifrado)) {
                                                                                        //como o hash bateu, entao eu posso fazer a decifragem da mensagem AES e usa-la
                                                                                        try {
                                                                                            decifraAESDaMensagem = criptoAES.decifrar(mensagemCifradaAES, chaveAESFirewall);

                                                                                            resposta = decifraAESDaMensagem;
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                    }
                                                                                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



                                                        System.out.println(resposta);                           
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|********************************|");
                                                        System.out.println();
                                                        System.out.println();
                                                        System.out.println("////////////////////////////////////////////////");
                                                        System.out.println();
                                                        System.out.println();                  

                                                    }

                                                    //caso usuário escolha fazer um depósito
                                                    if (escolha == 2) {

                                                        System.out.println("|********************************|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|########### Depósito ###########|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.print("|Valor: ");
                                                        double deposito = teclado.nextDouble();

                                                                                //ENVIAR
                                                                                //----------------------------------------------------------------------------
                                                                                                //Envia AES
                                                                                        //cifrar e enviar

                                                                                        doubleParaTexto = deposito+"";

                                                                                        try {
                                                                                            cifrado = criptoAES.cifrar(doubleParaTexto, chaveAESFirewall);
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                        saida.println(cifrado);
                                                                                
                                                                                    //Envia RSA com hash
                                                                                        //cifrar e enviar
                                                                                            
                                                                                            //Faz o hash do texto cifrado AES
                                                                                            hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                            resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                            //cifra Hash com RSA
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

                                                                                    String resposta = "";

                                                                                    //verifica se os hash são iguais
                                                                                    if (resultadoDoHash.equals(hashDoAESDecifrado)) {
                                                                                        //como o hash bateu, entao eu posso fazer a decifragem da mensagem AES e usa-la
                                                                                        try {
                                                                                            decifraAESDaMensagem = criptoAES.decifrar(mensagemCifradaAES, chaveAESFirewall);

                                                                                            resposta = decifraAESDaMensagem;
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                    }
                                                                                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


                                                        System.out.println(resposta);
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|********************************|");
                                                        System.out.println();
                                                        System.out.println();
                                                        System.out.println("////////////////////////////////////////////////");
                                                        System.out.println();
                                                        System.out.println();

                                                    }

                                                    //caso o usuário escolha fazer uma transferencia
                                                    if (escolha == 3) {
                                                        System.out.println("|********************************|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|######### Transferência ########|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.print("|Conta Origem: ");
                                                        teclado.nextLine();
                                                        String numContaOrigem = teclado.nextLine();
                                                        System.out.print("|Conta destino: ");
                                                        String numContaDestino = teclado.nextLine();
                                                        System.out.print("|Valor: ");
                                                        double valorTransferencia = teclado.nextDouble();
                                                        System.out.print("|Senha: ");
                                                        teclado.nextLine();
                                                        String senhaContaOrigem = teclado.nextLine();


                                                        if (numContaOrigem.equals(numConta)) {
                                                            
                                                                if (senha.equals(senhaContaOrigem)) {
                                                                    
                                                                                //ENVIAR
                                                                                //----------------------------------------------------------------------------
                                                                                                //Envia AES
                                                                                        //cifrar e enviar

                                                                                        doubleParaTexto = numContaDestino+" "+valorTransferencia;

                                                                                        try {
                                                                                            cifrado = criptoAES.cifrar(doubleParaTexto, chaveAESFirewall);
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                        saida.println(cifrado);
                                                                                
                                                                                    //Envia RSA com hash
                                                                                        //cifrar e enviar
                                                                                            
                                                                                            //Faz o hash do texto cifrado AES
                                                                                            hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                            resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                            //cifra Hash com RSA
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

                                                                                    String resposta = "";

                                                                                    //verifica se os hash são iguais
                                                                                    if (resultadoDoHash.equals(hashDoAESDecifrado)) {
                                                                                        //como o hash bateu, entao eu posso fazer a decifragem da mensagem AES e usa-la
                                                                                        try {
                                                                                            decifraAESDaMensagem = criptoAES.decifrar(mensagemCifradaAES, chaveAESFirewall);

                                                                                            resposta = decifraAESDaMensagem;
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                    }
                                                                                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                                                                                System.out.println(resposta);

                                                                }
                                                                else{
                                                                    System.out.println(" ###Senha inválida###");
                                                                }
                                                        }
                                                        else{
                                                            System.out.println(" ##Conta de origem inválida##");
                                                        }

                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|********************************|");
                                                        System.out.println();
                                                        System.out.println();
                                                        System.out.println("////////////////////////////////////////////////");
                                                        System.out.println();
                                                        System.out.println();
            
                                                    }

                                                    //caso o usuário escolha ver o seu saldo
                                                    if (escolha == 4) {
                                                        System.out.println("|********************************|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|############# Saldo ############|");
                                                        System.out.println("|--------------------------------|");


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

                                                                                            
                                                                                        } catch (Exception e) {
                                                                                            // TODO Auto-generated catch block
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                    }
                                                                                //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


                                                        System.out.println(decifraAESDaMensagem);
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|********************************|");
                                                        System.out.println();
                                                        System.out.println();
                                                        System.out.println("////////////////////////////////////////////////");
                                                        System.out.println();
                                                        System.out.println();
                                                    }

                                                    //caso um usuário escolha fazer um investimento
                                                    if (escolha == 5) {
                                                        System.out.println("|********************************|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|######### Investimentos ########|");
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|Ver");
                                                        System.out.println("|Poupança ..................... 1|");
                                                        System.out.println("|Renda fixa ................... 2|");
                                                        System.out.println("|Digite: ");
                                                        int escolhaInvestimento = teclado.nextInt();
                                                        System.out.println("|--------------------------------|");
                                                        System.out.println("|********************************|");
                                                        System.out.println();
                                                        System.out.println();
                                                        System.out.println("////////////////////////////////////////////////");
                                                        System.out.println();
                                                        System.out.println();


                                                        if (escolhaInvestimento == 1) {
                                                            System.out.println("|********************************|");
                                                            System.out.println("|--------------------------------|");
                                                            System.out.println("|########### Poupança ###########|");
                                                            System.out.println("|--------------------------------|");

                                                                                //ENVIAR
                                                                                //----------------------------------------------------------------------------
                                                                                                //Envia AES
                                                                                                    //cifrar e enviar

                                                                                                    doubleParaTexto = escolhaInvestimento+"";

                                                                                                    try {
                                                                                                        cifrado = criptoAES.cifrar(doubleParaTexto, chaveAESFirewall);
                                                                                                    } catch (Exception e) {
                                                                                                        // TODO Auto-generated catch block
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                    saida.println(cifrado);
                                                                                            
                                                                                                //Envia RSA com hash
                                                                                                    //cifrar e enviar
                                                                                                        
                                                                                                        //Faz o hash do texto cifrado AES
                                                                                                        hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                                        resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                                        //cifra Hash com RSA
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

                                                                                                    } catch (Exception e) {
                                                                                                        // TODO Auto-generated catch block
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                                                            System.out.println(decifraAESDaMensagem);
                                                            System.out.println("|--------------------------------|");
                                                            System.out.println("|********************************|");
                                                            System.out.println();
                                                            System.out.println();
                                                            System.out.println("////////////////////////////////////////////////");
                                                            System.out.println();
                                                            System.out.println();                                

                                                        } 
                                                    
                                                        
                                                        if (escolhaInvestimento == 2) {
                                                            System.out.println("|********************************|");
                                                            System.out.println("|--------------------------------|");
                                                            System.out.println("|########## Renda fixa ##########|");
                                                            System.out.println("|--------------------------------|");
                                                            

                                                                                                                                            //ENVIAR
                                                                                            //----------------------------------------------------------------------------
                                                                                                //Envia AES
                                                                                                    //cifrar e enviar

                                                                                                    doubleParaTexto = escolhaInvestimento+"";

                                                                                                    try {
                                                                                                        cifrado = criptoAES.cifrar(doubleParaTexto, chaveAESFirewall);
                                                                                                    } catch (Exception e) {
                                                                                                        // TODO Auto-generated catch block
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                    saida.println(cifrado);
                                                                                            
                                                                                                //Envia RSA com hash
                                                                                                    //cifrar e enviar
                                                                                                        
                                                                                                        //Faz o hash do texto cifrado AES
                                                                                                        hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                                        resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                                        //cifra Hash com RSA
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

                                                                                                    } catch (Exception e) {
                                                                                                        // TODO Auto-generated catch block
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                                                            System.out.println(decifraAESDaMensagem);
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

                                        }
                                        //caso o login não seja válido
                                        else{

                                            contadorSenha++;
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

                    if (menu == 2) {
                        
                        saida.println(menu);
                        System.out.println("|********************************|");
                        System.out.println("|--------------------------------|");
                        System.out.println("|######### Criar conta ##########|");
                        System.out.println("|--------------------------------|");
                        System.out.print("|Nome completo: ");
                        teclado.nextLine();
                        String nome = teclado.nextLine();
                        System.out.print("|CPF: ");
                        String cpf = teclado.nextLine();
                        System.out.print("|Endereço: ");
                        String endereco = teclado.nextLine();
                        System.out.print("|Telefone: ");
                        String telefone = teclado.nextLine();
                        System.out.print("|Senha: ");
                        String senhaCriada = teclado.nextLine();
                        
                                                                                            //----------------------------------------------------------------------------
                                                                                                //Envia AES
                                                                                                    //cifrar e enviar

                                                                                                    doubleParaTexto = nome+";"+cpf+";"+endereco+";"+telefone+";"+senhaCriada;

                                                                                                    try {
                                                                                                        cifrado = criptoAES.cifrar(doubleParaTexto, chaveAESFirewall);
                                                                                                    } catch (Exception e) {
                                                                                                        // TODO Auto-generated catch block
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                    saida.println(cifrado);
                                                                                            
                                                                                                //Envia RSA com hash
                                                                                                    //cifrar e enviar
                                                                                                        
                                                                                                        //Faz o hash do texto cifrado AES
                                                                                                        hashDoTextoCifradoAES = ImplSHA3.resumo(cifrado.getBytes(ImplSHA3.UTF_8), algoritmoHash);
                                                                                                        resultadoDoHash = ImplSHA3.bytes2Hex(hashDoTextoCifradoAES);

                                                                                                        //cifra Hash com RSA
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

                                                                                                    } catch (Exception e) {
                                                                                                        // TODO Auto-generated catch block
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


                        System.out.println(decifraAESDaMensagem);                                            
                        System.out.println("|--------------------------------|");
                        System.out.println("|********************************|");
                        System.out.println();
                        System.out.println();
                        System.out.println("////////////////////////////////////////////////");
                        System.out.println();
                        System.out.println();


                    }

                    if (menu == 3) {
                        conexaoParaTrocaDeMensagens = false;
                    }

                    //caso esse código seja digitado, o cliente tentará acessar o backdoor
                    if (menu == 1010) {
                        saida.println(menu);

                        bloquear++;

                    }

                    if (menu == 1010) {
                        saida.println(menu);
                    }

                    }
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
