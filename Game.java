import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game {
    public static void main(String[] args) {
        Gamepage framegame = new Gamepage();
        framegame.showFrame();
    }
}

class Gamepage {
    private JFrame frame;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    Gamepage() {
        frame = new JFrame("Zombie Hunter");
        frame.setSize(screenSize.width, screenSize.height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // panel of Background
        BackgroundPanel panelBG = new BackgroundPanel(frame);
        panelBG.setLayout(null);
        frame.add(panelBG);
    }

    public void showFrame() {
        frame.setVisible(true);
    }
}

class BackgroundPanel extends JPanel {

    private Image bgimage;
    private Image spaceshipImage;
    private ImageIcon zombieNoob;
    private JFrame frame;

    /* ------------------------------ Zombie -------------------------------- */
    int spaceshipWidth = 100; // size
    int spaceshipHeight = 100; // size
    int spaceshipBetween = 60; // ค่า default ระยะห่างเริ่มต้น

    int[] zombiePositionX;
    int[] zombiePositionY; // เพิ่มตำแหน่ง Y ของซอมบี้
    private int zombiesToShow = 0; // ตัวแปรเพื่อกำหนดจำนวนซอมบี้ที่จะแสดงในขณะนี้

    BackgroundPanel(JFrame frame) {
        this.frame = frame;
        bgimage = new ImageIcon(getClass().getResource("/image/newBackG.jpg")).getImage(); // get image background มา
        spaceshipImage = new ImageIcon(getClass().getResource("/image/spaceship.png")).getImage()
                .getScaledInstance(spaceshipWidth, spaceshipHeight, Image.SCALE_SMOOTH); // get Image spaceship มา และ ปรับ scale ของภาพ

        zombieNoob = new ImageIcon(getClass().getResource("/image/zombie_noob.gif"));

        int amountZombie = 50; // จำนวน zombie ที่ fix ไว้
        zombiePositionX = new int[amountZombie + 1]; // เก็บตำแหน่ง X ของ zombie
        zombiePositionY = new int[amountZombie + 1]; // เก็บตำแหน่ง Y ของ zombie
        Random random = new Random();

        // loop random หา position ของ zombie ตาม index ที่ i
        for (int i = 0; i <= amountZombie; i++) {
            zombiePositionX[i] = random.nextInt(111) + 1000; // random Position X ของ zombie ตั้งแต่ 1000 - 1110
            zombiePositionY[i] = 160;//random.nextInt(500) + 100; // random Position Y ของ zombie ตั้งแต่ 100 - 600
        }

        // เริ่ม Thread สำหรับการแสดงผลทีละตัว
        new Thread(new ZombieSpawner(this, amountZombie)).start();
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // แสดงพิกัดของเมาส์บนหัว JFrame
                frame.setTitle("Zombie Hunter - Mouse at (" + e.getX() + ", " + e.getY() + ")");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgimage, 0, 0, getWidth(), getHeight(), this); // สั่งให้วาดภาพ background

        int spaceshipX = 0; // กำหนดให้ มันเริ่ม x = 0
        int spaceshipY = 50; // ให้มันเริ่มข้างล่าง Y

        // loop สร้าง spaceship
        for (int i = 0; i < 4; i++) {
            g.drawImage(spaceshipImage, spaceshipX, spaceshipY, spaceshipWidth, spaceshipHeight, this); // สั่งให้วาด spaceship
            spaceshipY += spaceshipWidth + spaceshipBetween; // ทำการเพิ่มระยะห่างให้กับ spaceship
        }

        // loop สร้าง zombie ทีละตัวตามจำนวนที่อนุญาตให้แสดง
        for (int i = 0; i < zombiesToShow; i++) {
            if (i < zombiePositionX.length - 1) {
                g.drawImage(zombieNoob.getImage(), zombiePositionX[i], zombiePositionY[i], 150, 150, this); // สั่งวาดซอมบี้ โดยใช้ตำแหน่ง Y
            }
        }
    }

    // ฟังก์ชันเพื่ออัปเดตตำแหน่งซอมบี้
    public void updateZombiePosition(int index, int newPositionX, int newPositionY) {
        if (index < zombiePositionX.length) {
            zombiePositionX[index] = newPositionX;
            zombiePositionY[index] = newPositionY; // อัปเดตตำแหน่ง Y ด้วย
            repaint(); // สั่งวาดใหม่
        }
    }

    // ฟังก์ชันเพื่ออัปเดตจำนวนซอมบี้ที่จะถูกแสดง
    public void showNextZombie() {
        if (zombiesToShow < zombiePositionX.length - 1) {
            zombiesToShow++;
            repaint(); // สั่งวาดใหม่เพื่อแสดงซอมบี้ตัวถัดไป
        }
    }

    public int getPositionY(int index)
    {
        return zombiePositionY[index];
    }
}

// คลาสที่จัดการการสร้างซอมบี้ทีละตัว
class ZombieSpawner implements Runnable {
    private BackgroundPanel panel;
    private int amountZombie;
    Random random = new Random();

    ZombieSpawner(BackgroundPanel panel, int amountZombie) {
        this.panel = panel;
        this.amountZombie = amountZombie;
    }

    @Override
    public void run() {
        for (int i = 0; i < amountZombie; i++) {
            panel.showNextZombie(); // แสดงซอมบี้ตัวถัดไป
            ZombieMover zombieMover = new ZombieMover(panel.zombiePositionX[i], panel.zombiePositionY[i], i, panel, random.nextInt(4));
            new Thread(zombieMover).start();
            // รอ 2 วินาทีก่อนสร้างซอมบี้ตัวต่อไป
            try {
                Thread.sleep(2000); // รอ 2 วินาที
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// คลาสที่จัดการการเคลื่อนที่ของซอมบี้
class ZombieMover implements Runnable {
    private int positionX; // ตำแหน่ง x เริ่มต้นของซอมบี้
    private int positionY; // ตำแหน่ง y เริ่มต้นของซอมบี้
    private int index; // ดัชนีของซอมบี้ในอาร์เรย์
    private int moveToId;
    private BackgroundPanel panel; // แผงที่จะอัปเดตตำแหน่ง
    Random random = new Random();
    int[] possiblePositions = {50, 210, 370, 530}; // กำหนดตำแหน่งที่เป็นไปได้

    ZombieMover(int positionX, int positionY, int index, BackgroundPanel panel, int moveToId) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.index = index;
        this.panel = panel;
        this.moveToId = moveToId;
    }

    @Override
    public void run() {
        int targetX = 50; // เป้าหมายการเคลื่อนที่ในแนว X
        while (positionX > targetX) {
            positionX -= 2; 
            if (panel.getPositionY(index) != possiblePositions[moveToId] && panel.getPositionY(index) < possiblePositions[moveToId])
                positionY += 1;
            else
                positionY -= 1;
            panel.updateZombiePosition(index, positionX, positionY); // อัปเดตตำแหน่งซอมบี้ในแนว X และ Y
            try {
                Thread.sleep(30); // รอ 50 มิลลิวินาทีเพื่อให้การเคลื่อนที่ดูราบรื่น
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
