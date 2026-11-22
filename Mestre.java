import java.io.*;
import java.net.*;

public class Mestre {

    public static void main(String[] args) {
        Processo processo = null;

        try {
            processo = new Processo(args, true);

            int token = 2;
            System.out.println("Mestre iniciado na porta " + processo.portaLocal);
            System.out.println("Token inicial: " + token);

            for (int voltaAtual = 1; voltaAtual <= processo.numeroDeVoltas; voltaAtual++) {
                System.out.println("\n--- VOLTA " + voltaAtual + " ---");

                processo.enviarToken(token);
                System.out.println("Mestre enviou " + token + " ao Escravo 1.");

                token = processo.receberToken();
                System.out.println("Mestre recebeu " + token + " do Escravo 3.");

                token = processo.calcularToken(token);
                System.out.println("Mestre atualizou o token para " + token + ".");
            }

            System.out.println("\nRESULTADO FINAL: " + token);

        } catch (SocketTimeoutException erro) {
            System.out.println("Tempo esgotado: o token nao voltou ao Mestre.");
        } catch (SocketException erro) {
            System.out.println("Erro na porta UDP: " + erro.getMessage());
        } catch (IOException erro) {
            System.out.println("Erro de comunicacao: " + erro.getMessage());
        } catch (NumberFormatException erro) {
            System.out.println("Erro: ID, operacao, voltas, porta ou token nao e numero valido.");
        } catch (IllegalArgumentException erro) {
            System.out.println("Parametros invalidos: " + erro.getMessage());
        } finally {
            if (processo != null) {
                processo.fechar();
            }
        }
    }
}
