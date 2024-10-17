import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


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
    private JFrame frame;
    private Player player;
    private ClientManager client;
    private int target;
    
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

        this.bullets = new ArrayList<>(); // สร้าง ArrayList สำหรับกระสุน
  
        bgimage = new ImageIcon(getClass().getResource("/image/newBackG.jpg")).getImage();
        spaceshipImage = new ImageIcon(getClass().getResource("/image/spaceship.png")).getImage()
                .getScaledInstance(spaceshipWidth, spaceshipHeight, Image.SCALE_SMOOTH);

        zombieNoob = new ImageIcon(getClass().getResource("/image/Zombiefly_Noob.gif"));

        int amountZombie = 50; 
        zombies = new Zombie[amountZombie]; // สร้างอาร์เรย์ซอมบี้
        Random random = new Random();

        // loop random หา position ของ zombie ตาม index ที่ i
        for (int i = 0; i < amountZombie; i++) {
            int posX = 111+1250;//random.nextInt(111) + 1250; // random Position X ของ zombie
            int posY = 160; // สามารถเปลี่ยนได้ตามต้องการ
            zombies[i] = new Zombie(i, 100, posX, posY); // สร้างซอมบี้ใหม่
        }

        new Thread(new ZombieSpawner(this, amountZombie, player, client)).start();

        
        // เพิ่ม mouse click event สำหรับการยิง
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                // เพิ่มกระสุนใหม่เมื่อคลิกเมาส์ โดยตำแหน่งกระสุนจะออกจากหัวยาน
                int spaceshipX = 0; // ตำแหน่ง X ของยานอวกาศ
                int spaceshipY = 50; // ตำแหน่ง Y ของยานอวกาศ

                bullets.add(new Bullet(spaceshipX + spaceshipWidth, spaceshipY + spaceshipHeight / 2)); 
                // หาตำแหน่งของลำปืน เพื่อให้กระสุนออกหน้าลำปืนอ่ะ
            }
        });


        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                frame.setTitle("Zombie Hunter - Mouse at (" + e.getX() + ", " + e.getY() + ")");
            }
        });

        

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgimage, 0, 0, getWidth(), getHeight(), this);

        int spaceshipX = 0;
        int spaceshipY = 50;

        for (int i = 0; i < player.getCountPlayer(); i++) {
            g.drawImage(spaceshipImage, spaceshipX, spaceshipY, spaceshipWidth, spaceshipHeight, this);
            spaceshipY += spaceshipWidth + spaceshipBetween;
        }

        // loop วาดซอมบี้
        for (int i = 0; i < zombiesToShow; i++) {
            Zombie zombie = zombies[i];
            g.drawImage(zombieNoob.getImage(), zombie.getPositionX(), zombie.getPositionY(), 105, 105, this);

            
        /*------------------------------- วาด หลอดเลือด Zombie ------------------------------------- */
            int HealtBarWidth = 70; //ขนาดของหลอดเลือด
            int HealtBarHeigth = 7;//ขนาดของหลอดเลือด
            int currentHealt = (int) ((double) zombie.getHealth() / 100 * HealtBarWidth);  // เก็บค่าเลือดล่าสุดของ zombie

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
            bullet.move();
            g.setColor(Color.YELLOW); // สีกระสุน
            g.fillRect(bullet.getX(), bullet.getY(), 5, 5); //วาดกระสุน

            // ตรวจสอบว่ากระสุนชนกับซอมบี้หรือไม่
            for (Zombie zombie : zombies) {
                if (bullet.checkCollision(zombie)) { //ดึง method มาจาก class Bullet เพื่อเช็คว่ากระสุนโดน zombie ไหม

                    zombie.setHealth(zombie.getHealth() - 15); // เลือดจะลด 15 Hp เมื่อกระสุนชน

                    if (zombie.getHealth() <= 0) {
                        zombie.setPosition(-100, -100);  // ซ่อนซอมบี้ที่โดนยิงตายแล้ว
                    }

                    bullet.setActive(false); // กระสุนที่ใช้ไปจะหยุดทำงาน
                    break;
                }
            }
        }
        bullets.removeIf(bullet -> !bullet.isActive()); // ลบกระสุนที่ไม่ทำงานแล้ว
    }
        

    public void updateZombiePosition(int index, int newPositionX, int newPositionY) {
        if (index < zombies.length) {
            zombies[index].setPosition(newPositionX, newPositionY); // อัปเดตตำแหน่งซอมบี้
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

    public int getTarget()
    {
        return this.target;
    }

    public int setTarget(int targetid)
    {
        return this.target = targetid;
    }
    
}

class ZombieSpawner implements Runnable {
    private BackgroundPanel panel;
    private int amountZombie;
    private Player player;
    private ClientManager client;
    
    ZombieSpawner(BackgroundPanel panel, int amountZombie, Player player, ClientManager client) {
        this.panel = panel;
        this.amountZombie = amountZombie;
        this.player = player;
        this.client = client;
    }
    

    @Override
    public void run() {
        for (int i = 0; i < amountZombie; i++) {
            client.getTarget();
            panel.showNextZombie(); // แสดงซอมบี้ตัวถัดไป
            ZombieMover zombieMover = new ZombieMover(panel.getZombies()[i], panel, panel.getTarget()); // ใช้ getZombies() เพื่อเข้าถึง
            // System.out.println(panel.getTarget());
            new Thread(zombieMover).start();
            // รอ 2 วินาทีก่อนสร้างซอมบี้ตัวต่อไป
            try {
                Thread.sleep(1500); // รอ 1.5 วินาที
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


// คลาสที่จัดการการเคลื่อนที่ของซอมบี้
class ZombieMover implements Runnable {
    private Zombie zombie; 
    private BackgroundPanel panel; 
    private int moveToId;
    int[] possiblePositions = {50, 210, 370, 530};

    ZombieMover(Zombie zombie, BackgroundPanel panel, int moveToId) {
        this.zombie = zombie;
        this.panel = panel;
        this.moveToId = moveToId;
    }

    @Override
    public void run() {
        int targetX = 50;
        while (zombie.getPositionX() > targetX) {
            zombie.setPosition(zombie.getPositionX() - 2, zombie.getPositionY()); 
            
            if (zombie.getPositionY() != possiblePositions[moveToId] && zombie.getPositionY() < possiblePositions[moveToId])
                zombie.setPosition(zombie.getPositionX(), zombie.getPositionY()+1);
            else
                zombie.setPosition(zombie.getPositionX(), zombie.getPositionY()-1);

            panel.updateZombiePosition(zombie.getId(), zombie.getPositionX(), zombie.getPositionY());
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
    private int x, y;
    private boolean active;

    public Bullet(int x, int y) {
        this.x = x; // จะเป็น spaceshipX + spaceshipWidth เพื่อให้ออกจากหัวยาน
        this.y = y; // จะเป็น spaceshipY + spaceshipHeight / 2 เพื่อให้กระสุนออกจากกลางยาน
        this.active = true;
    }

    public void move() {
        x += 10; // กระสุนจะเคลื่อนที่ไปทางขวา
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
