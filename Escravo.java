import java.io.*;
// Importa IOException para tratar falhas de entrada e saída.
import java.net.*;
// Importa SocketTimeoutException e SocketException para tratar erros UDP.

public class Escravo {
// Declara o processo escravo, que recebe, modifica e encaminha o token.

    public static void main(String[] args) {
    // É o ponto de entrada executado quando o comando java Escravo é chamado.
        Processo processo = null;
        // Cria uma referência vazia para permitir o fechamento seguro no final.

        try {
        // Agrupa as operações que podem lançar exceções.
            processo = new Processo(args, false);
            // Inicializa o escravo, valida os argumentos e abre o socket UDP.

            System.out.println("Escravo " + processo.idProcesso +
                    " na porta " + processo.portaLocal);
            // Exibe o ID e a porta UDP deste escravo.
            System.out.println("Esperando o token...");
            // Informa que o processo está aguardando o pacote.

            for (int voltaAtual = 1; voltaAtual <= processo.numeroDeVoltas; voltaAtual++) {
            // Repete o ciclo pelo mesmo número de voltas definido nos argumentos.
                int token = processo.receberToken();
                // Aguarda o token recebido do processo anterior.

                processo.mudarTempoDeEspera(15000);
                // Reduz o timeout para 15 segundos após a primeira operação de espera.

                System.out.println("\nVolta " + voltaAtual);
                // Exibe o número da volta atual.
                System.out.println("Recebi: " + token);
                // Exibe o valor recebido antes da operação.

                token = processo.calcularToken(token);
                // Soma 1 ou multiplica por 2 conforme a operação do escravo.
                System.out.println("Novo token: " + token);
                // Exibe o valor calculado.

                processo.enviarToken(token);
                // Encaminha o novo token ao próximo processo.
                System.out.println("Enviei: " + token);
                // Confirma o valor enviado.
            }

            System.out.println("\nFim do Escravo " + processo.idProcesso + ".");
            // Informa que este escravo concluiu todas as suas voltas.

        } catch (SocketTimeoutException erro) {
        // Trata o caso em que o token não chega dentro do tempo permitido.
            System.out.println("O token demorou demais para chegar.");
            // Informa ao usuário que ocorreu timeout no escravo.
        } catch (SocketException erro) {
        // Trata falhas relacionadas ao uso do socket ou da porta.
            System.out.println("Nao consegui usar a porta: " + erro.getMessage());
            // Mostra a mensagem técnica da falha de rede.
        } catch (IOException erro) {
        // Trata demais falhas gerais de entrada, saída ou comunicação.
            System.out.println("Falha na comunicacao: " + erro.getMessage());
            // Exibe o motivo da falha de comunicação.
        } catch (NumberFormatException erro) {
        // Trata argumentos numéricos que não puderam ser convertidos.
            System.out.println("Algum numero informado nao e valido.");
            // Orienta o usuário sobre os dados informados.
        } catch (IllegalArgumentException erro) {
        // Trata regras inválidas verificadas no construtor Processo.
            System.out.println("Confira os parametros: " + erro.getMessage());
            // Exibe a regra específica que foi violada.
        } finally {
        // Executa o bloco de limpeza independentemente do resultado.
            if (processo != null) {
            // Fecha o socket somente se o processo foi criado.
                processo.fechar();
                // Libera a porta e os recursos de rede.
            }
        }
    }
}
