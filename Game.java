import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private Gamepage gamePage; // สร้างตัวแปร gamePage ที่เก็บอ้างอิง

    public void startGame(Player player, ClientManager client) {
        gamePage = new Gamepage(player, client);
        gamePage.showFrame();
    }
    public BackgroundPanel getPanelObject()
    {
        return gamePage.getPanel();
    }
}

class Gamepage {
    private JFrame frame;
    private Player player;
    private BackgroundPanel  panel;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    Gamepage(Player player, ClientManager client) {
        this.player = player;

        frame = new JFrame("Zombie Hunter");
        frame.setSize(screenSize.width, screenSize.height);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // panel of Background
        panel = new BackgroundPanel(frame, player, client);
        panel.setLayout(null);
        frame.add(panel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Game is closing...");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

    public void showFrame() {
        frame.setVisible(true);
    }

    public BackgroundPanel getPanel()
    {
        return this.panel;
    }
}

class BackgroundPanel extends JPanel {
    private Image bgimage;
    private Image spaceshipImage;
    private ImageIcon zombieNoob;
    private ImageIcon zombieBoss;
    private JFrame frame;
    private Player player;
    private ClientManager client;
    private int[] target; 
    
    private ArrayList<Bullet> bullets; // เก็บกระสุนทั้งหมด

    int spaceshipWidth = 100; 
    int spaceshipHeight = 100; 
    int spaceshipBetween = 60; 

    private Zombie[] zombies; // เปลี่ยนจากตำแหน่งเป็น Zombie
    private int zombiesToShow = 0;

    BackgroundPanel(JFrame frame, Player player, ClientManager client) {
        this.frame = frame;
        this.player = player;
        this.client = client;
    
        this.bullets = new ArrayList<>(); // Create ArrayList for bullets
    
        bgimage = new ImageIcon(getClass().getResource("/image/newBackG.jpg")).getImage();
        spaceshipImage = new ImageIcon(getClass().getResource("/image/spaceship.png")).getImage()
                .getScaledInstance(spaceshipWidth, spaceshipHeight, Image.SCALE_SMOOTH);
    
        zombieNoob = new ImageIcon(getClass().getResource("/image/Zombiefly_Noob.gif"));
        zombieBoss = new ImageIcon(getClass().getResource("/image/ZombieBoss.gif"));
    
        int amountZombie = 20; 
        zombies = new Zombie[amountZombie]; // Create an array for zombies
        Random random = new Random();
    
        // Loop random to find zombie positions
        for (int i = 0; i < amountZombie; i++) {
            int posX = 111 + 1000; // Random position X for zombies
            int posY = 160; // Can change as needed
            zombies[i] = new Zombie(i, 100, posX, posY); // Create new zombie
        }
    
        new Thread(new ZombieSpawner(this, amountZombie, player, client)).start();
    
        // Mouse click event to shoot
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int spaceshipX = 0; // Position X of the spaceship
                int spaceshipY = 50; // Position Y of the spaceship

                client.drawBullet(spaceshipX + spaceshipWidth, spaceshipY + spaceshipHeight / 2, e.getX(), e.getY());
                
            }
        });
    
