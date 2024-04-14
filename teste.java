import java.util.Scanner;



public class teste {
    
    public static void main(String[] args) {
        final String senhaCorreta = "senha123";
        final int MAX_TENTATIVAS = 3;
        final long TEMPO_BLOQUEIO = 10000; // 10 segundos em milissegundos
        int tentativas = 0;
        boolean bloqueado = false;
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            if (!bloqueado) {
                System.out.print("Digite a senha: ");
                String senha = scanner.nextLine();
                
                if (senha.equals(senhaCorreta)) {
                    System.out.println("Senha correta. Acesso permitido!");
                    break;
                } else {
                    tentativas++;
                    if (tentativas >= MAX_TENTATIVAS) {
                        bloqueado = true;
                    }
                    System.out.println("Senha incorreta. Tente novamente.");
                }
            } else {
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
                bloqueado = false;
                tentativas = 0;
            }
        }
        
        scanner.close();
    }

}
