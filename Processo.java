import java.io.*;
// Importa classes gerais de entrada e saída, como IOException e SocketException.
import java.net.*;
// Importa classes de rede, como DatagramSocket, DatagramPacket e InetAddress.

public class Processo {
// Declara a classe Processo, responsável pelas configurações e pela comunicação.

    int idProcesso,
            // Identifica o processo: mestre = 0 e escravos = 1, 2 ou 3.
            tipoDeOperacao,
            // Define a operação sobre o token: 0 soma 1 e 1 multiplica por 2.
            numeroDeVoltas,
            // Quantidade de vezes que o token percorrerá o anel.
            portaLocal,
            // Porta UDP em que este processo receberá pacotes.
            portaDoProximoProcesso;
            // Porta UDP do processo seguinte no anel.

    String ipDoProximoProcesso;
    // Endereço IP do processo seguinte no anel.

    DatagramSocket socketUdp;
    // Socket UDP usado para enviar e receber dados.
    InetAddress enderecoDoProximoProcesso;
    // Representação de rede do IP do processo seguinte.

    public Processo(String[] args, boolean ehMestre) throws IOException {
    // Constrói o processo a partir dos argumentos e indica se ele é o mestre.
        if (args.length < 3 || args.length > 6) {
        // Exige entre três e seis argumentos na linha de comando.
            throw new IllegalArgumentException(
                    "Informe ID, operacao e voltas.");
            // Interrompe a criação e informa quais dados básicos são necessários.
        }

        idProcesso = Integer.parseInt(args[0]);
        // Converte o primeiro argumento para o identificador inteiro do processo.
        tipoDeOperacao = Integer.parseInt(args[1]);
        // Converte o segundo argumento para a operação inteira.
        numeroDeVoltas = Integer.parseInt(args[2]);
        // Converte o terceiro argumento para a quantidade de voltas.

        if (ehMestre && idProcesso != 0) {
        // Garante que somente o processo com ID 0 poderá ser mestre.
            throw new IllegalArgumentException("O Mestre deve ter ID 0.");
            // Mostra erro caso o identificador informado não seja 0.
        }
        if (!ehMestre && (idProcesso < 1 || idProcesso > 3)) {
        // Garante que os escravos tenham IDs válidos.
            throw new IllegalArgumentException("O Escravo deve ter ID 1, 2 ou 3.");
            // Mostra erro caso o escravo use outro identificador.
        }
        if (tipoDeOperacao != 0 && tipoDeOperacao != 1) {
        // Aceita somente soma ou multiplicação.
            throw new IllegalArgumentException("Operacao deve ser 0 (somar 1) ou 1 (multiplicar por 2).");
            // Explica os únicos valores válidos para a operação.
        }
        if (numeroDeVoltas < 1) {
        // Impede que o token circule uma quantidade zero ou negativa.
            throw new IllegalArgumentException("A quantidade de voltas deve ser maior que zero.");
            // Informa que a quantidade mínima de voltas é 1.
        }

        portaLocal = 5000 + idProcesso;
        // Calcula a porta padrão: 5000, 5001, 5002 ou 5003.
        if (idProcesso == 3) {
        // O processo 3 é o último do anel e envia ao mestre.
            portaDoProximoProcesso = 5000;
            // Faz o processo 3 enviar o token para a porta do mestre.
        } else {
        // Os demais processos calculam a porta do próximo pelo ID.
            portaDoProximoProcesso = 5001 + idProcesso;
            // Faz o processo 0 enviar ao 1, o 1 ao 2 e o 2 ao 3.
        }
        ipDoProximoProcesso = "127.0.0.1";
        // Usa localhost por padrão, mantendo todos os processos na máquina local.

        if (args.length >= 4) {
        // Permite substituir a porta local padrão.
            portaLocal = Integer.parseInt(args[3]);
            // Converte o quarto argumento em porta local.
        }
        if (args.length >= 5) {
        // Permite substituir o IP do próximo processo.
            ipDoProximoProcesso = args[4];
            // Guarda o quarto endereço informado pelo usuário.
        }
        if (args.length == 6) {
        // Permite substituir a porta do próximo processo.
            portaDoProximoProcesso = Integer.parseInt(args[5]);
            // Converte o sexto argumento em porta de destino.
        }

        if (portaLocal < 1 || portaLocal > 65535 ||
                portaDoProximoProcesso < 1 || portaDoProximoProcesso > 65535) {
        // Testa se as portas estão dentro do intervalo válido de 1 a 65535.
            throw new IllegalArgumentException("As portas devem estar entre 1 e 65535.");
            // Interrompe a execução caso alguma porta seja inválida.
        }

        enderecoDoProximoProcesso = InetAddress.getByName(ipDoProximoProcesso);
        // Converte o texto do IP para um objeto reconhecido pela API Java de rede.
        socketUdp = new DatagramSocket(portaLocal);
        // Abre o socket UDP para receber pacotes na porta local.

        if (ehMestre) {
        // Configura o tempo máximo de espera diferente para o mestre.
            socketUdp.setSoTimeout(15000);
            // Faz o mestre esperar até 15 segundos por uma resposta.
        } else {
        // Configura o tempo máximo de espera de um escravo.
            socketUdp.setSoTimeout(120000);
            // Faz o escravo esperar até 120 segundos pelo próximo token.
        }
    }

