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
    private SistemaBancario sistema;
    

    public Servidor(Socket c, SistemaBancario sist) {
        this.cliente = c;
        this.sistema = sist;
    }

    public void run() {
        try {
            teclado = new Scanner(System.in);
            saida = new PrintStream(cliente.getOutputStream());
            Scanner entrada = new Scanner(cliente.getInputStream());

            

            boolean resposta;

            String mensagemResposta;

            int opcao = -1;

            int operacao = 6;

            Double valor;

            String mensagem;

            String numConta;
            String senha;

            while (conexao) {
                

                // Recebe mensagem do firewall e envia para o cliente
                
                    opcao = entrada.nextInt();

                    //limpa o buffer
                    entrada.nextLine();
                    

                    System.out.println(opcao);

                    //fazer login
                    if (opcao == 1) {
                        System.out.println("Operação de login..");

                        numConta = entrada.nextLine();
                        senha = entrada.nextLine();

                        //acessar o banco de dados
                        resposta = sistema.autenticarUser(numConta, senha);
                        
                        saida.println(resposta);

                        if (resposta == true) {
                            operacao = 0;
                        }

                        

                        while (operacao != 6) {
  
                            operacao = entrada.nextInt();
                            
                            System.out.println("Login bem sucedido");
                            //saque
                            if (operacao == 1) {
                               

                                entrada.nextLine();

                                mensagem = entrada.nextLine();
                                valor = Double.parseDouble(mensagem);

                                if (valor <=sistema.saldo(numConta)) {

                                    System.out.println("Operação de saque em andamento..");

                                    sistema.saque(numConta, valor);

                                    mensagemResposta = " Saldo atual:"+sistema.saldo(numConta);
                                }else{
                                    mensagemResposta = " Saldo insuficiente";
                                }

                                saida.println(mensagemResposta);
                                
                            }
    
                            //depósito
                            if (operacao == 2) {

                                entrada.nextLine();

                                mensagem = entrada.nextLine();
                                valor = Double.parseDouble(mensagem);
                                
                                if (valor > 0.0) {
                                    System.out.println("Operação de deposito em andamento..");
                                
                                    sistema.deposito(numConta, valor);
    
                                    mensagemResposta = " Saldo atual:"+sistema.saldo(numConta);
                                }
                                else{
                                    mensagemResposta = " Valor inválido";
                                }

                                saida.println(mensagemResposta);

    
                            }

                            //transferencia
                            if (operacao == 3) {
                                System.out.println("Operação de transferencia em andamento..");

                                entrada.nextLine();

                                mensagem = entrada.nextLine();

                                String[] contaDestinoEvalor = mensagem.split(" ");

                                String contaDestino = contaDestinoEvalor[0];
                                Double valorTrans = Double.parseDouble(contaDestinoEvalor[1]);

                                String retorno = sistema.transferência(numConta, contaDestino, valorTrans);

                                saida.println(retorno);

                            }

                            //saldo
                            if (operacao == 4) {
                                System.out.println("Operação de saldo em andamento..");

                                mensagemResposta = " Saldo atual:"+sistema.saldo(numConta);

                                saida.println(mensagemResposta);
                            }

                            //Investimentos
                            if (operacao == 5) {
                               

                                entrada.nextLine();

                                mensagem = entrada.nextLine();
                                valor = Double.parseDouble(mensagem);


                                System.out.println("Operação de deposito em andamento..");

                                int tipo = 0;
                                
                                if (valor == 1) {
                                    tipo = 1;
                                }else{
                                    tipo = 2;
                                }

                               String retorno = sistema.investimentos(numConta, tipo);

                               System.out.println(retorno);

                                saida.println(retorno);

                            }


                            
                        }

                       

                    }

                    if (opcao == 2) {

                       
                        mensagem = entrada.nextLine();

                        String[] nomeCpfEnderecoTelefoneSenhaCriada;
                        String nome = "";
                        String cpf = "";
                        String endereco = "";
                        String telefone = "";
                        String senhaCriada = "";

                        nomeCpfEnderecoTelefoneSenhaCriada = mensagem.split(";");

                        nome = nomeCpfEnderecoTelefoneSenhaCriada[0];
                        cpf = nomeCpfEnderecoTelefoneSenhaCriada[1];
                        endereco = nomeCpfEnderecoTelefoneSenhaCriada[2];
                        telefone = nomeCpfEnderecoTelefoneSenhaCriada[3];
                        senhaCriada = nomeCpfEnderecoTelefoneSenhaCriada[4];

                        String retorno = sistema.criarContaCorrente(nome, cpf, endereco, telefone, senhaCriada);
                        System.out.println("Operação de criar conta...");

                        saida.println(retorno);


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
