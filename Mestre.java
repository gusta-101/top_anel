import java.io.*;
import java.net.*;

public class Mestre {

    public static void main(String[] args) {
        Processo processo = null;

        try {
            processo = new Processo(args, true);

            int token = 2;
            System.out.println("Mestre na porta " + processo.portaLocal);
            System.out.println("Token: " + token);

            for (int voltaAtual = 1; voltaAtual <= processo.numeroDeVoltas; voltaAtual++) {
                System.out.println("\nVolta " + voltaAtual);

                processo.enviarToken(token);
                System.out.println("Enviei: " + token);

                token = processo.receberToken();
                System.out.println("Recebi: " + token);

                token = processo.calcularToken(token);
                System.out.println("Novo token: " + token);
            }

            System.out.println("\nFim! Token final: " + token);

        } catch (SocketTimeoutException erro) {
            System.out.println("O token demorou demais para voltar.");
        } catch (SocketException erro) {
            System.out.println("Nao consegui usar a porta: " + erro.getMessage());
        } catch (IOException erro) {
            System.out.println("Falha na comunicacao: " + erro.getMessage());
        } catch (NumberFormatException erro) {
            System.out.println("Algum numero informado nao e valido.");
        } catch (IllegalArgumentException erro) {
            System.out.println("Confira os parametros: " + erro.getMessage());
        } finally {
            if (processo != null) {
                processo.fechar();
            }
        }
    }
}
