import java.io.*;
import java.net.*;

public class Processo {

    int idProcesso,
            tipoDeOperacao,
            numeroDeVoltas,
            portaLocal,
            portaDoProximoProcesso;

    String ipDoProximoProcesso;

    DatagramSocket socketUdp;
    InetAddress enderecoDoProximoProcesso;

    public Processo(String[] args, boolean ehMestre) throws IOException {
        if (args.length < 3 || args.length > 6) {
            throw new IllegalArgumentException(
                    "Informe: ID OPERACAO VOLTAS [PORTA_LOCAL IP_PROXIMO PORTA_PROXIMA]");
        }

        idProcesso = Integer.parseInt(args[0]);
        tipoDeOperacao = Integer.parseInt(args[1]);
        numeroDeVoltas = Integer.parseInt(args[2]);

        if (ehMestre && idProcesso != 0) {
            throw new IllegalArgumentException("O Mestre deve ter ID 0.");
        }
        if (!ehMestre && (idProcesso < 1 || idProcesso > 3)) {
            throw new IllegalArgumentException("O Escravo deve ter ID 1, 2 ou 3.");
        }
        if (tipoDeOperacao != 0 && tipoDeOperacao != 1) {
            throw new IllegalArgumentException("Operacao deve ser 0 (somar 1) ou 1 (multiplicar por 2).");
        }
        if (numeroDeVoltas < 1) {
            throw new IllegalArgumentException("A quantidade de voltas deve ser maior que zero.");
        }

        portaLocal = 5000 + idProcesso;
        if (idProcesso == 3) {
            portaDoProximoProcesso = 5000;
        } else {
            portaDoProximoProcesso = 5001 + idProcesso;
        }
        ipDoProximoProcesso = "127.0.0.1";

        if (args.length >= 4) {
            portaLocal = Integer.parseInt(args[3]);
        }
        if (args.length >= 5) {
            ipDoProximoProcesso = args[4];
        }
        if (args.length == 6) {
            portaDoProximoProcesso = Integer.parseInt(args[5]);
        }

        if (portaLocal < 1 || portaLocal > 65535 ||
                portaDoProximoProcesso < 1 || portaDoProximoProcesso > 65535) {
            throw new IllegalArgumentException("As portas devem estar entre 1 e 65535.");
        }

        enderecoDoProximoProcesso = InetAddress.getByName(ipDoProximoProcesso);
        socketUdp = new DatagramSocket(portaLocal);

        if (ehMestre) {
            socketUdp.setSoTimeout(15000);
        } else {
            socketUdp.setSoTimeout(120000);
        }
    }

    public void enviarToken(int token) throws IOException {
        String mensagemParaEnvio = String.valueOf(token);
        byte[] dadosParaEnvio = mensagemParaEnvio.getBytes();

        DatagramPacket pacoteParaEnvio = new DatagramPacket(
                dadosParaEnvio, dadosParaEnvio.length,
                enderecoDoProximoProcesso, portaDoProximoProcesso);
        socketUdp.send(pacoteParaEnvio);
    }

    public int receberToken() throws IOException {
        byte[] bufferDeRecebimento = new byte[100];
        DatagramPacket pacoteRecebido = new DatagramPacket(
                bufferDeRecebimento, bufferDeRecebimento.length);
        socketUdp.receive(pacoteRecebido);

        String mensagemRecebida = new String(
                pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
        return Integer.parseInt(mensagemRecebida.trim());
    }

    public int calcularToken(int token) {
        if (tipoDeOperacao == 0) {
            return token + 1;
        } else {
            return token * 2;
        }
    }

    public void mudarTempoDeEspera(int tempoEmMilissegundos) throws SocketException {
        socketUdp.setSoTimeout(tempoEmMilissegundos);
    }

    public void fechar() {
        if (socketUdp != null) {
            socketUdp.close();
        }
    }
}
