# Documentação completa do projeto de processos em Java

## 1. Objetivo

Este projeto demonstra a comunicação entre quatro processos em um anel:

1. Mestre, com ID 0.
2. Escravo 1, com ID 1.
3. Escravo 2, com ID 2.
4. Escravo 3, com ID 3.

Os processos usam UDP para enviar um valor chamado **token**. O mestre inicia o token com 2. O mestre e os escravos modificam esse valor e o encaminham até que ele complete todas as voltas configuradas.

## 2. Arquivos

### Processo.java

Contém os dados e os recursos comuns a todos os processos:

- Validação dos argumentos.
- Identificação do processo.
- Portas locais e remotas.
- Endereço IP do próximo processo.
- Socket UDP.
- Envio e recebimento do token.
- Operação matemática sobre o token.
- Abertura e fechamento do socket.

### Mestre.java

Contém o método `main` do mestre. Ele:

1. Cria um objeto `Processo`.
2. Inicializa o token em 2.
3. Envia o token ao Escravo 1.
4. Aguarda o token voltar após passar pelos três escravos.
5. Aplica sua própria operação ao token.
6. Repete o processo pelo número de voltas.

### Escravo.java

Contém o método `main` de cada escravo. Ele:

1. Cria um objeto `Processo`.
2. Aguarda um token UDP.
3. Aplica a operação configurada.
4. Envia o novo token ao processo seguinte.
5. Repete o processo pelo número de voltas.

## 3. Organização do anel

Por padrão, todos os processos usam o endereço local `127.0.0.1`.

| Processo | Porta padrão | Próximo destino |
|---|---:|---:|
| Mestre | 5000 | Escravo 1 pela porta 5001 |
| Escravo 1 | 5001 | Escravo 2 pela porta 5002 |
| Escravo 2 | 5002 | Escravo 3 pela porta 5003 |
| Escravo 3 | 5003 | Mestre pela porta 5000 |

A rota padrão é:

```text
Mestre -> Escravo 1 -> Escravo 2 -> Escravo 3 -> Mestre
```

Cada processo recebe em uma porta e envia para a porta do próximo processo no anel.

## 4. Argumentos

O formato básico é:

```text
ID OPERACAO VOLTAS [PORTA_LOCAL IP_PROXIMO PORTA_PROXIMA]
```

| Argumento | Obrigatório | Significado |
|---|---|---|
| `ID` | Sim | 0 no mestre ou 1, 2 e 3 nos escravos |
| `OPERACAO` | Sim | 0 para somar 1; 1 para multiplicar por 2 |
| `VOLTAS` | Sim | Número inteiro maior que zero |
| `PORTA_LOCAL` | Não | Porta onde o processo recebe pacotes |
| `IP_PROXIMO` | Não | IP do próximo processo; padrão `127.0.0.1` |
| `PORTA_PROXIMA` | Não | Porta do próximo processo |

Os primeiros três argumentos são obrigatórios. Os três argumentos opcionais permitem configurar um anel distribuído em mais de uma máquina ou alterar portas.

## 5. Compilação

Na pasta que contém os arquivos Java:

```bash
javac Processo.java Mestre.java Escravo.java
```

A compilação gera os arquivos `.class`. A pasta contém:

- `Processo.class`
- `Mestre.class`
- `Escravo.class`

## 6. Execução

O programa precisa de quatro processos separados, normalmente em quatro terminais.

### Terminal do mestre

```bash
java Mestre 0 0 5
```

Esse comando cria o mestre na porta 5000 e soma 1 ao token em cada volta.

### Terminal do Escravo 1

```bash
java Escravo 1 0 5
```

Esse escravo recebe um token e soma 1.

### Terminal do Escravo 2

```bash
java Escravo 2 1 5
```

Esse escravo recebe um token e multiplica por 2.

### Terminal do Escravo 3

```bash
java Escravo 3 0 5
```

Esse escravo recebe um token e soma 1.

Os escravos devem ser iniciados antes do mestre. Caso o mestre envie o token imediatamente antes de um escravo estar ligado, o datagrama poderá ser perdido.

## 7. Fluxo detalhado

Suponha que todas as operações sejam soma e o mestre use 5 voltas:

1. O mestre define `token = 2`.
2. O mestre envia 2 pela porta 5001.
3. O Escravo 1 recebe 2 e soma 1, produzindo 3.
4. O Escravo 1 envia 3 pela porta 5002.
5. O Escravo 2 recebe 3 e soma 1, produzindo 4.
6. O Escravo 2 envia 4 pela porta 5003.
7. O Escravo 3 recebe 4 e soma 1, produzindo 5.
8. O Escravo 3 envia 5 pela porta 5000.
9. O mestre recebe 5 e aplica sua própria operação.
10. O mestre usa o novo valor como token da próxima volta.

Se o Escravo 2 estiver configurado para multiplicar, o mesmo token seria multiplicado por 2 ao passar por ele.

