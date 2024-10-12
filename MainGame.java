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
        client.setMenuFrame(mf);
        
        MainMenu menu = new MainMenu();
        menu.showMainMenu(true, client);

    }
}

class ClientManager {
    private menuFrame mf;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private MainMenu mainmenu;
    private Socket socket;
    private Player player; // เพิ่มตัวแปร Player

    public void setMenuFrame(menuFrame mf) {
        this.mf = mf;
    }

    public void setPlayer(Player player)
    {
        this.player = player;
    }

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
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
                                    String name = (String) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    int playerId = (int) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    int roomid = (int) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    boolean isOwner = (boolean) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    int ind = (int) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    String addName = (String) in.readObject();
                                    
                                    this.player.changeName(name);
                                    this.player.setId(playerId);
                                    this.player.setRoomID(roomid);
                                    this.player.setOwner(isOwner);
                                    this.player.addInNameRoom(ind, addName);
                                    System.out.println(addName);
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
    
    public void isRoomExist(int roomNumber) {
        try {
            out.writeObject(3);
            out.writeObject(roomNumber);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
        }
    }

    public Player getPlayerData()
    {
        return this.player;
    }

    public void leaveRoom()
    {
        try {
            out.writeObject(99);
            out.writeObject(this.player);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
        }
    }
}
