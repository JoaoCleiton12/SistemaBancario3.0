import javax.crypto.SecretKey;

import Criptografia.CriptoRSA;
import Criptografia.CriptografiaAES;
import Criptografia.ImplSHA3;

public class teste {
    
    public static void main(String[] args) {
        CriptoRSA cripto = new CriptoRSA();
        CriptografiaAES criptaes = new CriptografiaAES();

        //texto de exemplo
        String texto = "joaocleiton idioma";


        System.out.println("texto original "+texto);


        
        
        try {
            SecretKey chaveaes = criptaes.gerarChave();


            //cifra em aes
        String cifradoaes = "";
        try {
            cifradoaes = criptaes.cifrar(texto, chaveaes);




            byte[] hashtextocifrado = ImplSHA3.resumo(cifradoaes.getBytes(ImplSHA3.UTF_8), "SHA-256");
            String resultadoHash = ImplSHA3.bytes2Hex(hashtextocifrado);


            System.out.println("hash do texto cifrado: "+resultadoHash);


            String cifrado = cripto.encriptar(resultadoHash, cripto.enviarD(), cripto.enviarN());
            

    
            System.out.println("hash cifrado/assinado co chave privada: "+ cifrado);
            

    
            String deci = cripto.desencriptar(cifrado, cripto.enviarE(), cripto.enviarN());
    
            System.out.println("texto decifrado: "+deci);
    



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        


       

       


    }
}
