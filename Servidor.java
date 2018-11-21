import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import java.net.ServerSocket;
import java.net.Socket;

//	VITOR CAZELLATO 201400100
//	IURI BALTIERI 201500069

public class Servidor {

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8901);
        System.out.println("Servidor do jogo da velha está executando");
        try {
            while (true) {
                Jogo game = new Jogo();
                Jogo.Jogador playerX = game.new Jogador(listener.accept(), 'X');
                Jogo.Jogador playerO = game.new Jogador(listener.accept(), 'O');
                playerX.setOponente(playerO);
                playerO.setOponente(playerX);
                game.jogadorAtual = playerX;
                playerX.start();
                playerO.start();
            }
        } finally {
            listener.close();
        }
    }
}

class Jogo {
    private Jogador[] board = { null, null, null, null, null, null, null, null, null };

    Jogador jogadorAtual;

    public boolean haVencedor() {
        return (board[0] != null && board[0] == board[1] && board[0] == board[2])
                || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                || (board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    public boolean tabuleiroPreenchido() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean movimentoPermitido(int localizacao, Jogador jogador) {
        if (jogador == jogadorAtual && board[localizacao] == null) {
            board[localizacao] = jogadorAtual;
            jogadorAtual = jogadorAtual.oponente;
            jogadorAtual.outroJogadorMovimentou(localizacao);
            return true;
        }
        return false;
    }

    class Jogador extends Thread {
        char marca;
        Jogador oponente;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        public Jogador(Socket socket, char marca) {
            this.socket = socket;
            this.marca = marca;
            try {
                System.out.println("Jogador " + this.marca + " entrou: " + socket.getInetAddress().getHostAddress());

                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                output.println("WELCOME " + marca);
                output.println("MESSAGE Aguardando a conexão do oponente");
            } catch (IOException e) {
                System.out.println("Jogador " + this.marca + " saiu: " + e.getMessage());
            }
        }

        public void setOponente(Jogador oponente) {
            this.oponente = oponente;
        }

        public void outroJogadorMovimentou(int localizacao) {
            output.println("OPPONENT_MOVED " + localizacao);
            output.println(haVencedor() ? "DEFEAT" : tabuleiroPreenchido() ? "TIE" : "");
        }

        public void run() {
            try {
                output.println("MESSAGE Jogadores conectados");

                if (marca == 'X') {
                    output.println("MESSAGE Sua vez");
                }

                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (movimentoPermitido(location, this)) {
                            output.println("VALID_MOVE");
                            output.println(haVencedor() ? "VICTORY" : tabuleiroPreenchido() ? "TIE" : "");
                        } else {
							output.println("INVALID_PLAYER");
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("Jogador " + this.marca + " saiu: " + e.getMessage());

                this.oponente.output.println("BREAK");;
            } finally {
                try {
                    socket.close();
                    
                } catch (IOException e) {
                }
            }
        }
    }
}
