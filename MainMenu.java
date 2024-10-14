import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class MainMenu {
    private menuFrame mnF; // เก็บอินสแตนซ์ของ menuFrame

    public void showMainMenu(boolean show, ClientManager client) {
        if (mnF == null) {
            mnF = new menuFrame(client); // สร้างหน้าต่างเฉพาะครั้งแรก
        }
        mnF.setVisible(show); // แสดงหรือปิดหน้าต่าง
    }
}

class menuFrame extends JFrame{
    private ClientManager client;
    private CreateRoomFrame roomFrame; 

    menuFrame(ClientManager client) {
        this.client = client;
        //รับค่าขนาดหน้าจอ
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // ตั้งค่า JFrame ให้มีขนาดเท่ากับขนาดของหน้าจอ
        setSize(screenSize.width, screenSize.height);
        setTitle("Zombie Hunter");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel panels = new JPanel();
        panels.setLayout(new GridLayout(2,1));

        panels.setBackground(new Color(99, 93, 221));

        add(panels);
        
        ImageIcon icon = new ImageIcon(getClass().getResource("/image/icon.png"));
        setIconImage(icon.getImage());
        
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

        

    //กด Exit แล้วออกโปรแกรม
        exits.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
            
        });
        btnJoin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Your Name");
                
                // ตรวจสอบว่าชื่อไม่เป็น null และไม่ว่าง
                if (name == null || name.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // ออกจากเมธอดหากชื่อไม่ถูกต้อง
                }
        
                // เปลี่ยนชื่อผู้ใช้
                client.changeName(name, 1);
                
                String roomNumberInput = JOptionPane.showInputDialog("Enter room number:");
        
                // ตรวจสอบหมายเลขห้อง
                if (roomNumberInput == null || roomNumberInput.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Room number cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // ออกจากเมธอดหากหมายเลขห้องไม่ถูกต้อง
                }
        
                try {
                    int roomNumber = Integer.parseInt(roomNumberInput.trim()); // แปลงค่าเป็น int
        
                    // เช็คห้อง
                    client.isRoomExist(roomNumber); 
                    setVisible(false);


                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid room number format. Please enter a valid room number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnHowtoplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new howtoplays(client).setVisible(true);
            }
        });

        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Your Name");
                if (name != null && !name.isEmpty()) {
                    setVisible(false);
                    client.changeName(name, 0);
                    roomFrame = new CreateRoomFrame(client);
                    //roomFrame.setTextForEmpty(0, name); // เรียกใช้ทันทีเมื่อเปิด CreateRoomFrame
                    roomFrame.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter your name to continue.");
                }
            }
        });
    }

    public void joinRoomFrame()
    {
        setVisible(false); // ปิดหน้าต่างปัจจุบัน
        revalidate(); // อัปเดตการแสดงผล
        repaint(); // รีเฟรชการแสดงผล
        roomFrame = new CreateRoomFrame(client);
        roomFrame.setVisible(true); // แสดงหน้าต่างห้อง
    }

    public void roomNotFound()
    {
        setVisible(true); 
    }

    public void disRFrame()
    {
        roomFrame.disposeRFrame();
        
    }
}
class CreateRoomFrame extends JFrame {
    private JLabel[] emptyLabels;
    private ClientManager client;
    public String[] labelText = {"Player 1: empty", "Player 2: empty", "Player 3: empty", "Player 4: empty"};
    
    JPanel panelcenter = new JPanel();
    JLabel roomIdLabel = new JLabel("Room ID: 123456");

    CreateRoomFrame(ClientManager client) {
        this.client = client;
        // รับค่าขนาดหน้าจอ
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        BackgroundPanel bgframe = new BackgroundPanel();
        add(bgframe, BorderLayout.CENTER);
        bgframe.setLayout(new GridBagLayout());

        panelcenter.setPreferredSize(new Dimension(590, 620));
        panelcenter.setBackground(new Color(215, 201, 255));
        panelcenter.setLayout(new GridBagLayout());

        // ====================== สร้าง JLabel สำหรับแสดง Room ID ======================
        
        roomIdLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roomIdLabel.setForeground(Color.BLACK);
        roomIdLabel.setHorizontalAlignment(JLabel.CENTER);

        // ====================== GridBagConstraints สำหรับ JLabel ======================
        GridBagConstraints gbcRoomId = new GridBagConstraints();
        gbcRoomId.gridx = 0;
        gbcRoomId.gridy = 0;
        gbcRoomId.anchor = GridBagConstraints.CENTER;
        gbcRoomId.insets = new Insets(10, 0, 10, 0);
        panelcenter.add(roomIdLabel, gbcRoomId);

        // ====================== สร้าง array สำหรับ emptyLabels ======================
        emptyLabels = new JLabel[4];

        // ====================== สร้าง JLabel สำหรับข้อความ empty ======================
        for (int i = 0; i < emptyLabels.length; i++) {
            emptyLabels[i] = new JLabel(labelText[i]);
            emptyLabels[i].setFont(new Font("Arial", Font.PLAIN, 18));
            emptyLabels[i].setForeground(Color.BLACK);
            emptyLabels[i].setHorizontalAlignment(JLabel.CENTER);

            GridBagConstraints gbcEmpty = new GridBagConstraints();
            gbcEmpty.gridx = 0;
            gbcEmpty.gridy = i + 1;
            gbcEmpty.anchor = GridBagConstraints.CENTER;
            gbcEmpty.insets = new Insets(10, 0, 10, 0);
            panelcenter.add(emptyLabels[i], gbcEmpty);
        }

        // ============================ จัด button ===========================
        Button_ btnStart = new Button_("Start", 120, 60, new Color(218, 212, 207), 15);
        btnStart.setBackground(new Color(255, 102, 255));
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = 0;
        gbcButton.gridy = 5; // วางปุ่ม Start ด้านล่างของ empty
        gbcButton.anchor = GridBagConstraints.SOUTH;
        panelcenter.add(btnStart, gbcButton);

        // ============================ เพิ่ม action ให้กับปุ่ม Start ===========================
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelcenter.revalidate();
                panelcenter.repaint();
                Player player = client.getPlayerData();
                if (player.isOwner() == false)
                {
                    JOptionPane.showMessageDialog(null,"You are not Owner", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    int countPlayer = 0;
                    for (int i=0;i<4;i++)
                    {
                        if (player.getPlayerInRoomFromIndex(i).equals("Empty"))
                        {
                            //JOptionPane.showMessageDialog(null,"Cannot start the game. The room is not full with 4 players.", "Error", JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                        countPlayer++ ;
                    }
                    
                    if (countPlayer < 4)
                    {
                        client.startGame(CreateRoomFrame.this, player.getRoomID());
                    }
                }
            }
        });

