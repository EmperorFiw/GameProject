import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
public class PlayMenu {
    MainMenu menu;

    public void showPlayMenu(int roomID) {  // Accept room ID and pass it to MainMenu
        menu = new MainMenu(roomID);
        menu.setVisible(true);
    }

    public void setRoomNumber(int id) {
        menu.setRoomNumber(id);  // Set the room number in the UI
    }
}

class MainMenu extends JFrame {
    private ClientManager client = new ClientManager();
    private JLabel roomNumberLabel;

    public MainMenu(int roomID) { // Accept room ID as a parameter
        MainPanel mainPanel = new MainPanel(roomID); // Pass room ID to the main panel
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(MainMenu.this,
                        "Are you sure you want to exit?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // ส่ง PlayerAction สำหรับออกจากเกม
                        PlayerAction action = new PlayerAction(PlayerAction.ActionType.EXIT_GAME);
                        client.sendAction(action);
                        client.closeConnection(); // ปิดการเชื่อมต่อที่นี่
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.exit(0);
                }
            }
            
        });
        
        add(mainPanel);
        revalidate();
        repaint();

    }

    class MainPanel extends JPanel {
        public MainPanel(int roomID) {  // Accept room ID and use it
            setBackground(Color.BLACK);
            setLayout(null);

            // Adding player panels
            add(createPlayerPanel(10, 510));
            add(createPlayerPanel(250, 510));
            add(createPlayerPanel(550, 510));
            add(createPlayerPanel(775, 510));

            // Adding menu panel
            add(createMenuPanel(roomID));  // Pass room ID to the menu panel
        }

        private JPanel createPlayerPanel(int x, int y) {
            JPanel playerPanel = new JPanel();
            playerPanel.setBounds(x, y, 100, 100);
            playerPanel.setBackground(Color.WHITE);
            return playerPanel;
        }

        private JPanel createMenuPanel(int roomID) {  // Accept room ID here
            JPanel menuPanel = new JPanel(new BorderLayout());
            menuPanel.setBounds((900 - 400) / 2, 50, 400, 350);
            menuPanel.setBackground(Color.WHITE);

            // Room number
            roomNumberLabel = new JLabel("ROOM NUMBER: " + roomID);  // Display room ID
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

    public void setRoomNumber(int roomID) {
        roomNumberLabel.setText("ROOM NUMBER: " + roomID);
    }
}
