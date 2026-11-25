import java.io.*;
// Importa IOException para tratar falhas de entrada e saída.
import java.net.*;
// Importa SocketTimeoutException e SocketException para tratar erros UDP.

public class Mestre {
// Declara o processo mestre, que inicia e recebe o token no final de cada volta.

    public static void main(String[] args) {
    // É o ponto de entrada executado quando o comando java Mestre é chamado.
        Processo processo = null;
        // Cria uma referência vazia para permitir o fechamento seguro no final.

        try {
        // Agrupa as operações que podem lançar exceções.
            processo = new Processo(args, true);
            // Inicializa o mestre, valida os argumentos e abre o socket UDP.

            int token = 2;
            // Define o valor inicial enviado ao primeiro escravo.
            System.out.println("Mestre na porta " + processo.portaLocal);
            // Exibe em qual porta UDP o mestre está recebendo pacotes.
            System.out.println("Token: " + token);
            // Exibe o token inicial.

            for (int voltaAtual = 1; voltaAtual <= processo.numeroDeVoltas; voltaAtual++) {
            // Repete o envio e a recepção até alcançar o número de voltas.
                System.out.println("\nVolta " + voltaAtual);
                // Exibe o número da volta atual.

                processo.enviarToken(token);
                // Envia o token atual ao próximo processo no anel.
                System.out.println("Enviei: " + token);
                // Confirma o valor enviado.

                token = processo.receberToken();
                // Aguarda o token percorrer o anel e retorna ao mestre.
                System.out.println("Recebi: " + token);
                // Exibe o token recebido após passar pelos escravos.

                token = processo.calcularToken(token);
                // Aplica a operação de soma ou multiplicação configurada no mestre.
                System.out.println("Novo token: " + token);
                // Exibe o novo valor após a operação do mestre.
            }

            System.out.println("\nFim! Token final: " + token);
            // Exibe o resultado final depois de todas as voltas.

        } catch (SocketTimeoutException erro) {
        // Trata o caso em que o retorno do token excede o tempo permitido.
            System.out.println("O token demorou demais para voltar.");
            // Informa ao usuário que ocorreu timeout no mestre.
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
