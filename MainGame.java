import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
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
        MainMenu menu = new MainMenu();
        menu.showMainMenu(true, client);
    }
}

class ClientManager {
    private Set<Integer> roomEx = new HashSet<>();
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private Player player; // เพิ่มตัวแปร Player

    JFrame frame = new JFrame();

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // สร้าง Player ใหม่เมื่อเชื่อมต่อ
            player = new Player("Player 1", 0, -1, false); // ตั้งค่าเริ่มต้น (สามารถเปลี่ยนแปลงได้ตามความเหมาะสม)

            new Thread(() -> {
                try {
                    Object messageFromServer;
                    while ((messageFromServer = in.readObject()) != null) {
                        if (messageFromServer instanceof String) {
                            String message = (String) messageFromServer;
                            System.out.println(message);
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

    public void addRoomEx(int roomID) {
        roomEx.add(roomID); // เพิ่มห้องเข้าไปใน Set
        System.out.println("Added Room ID: " + roomID); // แจ้งเมื่อเพิ่มห้อง
        System.out.println("Current roomEx: " + roomEx); // แสดงค่าปัจจุบันของ roomEx
    }

    public boolean isRoomExist(int roomNumber) {
        System.out.println("askr : "+roomEx.contains(roomNumber));
        return roomEx.contains(roomNumber); // ตรวจสอบว่ามีห้องนี้อยู่หรือไม่
    }

    public void removeRoomEx(int roomID) {
        roomEx.remove(roomID); // ฟังก์ชันสำหรับลบห้อง
    }

    
}
