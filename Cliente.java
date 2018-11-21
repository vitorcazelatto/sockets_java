import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//	IURI BALTIERI 201500069
//	VITOR CAZELLATO 201400100

public class Cliente {
    public JFrame frame = new JFrame("JogoDaVelha");
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icone;
    private ImageIcon iconeOponente;

    private Quadrado[] tabuleiro = new Quadrado[9];
    private Quadrado quadradoCorrente;

    private static int PORTA = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Cliente(String serverAddress) throws Exception {
        socket = new Socket(serverAddress, PORTA);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, java.awt.BorderLayout.SOUTH);

        JPanel tabuleiroPanel = new JPanel();
        tabuleiroPanel.setBackground(Color.yellow);
        tabuleiroPanel.setLayout(new GridLayout(3, 3, 8, 8));
        for (int i = 0; i < tabuleiro.length; i++) {
            final int j = i;
            tabuleiro[i] = new Quadrado();
            tabuleiro[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    quadradoCorrente = tabuleiro[j];
                    out.println("MOVE " + j);
                }
            });
            tabuleiroPanel.add(tabuleiro[i]);
        }
        frame.getContentPane().add(tabuleiroPanel, java.awt.BorderLayout.CENTER);
    }

    public void jogar() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                icone = new ImageIcon(mark == 'X' ? "x.png" : "o.png");
                iconeOponente = new ImageIcon(mark == 'X' ? "o.png" : "x.png");
                frame.setTitle("JogoDaVelha - Jogador " + mark);
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Movimento válido, aguarde");
                    quadradoCorrente.setIcon(icone);
                    quadradoCorrente.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    tabuleiro[loc].setIcon(iconeOponente);
                    tabuleiro[loc].repaint();
                    frame.repaint();
                    messageLabel.setText("Oponente fez seu movimento, sua vez");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("Você venceu");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("Você perdeu");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("Houve empate");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
				} else if (response.startsWith("INVALID_PLAYER")) {
					JOptionPane.showMessageDialog(frame, "Vez de seu oponente!");
                } else if (response.startsWith("BREAK")) {
                    messageLabel.setText("Oponente saiu");
                    break;
                }
            }
            out.println("QUIT");
        } finally {
            socket.close();
        }
    }

    private boolean desejaJogarNovamente() {
        int response = JOptionPane.showConfirmDialog(frame, "Deseja jogar novamente?", "Aviso",
                JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    static class Quadrado extends JPanel {
        JLabel label = new JLabel((Icon) null);

        public Quadrado() {
            setBackground(Color.white);
            setLayout(new BorderLayout());
            add(label);
        }

        public void setIcon(Icon icon) {
            label.setIcon(icon);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
        }
        
        while (true) {
            String serverAddress = (args.length == 0) ? "localhost" : args[0];
            Cliente client = new Cliente(serverAddress);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(500, 400);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.frame.setLocationRelativeTo(null);
            client.jogar();
            if (!client.desejaJogarNovamente()) {
                break;
            }
        }
    }
}