import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PlayMenu {

    public void showPlayMenu() {
        MainMenu menu = new MainMenu();
        menu.setVisible(true);
    }
}

class MainMenu extends JFrame {

    public MainMenu() {
        MainPanel mainPanel = new MainPanel();
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        add(mainPanel);
    }

    class MainPanel extends JPanel {

        public MainPanel() {
            setBackground(Color.BLACK);
            setLayout(null);

            // Adding player panels
            add(createPlayerPanel(10, 510));
            add(createPlayerPanel(250, 510));
            add(createPlayerPanel(550, 510));
            add(createPlayerPanel(775, 510));

            // Adding menu panel
            add(createMenuPanel());
        }

        private JPanel createPlayerPanel(int x, int y) {
            JPanel playerPanel = new JPanel();
            playerPanel.setBounds(x, y, 100, 100);
            playerPanel.setBackground(Color.WHITE);
            return playerPanel;
        }

        private JPanel createMenuPanel() {
            JPanel menuPanel = new JPanel(new BorderLayout());
            menuPanel.setBounds((900 - 400) / 2, 50, 400, 350);
            menuPanel.setBackground(Color.WHITE);

            // Room number
            JLabel roomNumberLabel = new JLabel("ROOM NUMBER: ");
            JPanel numberRoomPanel = new JPanel();
            numberRoomPanel.add(roomNumberLabel);
            menuPanel.add(numberRoomPanel, BorderLayout.NORTH);

            // Player status
            JPanel playPanel = new JPanel(new GridLayout(4, 1));
            for (int i = 0; i < 4; i++) {
                JLabel playerLabel = new JLabel("Empty", SwingConstants.CENTER);
                playPanel.add(playerLabel);
            }
            menuPanel.add(playPanel, BorderLayout.CENTER);

            // Start button
            JButton startButton = new JButton("START");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(startButton);
            menuPanel.add(buttonPanel, BorderLayout.SOUTH);

            return menuPanel;
        }
    }

    public void setNameInRoom(String name) {
        // Implement functionality if needed
    }

    public void setRoomNumber(int id) {
        // Implement functionality if needed
    }
}
