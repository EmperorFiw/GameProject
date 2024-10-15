import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;


import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;


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

        // ImageIcon zombieGif = new ImageIcon(getClass().getResource("/image/zombie_noob.gif"));
        // Image scaledZombieGif = zombieGif.getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT);
        // ImageIcon scaledzombiechange = new ImageIcon(scaledZombieGif);

        // JLabel zombieLabel_1 = new JLabel(scaledzombiechange);
        // zombieLabel_1.setBounds(1150, 20, zombieGif.getIconWidth(), zombieGif.getIconHeight());

        // panelBG.add(zombieLabel_1);

        // panelBG.setComponentZOrder(zombieLabel_1, 0);  // ให้ GIF อยู่หน้า background


    }

    public void showFrame() {
        frame.setVisible(true);
    }
}

class BackgroundPanel extends JPanel {
    private Image bgimage;
    private Image spaceshipImage;

    private int spaceshipWidth = 150;
    private int spaceshipHeight = 150;

    BackgroundPanel(){
        bgimage = new ImageIcon(getClass().getResource("/image/newBackG.jpg")).getImage();
        spaceshipImage = new ImageIcon(getClass().getResource("/image/spaceship.png")).getImage().getScaledInstance(spaceshipWidth, spaceshipHeight, Image.SCALE_SMOOTH);        
        
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        g.drawImage(bgimage, 0, 0,getWidth(),getHeight(),this);

        int spaceshipX = 0; // กำหนดให้ มันเริ่ม x = 0 
        int spaceshipY = getHeight() - spaceshipHeight; // ให้มันนเริ่มข้างล่าง   
        
        g.drawImage(spaceshipImage, spaceshipX, spaceshipY, spaceshipWidth, spaceshipHeight, this); //สั่งให้วาด spaceship 
    }

}