        // เพิ่ม action listener สำหรับการปิดหน้าต่าง
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                client.leaveRoom();
                menuFrame mf = new menuFrame(client);
                mf.setVisible(true);
            }
        });

        bgframe.add(panelcenter); // เพิ่ม panelcenter ลงใน bgframe
        Player player = client.getPlayerData();
        updateLb updater = new updateLb(client, player, this); // ส่งอ้างอิง CreateRoomFrame
        Thread updateThread = new Thread(updater);
        updateThread.start(); // เริ่ม Thread สำหรับอัปเดต UI

    }

    // ============================ เมธอดตั้งค่าข้อความสำหรับ empty ============================
    public void setNameInRoom(int index, String text) {
        //System.out.println("Setting text for index: " + index + ", text: " + text);
    
        if (index >= 0 && index < emptyLabels.length) {
            this.labelText[index] = text;
            SwingUtilities.invokeLater(() -> {
                emptyLabels[index].setText(text);
                
                //System.out.println("อัพเดทใน for empty: " + index + " to: " + text); // ตรวจสอบการอัปเดต
            });
        } else {
            System.out.println("Index out of bounds: " + index);
        }
    }

    public void setRoomNumber(int rid)
    {
        roomIdLabel.setText("ROOM ID: "+String.valueOf(rid));
    }

    // Method สำหรับการ repaint CreateRoomFrame
    public void repaintRoomFrame() {
        panelcenter.revalidate(); // Re-layout the components
        panelcenter.repaint();    // Repaint the panel
    }

    public void disposeRFrame()
    {
        setVisible(false);
    }
    /* ============================ setBackground ที่ดึงภาพมา =========================== */

    // คลาสสำหรับพื้นหลัง
    class BackgroundPanel extends JPanel {
        private Image bgfromImage;

        BackgroundPanel() {
            bgfromImage = new ImageIcon(getClass().getResource("/image/BackG.png")).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bgfromImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

class updateLb implements Runnable {
    private String[] playerName = new String[4]; 
    private CreateRoomFrame crf; 
    private volatile boolean running = true; 
    private ClientManager client;
    private Player player;

    updateLb(ClientManager client, Player player, CreateRoomFrame crf) {
        this.client = client;
        this.crf = crf; // เก็บอ้างอิง CreateRoomFrame
        this.player = player;

        for (int i = 0; i < playerName.length; i++) {
            playerName[i] = "Player " + (i + 1) + ": "; 
        }
    }

    @Override
    public void run() {
        while (running) {
            crf.setRoomNumber(player.getRoomID()); 

            for (int i=0;i<4;i++)
            {
                crf.setNameInRoom(i, playerName[i] + player.getPlayerInRoomFromIndex(i)); 
            }

            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); 
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false; 
    }

    public void setStringName(int id, String name) {
        if (id >= 0 && id < playerName.length) {
            playerName[id] = name; 
            System.out.println("Updated in setString thread: " + playerName[id]);
        }
    }
}



class howtoplays extends JFrame {
    private ClientManager client;
    howtoplays(ClientManager client) { 
        this.client = client;
        //รับค่าขนาดหน้าจอ
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // ตั้งค่า JFrame ให้มีขนาดเท่ากับขนาดของหน้าจอ
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel htpPanels = new JPanel();
        htpPanels.setBackground(new Color(64, 31, 113));
        htpPanels.setLayout(new BorderLayout());
        add(htpPanels);
        
        ImageIcon htpImage = new ImageIcon(getClass().getResource("/image/htp.jpg"));
        Image imghtp = htpImage.getImage().getScaledInstance(screenSize.width/2, 670, Image.SCALE_SMOOTH);
        JLabel htplabelimage = new JLabel(new ImageIcon(imghtp));

        htpPanels.add(htplabelimage,BorderLayout.CENTER);

        Button_ htpButtonBack = new Button_("Back", 290, 70, Color.WHITE, 28);

        htpPanels.add(htpButtonBack,BorderLayout.SOUTH);


        addWindowListener(new WindowAdapter() {
            @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            menuFrame mnF = new menuFrame(client); // ส่ง ClientManager ไปยัง menuFrame
            mnF.setVisible(true);
        }
        });

        htpButtonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false); 
                menuFrame mnF = new menuFrame(client); // ส่ง ClientManager ไปยัง menuFrame
                mnF.setVisible(true);
            }
            
        });
    }
}