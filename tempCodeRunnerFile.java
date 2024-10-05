class CreateRoomFrame extends JFrame {
    private JLabel[] emptyLabels;
    public String[] labelText = {"Player 1: empty", "Player 2: empty", "Player 3: empty", "Player 4: empty"};
    
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
        Thread thread = new Thread(new updateLb(this)); // ส่ง instance นี้ไปยัง updateLb
        thread.start();
    }

    // ============================ เมธอดตั้งค่าข้อความสำหรับ empty ============================
    public void setTextForEmpty(int index, String text) {
        System.out.println("Setting text for index: " + index + ", text: " + text);
    
        if (index >= 0 && index < emptyLabels.length) {
            this.labelText[index] = text;
            SwingUtilities.invokeLater(() -> {
                emptyLabels[index].setText(text);
                
                System.out.println("อัพเดทใน for empty: " + index + " to: " + text); // ตรวจสอบการอัปเดต
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

    // ============================ เมธอดสำหรับการดึงชื่อ ============================
    public String getStringName(int id) {
        if (id >= 0 && id < labelText.length) {
            return labelText[id];
        }
        return null; // คืนค่า null ถ้า id นอกขอบเขต
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
    private String[] playerName = new String[4]; // กำหนดขนาดอาร์เรย์
    private CreateRoomFrame createRoomFrame; // รับ CreateRoomFrame
    private volatile boolean running = true; // ตัวแปรควบคุมการทำงานของ Thread

    // Constructor ที่รับ CreateRoomFrame
    updateLb(CreateRoomFrame createRoomFrame) {
        this.createRoomFrame = createRoomFrame;

        // กำหนดค่าเริ่มต้นให้กับ playerName
        for (int i = 0; i < playerName.length; i++) {
            playerName[i] = "Player " + (i + 1) + ": empty"; // ตั้งค่าชื่อผู้เล่น
        }
    }

    @Override
    public void run() {
        while (running) {
            for (int i = 0; i < playerName.length; i++) {
                createRoomFrame.setTextForEmpty(i, playerName[i]); // อัปเดตข้อความ
                System.out.println("อัพเดทใน thread: " + playerName[i]);
            }

            try {
                Thread.sleep(1000); // หยุดการทำงานของ thread เป็นเวลา 1000 มิลลิวินาที (1 วินาที)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // ตั้งค่า interrupt flag
                e.printStackTrace();
            }
        }
    }

    // Method สำหรับการหยุด Thread
    public void stop() {
        running = false; // เปลี่ยนตัวแปรควบคุมเป็น false เพื่อหยุด Thread
    }

    public void setStringName(int id, String name) {
        if (id >= 0 && id < playerName.length) {
            playerName[id] = name; // อัปเดตชื่อผู้เล่น
            System.out.println("อัพเดทใน setString thread: " + playerName[id]);
        }
    }
}

