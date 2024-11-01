import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;
/*
/*
 * 0    Create Room
 * 1    Join Room
 * 2    Chnge Name
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
public class MainGame {
    public static void main(String[] args) {
        ClientManager client = new ClientManager();
        client.connectToServer();

        menuFrame mf = new menuFrame(client);  // สร้าง menuFrame
        MainMenu menu = new MainMenu();
        menu.showMainMenu(true, client);
        client.SendObject(mf, menu);
        

    }
}

class ClientManager {
    private menuFrame mf;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private MainMenu menu;
    private Socket socket;
    private Player player; // เพิ่มตัวแปร Player
    private Game game;
    private BackgroundPanel panelGame;


    public void SendObject(menuFrame mf, MainMenu menu) {
        this.mf = mf;
        this.menu = menu;
    }

    public void setPlayer(Player player)
    {
        this.player = player;
    }

    public void connectToServer() {
        try {
            socket = new Socket("10.160.92.96", 7777);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // สร้าง Player ใหม่เมื่อเชื่อมต่อ
            player = new Player("Player", 0, -1, false);

            new Thread(() -> {
                try {
                    Object messageFromServer;
                    while ((messageFromServer = in.readObject()) != null) {
                        if (messageFromServer instanceof String) {
                            String message = (String) messageFromServer;
                            if (!"Empty".equals(message))
                                System.out.println(message);
    
                        } else if (messageFromServer instanceof Integer) {
                            int commandId = (Integer) messageFromServer;
    
                            switch (commandId) {
                                case 0: //รับข้อมูลผู้เล่น
                                    if (player.getInGame())
                                    {
                                        // อ่านข้อมูลทิ้งโดยไม่ทำอะไรกับมัน
                                        in.readObject(); // ข้าม String หรือ Object ที่ถูกส่งมา
                                        in.readObject(); // ข้าม Integer
                                        in.readObject(); // ข้าม Integer หรือข้อมูลอื่น ๆ ที่ส่งมา
                                        in.readObject(); // ข้าม String หรือ Object ที่ถูกส่งมา
                                        in.readObject(); // ข้าม Integer
                                        in.readObject(); // ข้าม Integer หรือข้อมูลอื่น ๆ ที่ส่งมา
                                        System.out.println("Discarded case 1 data, player is in game.");
                                        break;
                                    }
                                    String name = (String) in.readObject();  // อ่าน String
                                    int playerId = (int) in.readObject();  // อ่าน Integer
                                    int roomId = (int) in.readObject();  // อ่าน Integer
                                    Boolean isOwner = (Boolean) in.readObject();  // อ่าน Boolean
                                    int ind = (int) in.readObject();  // อ่าน Integer
                                    String addName = (String) in.readObject();  // อ่าน String

                                    this.player.changeName(name);
                                    this.player.setId(playerId);
                                    this.player.setRoomID(roomId);
                                    this.player.setOwner(isOwner);
                                    this.player.addInNameRoom(ind, addName);
                                    break;
                                case 1: // จัดเก็บรายชื่อในห้อง
                                    try {
                                        Integer index = (Integer) in.readObject(); // รับตำแหน่งผู้เล่น
                                        String pName = (String) in.readObject(); // รับชื่อผู้เล่น
                                        
                                        this.player.addInNameRoom(index, pName); // เพิ่มชื่อผู้เล่นในห้อง
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 3: // create room
                                    boolean canJoin = (Boolean) in.readObject();
                                    int rid = (Integer) in.readObject();
                                    System.out.println("can join "+canJoin+rid);
                                    if (canJoin)
                                    {
                                        out.writeObject(1);
                                        out.writeObject(rid);
                                        out.flush();
                                        mf.joinRoomFrame();
                                        player.setRoomID(rid);
                                    }
                                    else
                                    {
                                        JOptionPane.showMessageDialog(null, "Room does not exist or room is full!.", "Error", JOptionPane.ERROR_MESSAGE);
                                        mf.roomNotFound();
                                    }
                                    break;
                                case 4: //hide main frame
                                    int hRid = (Integer) in.readObject();
                                    if (player.getRoomID() == hRid && player.isOwner() == false)
                                        mf.disRFrame();
                                    break;
                                case 5:
                                    Boolean isInGame = (Boolean) in.readObject();  // อ่าน Boolean
                                    Integer roomID = (Integer) in.readObject();
                                    Object gameObject = in.readObject();

                                    if (player.getRoomID() == roomID)
                                    {
                                        this.player.setInGame(isInGame);
                                        if (gameObject instanceof Game) {
                                            this.game = (Game) gameObject; // แปลงเป็น Game
                                            sendStartGame();
                                        } else {
                                            System.out.println("Received object is not of type Game");
                                        }
                                    }
                                    break;
                                case 6:
                                    int[] targetArr = (int[]) in.readObject(); 
                                    BackgroundPanel panel = game.getPanelObject();
                                    panel.setTarget(targetArr);
                                    break;
                                case 7:
                                    String typeGame = (String) in.readObject();
                                    BackgroundPanel panelgdata = game.getPanelObject();
                                    this.panelGame = panelgdata;
                                
                                    if (typeGame.equals("Zombie")) {
                                        int[] zdata = (int[]) in.readObject();
                                        synchronized (panelgdata) {  // ซิงโครไนซ์การอัปเดตข้อมูลซอมบี้
                                            panelgdata.updateZombie(zdata[0], zdata[1], zdata[2], zdata[3]);
                                            if (zdata.length > 4 && zdata[4] >= 80)
                                            {
                                                if (!player.isGameOver())
                                                {
                                                    victory();
                                                    player.setGameOver(true);
                                                }
                                            }

                                        }
                                    } else if (typeGame.equals("Bullet")) {
                                        int[] bdata = (int[]) in.readObject();
                                        synchronized (panelgdata) {  // ซิงโครไนซ์การวาดกระสุน
                                            panelgdata.drawBullet(bdata[0], bdata[1], bdata[2], bdata[3]);
                                        }
                                    }
                                    break;
                                
                                default:
                                    System.out.println("Unknow command");
                                    break;
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Connection was reset: " + e.getMessage());
                    closeConnection();
                    System.exit(0);
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Found error");
                    e.printStackTrace();
                    closeConnection(); // ปิดการเชื่อมต่อเมื่อเกิดข้อผิดพลาด
                }
            }).start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public void changeName(String name, int type) {
        try {
            synchronized(out) {
                out.writeObject(2);  // ส่งคำสั่ง ID 2 สำหรับการเปลี่ยนชื่อ
                if (type == 0) {
                    out.writeObject(0);
                } else {
                    out.writeObject(1);
                }
                out.writeObject(name);  // ส่งชื่อใหม่ที่ต้องการเปลี่ยน
                out.flush();
            }
            
        } catch (IOException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
        }
    }
    
    public synchronized void isRoomExist(int roomNumber) {
        try {
            out.writeObject(3);
            out.writeObject(roomNumber);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
        }
    }

    public synchronized void startGame(CreateRoomFrame frame, int rid)
    {
        frame.setVisible(false);
        try {
            out.writeObject(4);
            out.writeObject(rid);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    public Player getPlayerData()
    {
        return this.player;
    }

    public synchronized void leaveRoom()
    {
        try {
            out.writeObject(99);
            out.writeObject(this.player);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
        }
    }

    public void sendStartGame() {
        try {
            Thread.sleep(1000); // หน่วงเวลา 1 วินาที
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // จัดการกับการถูกระงับ
        }
        game.startGame(player, this);
    }
    
    public synchronized void getTarget()
    {
        try {
            out.writeObject(5);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendUpdateZP(int zid, int zpX, int zpY, int hp, int deathCount, int isDeath)
    {
        int[] zdata = new int[5];
        zdata[0] = zid;
        zdata[1] = zpX;
        zdata[2] = zpY;
        zdata[3] = hp;
        if (isDeath == 1)
            zdata[4] = deathCount;
        try {
            out.writeObject(6);
            out.writeObject("Zombie");
            out.writeObject(zdata);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void drawBullet(int x, int y, int tx, int ty)
    {
        int[] bdata = new int[4];
        bdata[0] = x;
        bdata[1] = y;
        bdata[2] = tx;
        bdata[3] = ty;
        try {
            out.writeObject(6);
            out.writeObject("Bullet");
            out.writeObject(bdata);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void victory() {
        panelGame.hideBG(menu);
    }
}
