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
            player = new Player("Player", -1, -1, false);

            new Thread(() -> {
                try {
                    Object messageFromServer;
                    while ((messageFromServer = in.readObject()) != null) {
                        if (messageFromServer instanceof String) {
                            String message = (String) messageFromServer;
                            System.out.println(message);
    
                        } else if (messageFromServer instanceof Integer) {
                            int commandId = (Integer) messageFromServer;
    
                            switch (commandId) {
                                case 0: //รับข้อมูลผู้เล่น
                                    String name = (String) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    int playerId = (int) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    int roomid = (int) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    Boolean isOwner = (Boolean) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                
                                    this.player.changeName(name);
                                    this.player.setId(playerId);
                                    this.player.setRoomID(roomid);
                                    this.player.setOwner(isOwner);

                                    break;
                                case 1: //จัดเก็บรายชื่อในห้อง
                                    int index = (int) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    String pName = (String) in.readObject(); // รับข้อมูล Player จากเซิร์ฟเวอร์
                                    System.out.println("Get name");
                                    this.player.addInNameRoom(index, pName);
                                    break;
                                case 3: // create room
                                    boolean canJoin = (Boolean) in.readObject();
                                    int rid = (Integer) in.readObject();
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
                                        JOptionPane.showMessageDialog(null, "Room does not exist. Please enter a valid room number.", "Error", JOptionPane.ERROR_MESSAGE);
                                        mf.roomNotFound();
                                    }
                                    break;
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.out.println("Connection was reset: " + e.getMessage());
                    closeConnection();
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
            out.writeObject(2); // ส่งคำสั่ง ID 2 สำหรับการเปลี่ยนชื่อ
            if (type == 0)
            {
                out.writeObject(0);
            } 
            else
            {
                out.writeObject(1); 
            }
            out.writeObject(name); // ส่งชื่อใหม่ที่ต้องการเปลี่ยน
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
        }
    }
    
    public boolean joinRoom(int rid) {
        try {
            out.writeObject(rid); // ส่งหมายเลขห้องไปยังเซิร์ฟเวอร์
            out.flush();
    
            return (boolean) in.readObject(); // รับค่าจากเซิร์ฟเวอร์
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(); // พิมพ์ stack trace เพื่อช่วยในการ debug
            return false; // ถ้าเกิดข้อผิดพลาดให้คืนค่า false
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

}