        // In the BackgroundPanel constructor
        new BulletMover(bullets, 30, this, client).start(); // Pass 'this' to the BulletMover

    
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                frame.setTitle("Zombie Hunter - Mouse at (" + e.getX() + ", " + e.getY() + ")");
            }
        });
    }

    public void drawBullet(int x, int y, int tx, int ty)
    {
        
        bullets.add(new Bullet(x, y, tx, ty)); 
        playGunSound();
    }

    public void playGunSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("    "));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgimage, 0, 0, getWidth(), getHeight(), this);

        int spaceshipX = 30;
        int spaceshipY = 50;
        
        Font font = new Font("Arial", Font.BOLD, 20);
        g.setFont(font);

        Graphics2D g2d = (Graphics2D) g; //โยนมาให้ g2d จัดการ
        
        for (int i = 0; i < player.getCountPlayer(); i++) {
        
            g.drawImage(spaceshipImage, spaceshipX, spaceshipY, spaceshipWidth, spaceshipHeight, this);
            spaceshipY += spaceshipWidth + spaceshipBetween;
            
/* ตัวหนังสือ */
            String namePlayer = player.getPlayerInRoomFromIndex(i);
            g2d.setColor(Color.WHITE);

            g2d.translate(spaceshipX, spaceshipY); //ปรับตำแหน่ง x y 
            g2d.rotate(Math.toRadians(90)); //ปรับองศาตัวหนังสือ
            g2d.drawString(namePlayer, 0, 0); //วาดชื่อ ตำแหน่ง 0 0 
            g2d.setTransform(new AffineTransform()); //reset ค่าทั้งหมด ไม่ให้กระทบกับการวาดอันอื่น
            
            


        }

        // loop วาดซอมบี้
        for (int i = 0; i < zombiesToShow; i++) {
            Zombie zombie = zombies[i];
            if (i == 19)
            {
                g.drawImage(zombieBoss.getImage(), zombie.getPositionX(), zombie.getPositionY(), 105, 105, this);
            }
            else
                g.drawImage(zombieNoob.getImage(), zombie.getPositionX(), zombie.getPositionY(), 105, 105, this);

            
        /*------------------------------- วาด หลอดเลือด Zombie ------------------------------------- */
            int HealtBarWidth = 70; //ขนาดของหลอดเลือด
            int HealtBarHeigth = 7;//ขนาดของหลอดเลือด
            int currentHealt = zombie.getHealth();  // เก็บค่าเลือดล่าสุดของ zombie

            int HealthX = zombie.getPositionX() + 4; // position ให้มันตำแหน่งเดัยวกับ zombie
            int HealthY = zombie.getPositionY() - 12; // position ให้มันตำแหน่งเดัยวกับ zombie

        //ปรับสีของหลอดเลือด ตาม hp ที่เหลือ
            if (currentHealt > 50) {
                g.setColor(Color.GREEN); 
            } else if (currentHealt > 25) {
                g.setColor(Color.YELLOW); 
            } else {
                g.setColor(Color.RED); 
            }
            
            g.fillRect(HealthX, HealthY, currentHealt, HealtBarHeigth); //สร้างโครงให้หลอด
            g.setColor(Color.BLACK); //ขอบของหลอดเลือด
            g.drawRect(HealthX, HealthY, currentHealt, HealtBarHeigth); //วาดหลอดเลือด

        }

        // วาดกระสุน
        for (Bullet bullet : bullets) {
            g.setColor(Color.YELLOW); // สีกระสุน
            g.fillRect(bullet.getX(), bullet.getY(), 5, 5); // วาดกระสุน
            repaint();
        }
    }
        

    public void updateZombie(int index, int newPositionX, int newPositionY, int hp) {
        if (index < zombies.length) {
            zombies[index].setPosition(newPositionX, newPositionY); // อัปเดตตำแหน่งซอมบี้
            zombies[index].setHealth(hp);
            repaint();
        }
    }

    public void showNextZombie() {
        if (zombiesToShow < zombies.length) {
            zombiesToShow++;
            repaint();
        }
    }

    public int getPositionY(int index) {
        return zombies[index].getPositionY(); // ใช้ getPositionY จาก Zombie
    }

    public Zombie[] getZombies() {
        return zombies;
    }

    public int getTarget(int index)
    {
        return this.target[index];
    }

    public int[] setTarget(int[] targetid) {
        this.target = targetid; // ตั้งค่าให้ตัวแปร target
        return this.target; // ส่งกลับอาเรย์ของเป้าหมาย
    }
    
}

class ZombieSpawner implements Runnable {
    private BackgroundPanel panel;
    private int amountZombie;
    private Player player;
    private ClientManager client;
    private boolean needTarget = true;
    
    ZombieSpawner(BackgroundPanel panel, int amountZombie, Player player, ClientManager client) {
        this.panel = panel;
        this.amountZombie = amountZombie;
        this.player = player;
        this.client = client;
    }
    
    
    @Override
    public void run() {
        for (int i = 0; i < amountZombie; i++) {
            if (needTarget) 
            {
                client.getTarget(); 
                needTarget = false;
            }
            try {
                Thread.sleep(2000); // รอ 1.5 วินาที
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            panel.showNextZombie(); // แสดงซอมบี้ตัวถัดไป
            ZombieMover zombieMover = new ZombieMover(panel.getZombies()[i], panel, panel.getTarget(i), client); // ใช้ getZombies() เพื่อเข้าถึง
            // System.out.println(panel.getTarget());
            new Thread(zombieMover).start();
        }
    }
}


// คลาสที่จัดการการเคลื่อนที่ของซอมบี้
class ZombieMover implements Runnable {
    private Zombie zombie; 
    private BackgroundPanel panel; 
    private int moveToId;
    private ClientManager client;
    int[] possiblePositions = {50, 210, 370, 530};