## 8. Explicação da classe Processo

### Campos

#### `idProcesso`

Identifica a posição do processo no anel.

#### `tipoDeOperacao`

Determina a operação:

- `0`: soma 1.
- `1`: multiplica por 2.

#### `numeroDeVoltas`

Define quantas vezes o token deve percorrer o anel.

#### `portaLocal`

Porta em que o processo recebe datagramas UDP.

#### `portaDoProximoProcesso`

Porta usada para enviar o token ao próximo processo.

#### `ipDoProximoProcesso`

Endereço IP usado como destino. O padrão `127.0.0.1` representa a própria máquina.

#### `socketUdp`

Objeto `DatagramSocket` usado para todas as operações de rede.

#### `enderecoDoProximoProcesso`

Objeto `InetAddress` criado pela conversão do texto do IP.

### Construtor

O construtor:

- Verifica a quantidade de argumentos.
- Converte os argumentos numéricos.
- Valida o ID, a operação e a quantidade de voltas.
- Define portas padrão.
- Permite substituir portas e IP por argumentos opcionais.
- Valida portas entre 1 e 65535.
- Resolve o endereço IP.
- Cria o socket UDP.
- Configura o timeout.

### `enviarToken(int token)`

O método:

1. Converte o inteiro para texto.
2. Converte o texto para bytes.
3. Cria um `DatagramPacket`.
4. Define dados, tamanho, endereço IP e porta.
5. Envia o pacote pelo `DatagramSocket`.

### `receberToken()`

O método:

1. Reserva um vetor de 100 bytes.
2. Cria um `DatagramPacket` para receber os dados.
3. Aguarda um pacote ou dispara timeout.
4. Converte somente os bytes recebidos em texto.
5. Remove espaços e converte o texto em inteiro.

### `calcularToken(int token)`

Retorna:

- `token + 1`, quando `tipoDeOperacao` é 0.
- `token * 2`, quando `tipoDeOperacao` é 1.

### `mudarTempoDeEspera(int tempoEmMilissegundos)`

Altera o tempo máximo que o método `receive` poderá esperar. O valor é informado em milissegundos.

### `fechar()`

Fecha o `DatagramSocket` e libera a porta utilizada pelo processo.

## 9. Exemplo de configurações personalizadas

### Alterar somente a porta local

```bash
java Mestre 0 0 5 5100
```

Nesse caso, a porta seguinte continua sendo calculada como:

- Mestre: envia pela porta 5001.
- IP seguinte: `127.0.0.1`.

Porém, o comando abaixo deve ser usado nos escravos somente se o IP ou porta seguinte deles também for configurado.

### Configurar IP e porta remota

```bash
java Mestre 0 0 5 5000 192.168.1.20 5001
```

Nesse exemplo:

- O mestre recebe pela porta 5000.
- O próximo processo está no IP `192.168.1.20`.
- O próximo processo recebe pela porta 5001.

Para comunicação em computadores diferentes, o firewall deve permitir as portas usadas e o IP informado deve ser alcançável pela rede.

## 10. Funcionamento dos timeouts

O mestre é configurado inicialmente com timeout de 15 segundos.

O escravo é configurado inicialmente com timeout de 120 segundos. Após receber o primeiro token, cada escravo altera seu timeout para 15 segundos por meio de `mudarTempoDeEspera`.

Quando o timeout expira:

- A classe Mestre mostra: `O token demorou demais para voltar.`
- A classe Escravo mostra: `O token demorou demais para chegar.`

O timeout não corrige o problema da comunicação; ele apenas evita que o processo espere indefinidamente.

## 11. Tratamento de erros

### `SocketTimeoutException`

Indica que o programa esperou por dados e o prazo expirou.

### `SocketException`

Indica que a porta não pôde ser aberta ou usada, que o socket foi encerrado ou que ocorreu outra falha de rede.

### `IOException`

Agrupa falhas de entrada, saída e comunicação.

### `NumberFormatException`

Indica que um argumento que deveria ser número não pôde ser convertido.

### `IllegalArgumentException`

Indica que um argumento ou combinação de argumentos viola uma regra do programa.

Os erros são exibidos no terminal. No bloco `finally`, o socket é fechado caso tenha sido criado.

## 12. UDP e diferenças importantes

UDP não garante:

- Entrega de todos os pacotes.
- Ordem de chegada.
- Detecção automática de perda.

O programa atual não implementa retransmissão ou confirmação. Por isso, iniciar os escravos antes do mestre reduz a probabilidade de perda do primeiro datagrama.

## 13. Comentários no código

Os arquivos Java foram comentados em português. Os comentários foram usados para explicar:

- Importações.
- Campos e variáveis.
- Construção do processo.
- Validações.
- Cálculo das portas.
- Criação do socket.
- Envio de pacotes.
- Recepção de pacotes.
- Operações sobre o token.
- Timeout.
- Tratamento de exceções.
- Fechamento dos recursos.
