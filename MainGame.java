
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MainGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientManager client = new ClientManager();
            client.mainMenu(true);
            client.connectToServer();
        });
    }
}

class ClientManager {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    PlayMenu playMenu = new PlayMenu();

    JFrame frame = new JFrame();

    public void mainMenu(boolean showMain) {
        if (showMain)
        {

            frame.setSize(620,723);
            frame.setTitle("Zombie Hunter");
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            
            JPanel panels = new JPanel();
            panels.setLayout(new GridLayout(2,1));
    
            panels.setBackground(new Color(99, 93, 221));
    
            frame.add(panels);
            
            ImageIcon icon = new ImageIcon("image/icon.png");
            frame.setIconImage(icon.getImage());
            
            Image img = icon.getImage();
            Image resetsize = img.getScaledInstance(267, 268, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resetsize);
            JLabel imageLabel = new JLabel(resizedIcon);
            panels.add(imageLabel);
    
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            buttonPanel.setBackground(new Color(99, 93, 221));
    
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.NONE; 
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.insets = new Insets(10, 0, 10, 0); //กำหนดระยะห่างของ button
    
            Dimension buttonsize = new Dimension(290,70); // setค่า defualt ให้กับ buttonsize
    
            Button_ btnCreate = new Button_("Create a Room", 290, 70, Color.WHITE, 25);
            Button_ btnJoin = new Button_("Join a Room", 290, 70, Color.WHITE, 25);
            Button_ btnHowtoplay = new Button_("How to Play", 290, 70, Color.WHITE, 25);
            Button_ exits = new Button_("Exits", 290, 70, Color.WHITE, 25);
    
            btnCreate.setPreferredSize(buttonsize);
            btnJoin.setPreferredSize(buttonsize);
            btnHowtoplay.setPreferredSize(buttonsize);
            exits.setPreferredSize(buttonsize);
    
            buttonPanel.add(btnCreate,gbc);
            buttonPanel.add(btnJoin,gbc);
            buttonPanel.add(btnHowtoplay,gbc);
            buttonPanel.add(exits,gbc);
    
            panels.add(buttonPanel);
    
            frame.setVisible(true);
         //กด Exit แล้วออกโปรแกรม
            exits.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
                
            });
            btnJoin.addActionListener(e -> {
                String name = JOptionPane.showInputDialog(frame, "Enter Your Name");
                if (name != null && !name.isEmpty()) {
                    changeName(name);
                }
            });
            btnCreate.addActionListener(e -> {
                if (socket != null && socket.isConnected() && !socket.isClosed()) {
                    // ส่งคำสั่งสร้างห้องใหม่
                    PlayerAction action = new PlayerAction(PlayerAction.ActionType.CREATE_ROOM, null);
                    sendAction(action);
                    mainMenu(false);
                    playMenu.showPlayMenu();
                } else {
                    JOptionPane.showMessageDialog(frame, "Unable to start game. Not connected to server.");
                }
            });
        } else
        {
            frame.setVisible(false);
        }
    }

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());


            new Thread(() -> {
                try {
                    Object messageFromServer;
                    while ((messageFromServer = in.readObject()) != null) {
                        if (messageFromServer instanceof String) {
                            String message = (String) messageFromServer;
                            System.out.println(message);
                            if ("RoomExists".equals(message)) {
                                mainMenu(false);
                                System.out.println("Joining room");
                                playMenu.showPlayMenu();
                            } else if ("RoomNotExists".equals(message)) {
                                System.out.println("Room not found");
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(frame, "Room not found");
                                });
                            }
                        } /*else if (messageFromServer instanceof PlayerAction) {
                        PlayerAction action = (PlayerAction) messageFromServer;
                        // ทำการจัดการกับ PlayerAction ที่ส่งมา
                        System.out.println("Received PlayerAction: " + action.getActionType());
                    } */
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendJoinRoom(String roomNumber) {
        PlayerAction action = new PlayerAction(PlayerAction.ActionType.JOIN_ROOM, Integer.parseInt(roomNumber));
        sendAction(action);
    }

    private void changeName(String name) {
        PlayerAction action = new PlayerAction(PlayerAction.ActionType.SET_NAME, name);
        sendAction(action);
        joinRoom();
    }

    private void joinRoom() {
        String roomNumber = JOptionPane.showInputDialog(frame, "Enter room number:");
        if (roomNumber != null && !roomNumber.isEmpty()) {
            sendJoinRoom(roomNumber);
        }
    }

    private void sendAction(PlayerAction action) {
        try {
            out.writeObject(action);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