    ZombieMover(Zombie zombie, BackgroundPanel panel, int moveToId, ClientManager client) {
        this.zombie = zombie;
        this.panel = panel;
        this.moveToId = moveToId;
        this.client = client;
    }

    @Override
    public void run() {
        int targetX = 50;
        while (zombie.getPositionX() > targetX && zombie.getHealth() != 0) {
            zombie.setPosition(zombie.getPositionX() - 2, zombie.getPositionY()); 
            
            if (zombie.getPositionY() != possiblePositions[moveToId] && zombie.getPositionY() < possiblePositions[moveToId])
                zombie.setPosition(zombie.getPositionX(), zombie.getPositionY()+1);
            else
                zombie.setPosition(zombie.getPositionX(), zombie.getPositionY()-1);
            
            panel.updateZombie(zombie.getId(), zombie.getPositionX(), zombie.getPositionY(), zombie.getHealth());
            try {
                Thread.sleep(30); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// คลาส Bullet สำหรับกระสุน
class Bullet {
    private int x, y, toX, toY;
    private boolean active;
    private boolean isCal = false;
    private double moveToX = 0;
    private double moveToY = 0;

    public Bullet(int x, int y, int toX, int toY) {
        this.x = x; // จะเป็น spaceshipX + spaceshipWidth เพื่อให้ออกจากหัวยาน
        this.y = y; // จะเป็น spaceshipY + spaceshipHeight / 2 เพื่อให้กระสุนออกจากกลางยาน
        this.toX = toX; 
        this.toY = toY; 
        this.active = true;
    }

    public void move() {

        if (!isCal)
        {
            int deltaX = toX - x;
            int deltaY = toY - y;
    
            // คำนวณระยะทางรวม
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    
            // คำนวณอัตราส่วนการเคลื่อนที่
            moveToX = (deltaX / distance) * 10; // 10 คือความเร็ว
            moveToY = (deltaY / distance) * 10; // 10 คือความเร็ว
             isCal = true;
        }

        // อัปเดตตำแหน่ง
        x += moveToX;
        y += moveToY;
    }

    

    public boolean checkCollision(Zombie zombie) {
        // ตรวจสอบว่ากระสุนชนกับซอมบี้หรือไม่
        return active && x >= zombie.getPositionX() && x <= zombie.getPositionX() + 105 &&
               y >= zombie.getPositionY() && y <= zombie.getPositionY() + 105;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

class BulletMover extends Thread {
    private final ArrayList<Bullet> bullets; 
    private final long sleepTime;
    private BackgroundPanel panel; // Reference to BackgroundPanel for collision detection
    private ClientManager client;

    public BulletMover(ArrayList<Bullet> bullets, long sleepTime, BackgroundPanel panel, ClientManager client) {
        this.bullets = bullets;
        this.sleepTime = sleepTime;
        this.panel = panel; // Store reference to BackgroundPanel
        this.client = client;
    }

    @Override
public void run() {
    while (true) {
        synchronized (bullets) {  // เพิ่มการซิงโครไนซ์
            for (int i = 0; i < bullets.size(); i++) {
                Bullet bullet = bullets.get(i);
                if (bullet.isActive()) {
                    bullet.move(); // Move the bullet

                    // Check collision with zombies
                    for (Zombie zombie : panel.getZombies()) {
                        if (bullet.checkCollision(zombie)) {
                            synchronized (zombie) {  // ซิงโครไนซ์การเข้าถึงข้อมูลซอมบี้
                                zombie.setHealth(zombie.getHealth() - 15); // Decrease zombie health
                                client.sendUpdateZP(zombie.getId(), zombie.getPositionX(), zombie.getPositionY(), zombie.getHealth());

                                if (zombie.getHealth() <= 0) {
                                    zombie.setPosition(-100, -100); // Hide dead zombie
                                    client.sendUpdateZP(zombie.getId(), zombie.getPositionX(), zombie.getPositionY(), zombie.getHealth());
                                }
                            }
                            bullet.setActive(false); // Deactivate bullet
                            break;
                        }
                    }
                }
            }

            // Remove inactive bullets
            bullets.removeIf(bullet -> !bullet.isActive());
        }

        try {
            Thread.sleep(sleepTime); // Delay for 30 ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle thread interruption
            break; // Exit loop if interrupted
        }
    }
}

}
