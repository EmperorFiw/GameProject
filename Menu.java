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
                    CreateRoomFrame roomFrame = new CreateRoomFrame(menuFrame.this);
                    roomFrame.setTextForEmpty(0, name); // เรียกใช้ทันทีเมื่อเปิด CreateRoomFrame
                    roomFrame.setVisible(true);
                    client.changeName(name, "create");
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter your name to continue.");
                }
            }
        });
    }
}
class CreateRoomFrame extends JFrame {
    private JLabel[] emptyLabels;
    public String[] labelText = {"empty", "empty", "empty", "empty"};
    
    JPanel panelcenter = new JPanel();

    CreateRoomFrame(menuFrame parent) {
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
        JLabel roomIdLabel = new JLabel("Room ID: 123");
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
            }
        });

        // เพิ่ม action listener สำหรับการปิดหน้าต่าง
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parent.setVisible(true);
            }
        });

        bgframe.add(panelcenter); // เพิ่ม panelcenter ลงใน bgframe
        startUpdatingLabels();
    }

    public void startUpdatingLabels() {
        new Thread(() -> {
            while (true) {
                for (int i = 0; i < labelText.length; i++) {
                    String currentLabelText = labelText[i]; // สร้างตัวแปรใหม่ที่เก็บค่า
                    if (currentLabelText != null) {
                        int  x = i;
                        SwingUtilities.invokeLater(() -> {
                            emptyLabels[x].setText(currentLabelText); // อัปเดตเลเบลด้วยชื่อผู้เล่น
                        });
                    }
                }
                try {
                    Thread.sleep(1000); // หยุด 1 วินาทีระหว่างการอัปเดต
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    

    // ============================ เมธอดตั้งค่าข้อความสำหรับ empty ============================
    public void setTextForEmpty(int index, String text) {
        System.out.println("Setting text for index: " + index + ", text: " + text);
        String name = "Player " + (index + 1) + ": " + text;
    
        if (index >= 0 && index < emptyLabels.length) {
            labelText[index] = name;
            SwingUtilities.invokeLater(() -> {
                emptyLabels[index].setText(name);
                System.out.println("Updated JLabel at index " + index + " to: " + name); // ตรวจสอบการอัปเดต
            });
        } else {
            System.out.println("Index out of bounds: " + index);
        }
    }

    // Method สำหรับการ repaint CreateRoomFrame
    public void repaintRoomFrame() {
        panelcenter.revalidate(); // Re-layout the components
        panelcenter.repaint();    // Repaint the panel
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
        
        ImageIcon htpImage = new ImageIcon(getClass().getResource("/image/htp.jpg"));
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