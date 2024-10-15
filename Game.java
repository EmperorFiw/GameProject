import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
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

    Gamepage(){
        frame = new JFrame("Zombie Hunter");
        frame.setSize(screenSize.width,screenSize.height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);


        // panel of Background
        BackgroundPanel panelBG = new BackgroundPanel();
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

/* ------------------------------ Zombie -------------------------------- */
    int spaceshipWidth = 150; // size
    int spaceshipHeight = 150; // size
    int spaceshipBetween = 120; // ค่า default ระยะห่างเริ่มต้น
    int[] zombiePosition;   

    BackgroundPanel(){
        bgimage = new ImageIcon(getClass().getResource("/image/newBackG.jpg")).getImage(); // get image background มา

        spaceshipImage = new ImageIcon(getClass().getResource("/image/spaceship.png")).getImage(). // get Image spaceship มา และ ปรับ scale ของภาพ 
        getScaledInstance(spaceshipWidth, spaceshipHeight, Image.SCALE_SMOOTH);        

        zombieNoob = new ImageIcon(getClass().getResource("/image/zombie_noob.gif"));

        int amountZombie = 20; // จำนวน zombie ที่ fix ไว้
        zombiePosition = new int[amountZombie + 1]; //เก็บตำแหน่งของ zombie
        Random random = new Random();

        // loop random หา position ของ zombie ตาม index ที่ i
        for (int i = 0; i <= amountZombie; i++) {
            zombiePosition[i] = random.nextInt(111) + 1290; // random Position ของzombie ตั้งแต่ 1290 - 1400
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(bgimage, 0, 0,getWidth(),getHeight(),this); // สั่งให้วาดภาพ background 

        int spaceshipX = 0; // กำหนดให้ มันเริ่ม x = 0 
        int spaceshipY = 50; // ให้มันนเริ่มข้างล่าง   Y

        int betweenZombie = 160; // ระยะห่างของ zombie

        // loop สร้าง spaceship
        for(int i=0; i<1; i++){
            g.drawImage(spaceshipImage, spaceshipX, spaceshipY, spaceshipWidth, spaceshipHeight, this); //สั่งให้วาด spaceship 
            spaceshipY += spaceshipWidth + spaceshipBetween; // ทำการเพิ่มระยะห่างให้กับ spaceship
        }

        // loop สร้างzombie 
        for(int i=0;i<zombiePosition.length -1; i++){
            g.drawImage(zombieNoob.getImage(), zombiePosition[i], betweenZombie, 150, 150, this); //สั่งวาด getImage,position,ระยะงห่าง,150คือขนาด,อ้างอิง
            System.out.println("ตัวที่ "+i);
        }


    }
}


