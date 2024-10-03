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


public class Menu {
    public void showMainMenu(boolean show, ClientManager client) { // รับ ClientManager
        menuFrame mnF = new menuFrame(client); // ส่ง ClientManager ไปยัง menuFrame
        if (show) {
            mnF.setVisible(true);
        } else {
            mnF.setVisible(false);
        }
    }
}

class menuFrame extends JFrame{
    private ClientManager client;

    // รับ ClientManager ผ่าน constructor
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
        
        ImageIcon icon = new ImageIcon("image/icon.png");
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
                if (name != null && !name.isEmpty()) {
                    client.changeName(name, "join");
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter your name to continue.");
                }
            }
        });
        btnHowtoplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new howtoplays(menuFrame.this).setVisible(true);
            }
            
        });

        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Your Name");
                if (name != null && !name.isEmpty()) {
                    setVisible(false);
                    new CreateRoomFrame(menuFrame.this, name).setVisible(true);
                    client.changeName(name, "create");
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter your name to continue.");
                }
            }
        });
    }
}

class howtoplays extends JFrame {
    howtoplays(menuFrame parent) { 
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
        
        ImageIcon htpImage = new ImageIcon("image/htp.jpg");
        Image imghtp = htpImage.getImage().getScaledInstance(screenSize.width/2, 670, Image.SCALE_SMOOTH);
        JLabel htplabelimage = new JLabel(new ImageIcon(imghtp));

        htpPanels.add(htplabelimage,BorderLayout.CENTER);

        Button_ htpButtonBack = new Button_("Back", 290, 70, Color.WHITE, 28);

        htpPanels.add(htpButtonBack,BorderLayout.SOUTH);


        addWindowListener(new WindowAdapter() {
            @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            parent.setVisible(true); 
        }
        });

        htpButtonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false); 
                parent.setVisible(true); 
            }
            
        });
    }
}
class CreateRoomFrame extends JFrame {
    private JLabel[] emptyLabels;
    JPanel panelcenter = new JPanel();

    CreateRoomFrame(menuFrame parent, String playerName) {
        // รับค่าขนาดหน้าจอ
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // ตั้งค่า JFrame ให้มีขนาดเท่ากับขนาดของหน้าจอ
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel createRoomPanel = new JPanel();
        createRoomPanel.setBackground(new Color(64, 31, 113));
        createRoomPanel.setLayout(new BorderLayout());
        add(createRoomPanel);

        Button_ btnStart = new Button_("Start", 120, 60, new Color(218, 212, 207), 15);
        btnStart.setBackground(new Color(255, 102, 255));
        setbackground bgframe = new setbackground();
        add(bgframe);

        bgframe.setLayout(new GridBagLayout());

        
        panelcenter.setPreferredSize(new Dimension(590, 620));
        panelcenter.setBackground(new Color(215, 201, 255));
        panelcenter.setLayout(new GridBagLayout());

        // ====================== สร้าง JLabel สำหรับแสดง Room ID ======================
        JLabel roomIdLabel = new JLabel("Room ID: 123");
        roomIdLabel.setFont(new Font("Arial", Font.BOLD, 24)); // เปลี่ยนฟอนต์และขนาด
        roomIdLabel.setForeground(Color.BLACK); // เปลี่ยนสีข้อความ
        roomIdLabel.setHorizontalAlignment(JLabel.CENTER); // จัดตำแหน่งกลางแนวนอน

        // ====================== GridBagConstraints สำหรับ JLabel ======================
        GridBagConstraints gbcRoomId = new GridBagConstraints();
        gbcRoomId.gridx = 0; // ตำแหน่ง x
        gbcRoomId.gridy = 0; // ตำแหน่ง y
        gbcRoomId.anchor = GridBagConstraints.CENTER; // จัดให้อยู่กลาง
        gbcRoomId.insets = new Insets(10, 0, 10, 0); // เพิ่มระยะห่างด้านบนและล่าง

        panelcenter.add(roomIdLabel, gbcRoomId); // เพิ่ม JLabel ลงใน panelcenter

        // ====================== สร้าง array สำหรับ emptyLabels ======================
        emptyLabels = new JLabel[4]; // สร้าง array สำหรับ 4 JLabel

        // ====================== สร้าง JLabel สำหรับข้อความ empty ======================
        for (int i = 0; i < emptyLabels.length; i++) {
            emptyLabels[i] = new JLabel("empty"); // สร้าง JLabel ใหม่
            emptyLabels[i].setFont(new Font("Arial", Font.PLAIN, 18)); // กำหนดฟอนต์
            emptyLabels[i].setForeground(Color.BLACK); // กำหนดสีข้อความ
            emptyLabels[i].setHorizontalAlignment(JLabel.CENTER); // จัดตำแหน่งกลางแนวนอน

            // ====================== GridBagConstraints สำหรับ emptyLabel ======================
            GridBagConstraints gbcEmpty = new GridBagConstraints();
            gbcEmpty.gridx = 0; // ตำแหน่ง x
            gbcEmpty.gridy = i + 1; // ตำแหน่ง y สำหรับแต่ละบรรทัด
            gbcEmpty.anchor = GridBagConstraints.CENTER; // จัดให้อยู่กลาง
            gbcEmpty.insets = new Insets(10, 0, 10, 0); // ระยะห่างระหว่างบรรทัด

            panelcenter.add(emptyLabels[i], gbcEmpty); // เพิ่ม JLabel ลงใน panelcenter
        }

        // ============================ จัด button ===========================
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = 0;
        gbcButton.gridy = 5; // วางปุ่ม Start ด้านล่างของ empty
        gbcButton.anchor = GridBagConstraints.SOUTH;
        panelcenter.add(btnStart, gbcButton);

        // ============================ เพิ่ม action ให้กับปุ่ม Create a room ===========================
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });

        bgframe.add(panelcenter); // เพิ่ม panelcenter ลงใน bgframe

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTextForEmpty(0, playerName);
                panelcenter.revalidate();
                panelcenter.repaint();
            }
        });
        

        // setTextForEmpty(0, playerName);
    }
    // ============================ เมธอดตั้งค่าข้อความสำหรับ empty ============================
    public void setTextForEmpty(int index, String text) {
        System.out.println("Setting text for index: " + index + ", text: " + text);
        String name = "Player " + index + ": " + text;
    
        if (index >= 0 && index < emptyLabels.length) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    emptyLabels[index].setText(name);
                    panelcenter.invalidate();  
                    panelcenter.revalidate();
                    panelcenter.repaint();
                    
                }
            });
        } else {
            System.out.println("Index out of bounds: " + index);
        }
    }
    
    
    
    /* ============================ setBackground ที่ดึงภาพมา =========================== */
    class setbackground extends JPanel {
        private Image bgfromImage;

        setbackground() {
            bgfromImage = new ImageIcon("image/BackG.png").getImage();
        }

        /* ============================ สั่งให้มันวาดที่ดึงมา =========================== */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bgfromImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
