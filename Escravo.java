import java.io.*;
import java.net.*;

public class Escravo {

    public static void main(String[] args) {
        Processo processo = null;

        try {
            processo = new Processo(args, false);

            System.out.println("Escravo " + processo.idProcesso +
                    " iniciado na porta " + processo.portaLocal);
            System.out.println("Aguardando o token...");

            for (int voltaAtual = 1; voltaAtual <= processo.numeroDeVoltas; voltaAtual++) {
                int token = processo.receberToken();

                processo.mudarTempoDeEspera(15000);

                System.out.println("Volta " + voltaAtual + ": Escravo " +
                        processo.idProcesso + " recebeu " + token + ".");

                token = processo.calcularToken(token);
                System.out.println("Escravo " + processo.idProcesso +
                        " atualizou o token para " + token + ".");

                processo.enviarToken(token);
                System.out.println("Escravo " + processo.idProcesso +
                        " enviou " + token + " ao proximo processo.");
            }

            System.out.println("Escravo " + processo.idProcesso + " terminou suas voltas.");

        } catch (SocketTimeoutException erro) {
            System.out.println("Tempo esgotado: Escravo nao recebeu o token.");
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