    public void enviarToken(int token) throws IOException {
    // Envia o token inteiro pela rede como uma mensagem UDP.
        String mensagemParaEnvio = String.valueOf(token);
        // Converte o número inteiro em texto antes da transmissão.
        byte[] dadosParaEnvio = mensagemParaEnvio.getBytes();
        // Converte o texto em bytes, formato exigido pelo pacote UDP.

        DatagramPacket pacoteParaEnvio = new DatagramPacket(
                dadosParaEnvio, dadosParaEnvio.length,
                enderecoDoProximoProcesso, portaDoProximoProcesso);
        // Monta o pacote com dados, tamanho, IP de destino e porta de destino.
        socketUdp.send(pacoteParaEnvio);
        // Transmite o pacote UDP pelo socket.
    }

    public int receberToken() throws IOException {
    // Aguarda e interpreta um token recebido pela porta local.
        byte[] bufferDeRecebimento = new byte[100];
        // Reserva espaço para receber até 100 bytes.
        DatagramPacket pacoteRecebido = new DatagramPacket(
                bufferDeRecebimento, bufferDeRecebimento.length);
        // Cria um pacote vazio que armazenará os dados recebidos.
        socketUdp.receive(pacoteRecebido);
        // Bloqueia até chegar um datagrama ou expirar o timeout.

        String mensagemRecebida = new String(
                pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
        // Converte somente os bytes úteis do pacote em texto.
        return Integer.parseInt(mensagemRecebida.trim());
        // Remove espaços e converte a mensagem recebida para inteiro.
    }

    public int calcularToken(int token) {
    // Aplica a operação configurada ao valor recebido.
        if (tipoDeOperacao == 0) {
        // Seleciona a operação de soma.
            return token + 1;
            // Soma uma unidade ao token.
        } else {
        // Seleciona a operação de multiplicação.
            return token * 2;
            // Multiplica o token por dois.
        }
    }

    public void mudarTempoDeEspera(int tempoEmMilissegundos) throws SocketException {
    // Altera o timeout atual do socket UDP.
        socketUdp.setSoTimeout(tempoEmMilissegundos);
        // Configura quantos milissegundos receive pode aguardar.
    }

    public void fechar() {
    // Libera os recursos de rede usados pelo processo.
        if (socketUdp != null) {
        // Verifica se o socket foi criado antes de tentar fechá-lo.
            socketUdp.close();
            // Fecha o socket e libera sua porta local.
        }
    }
}
