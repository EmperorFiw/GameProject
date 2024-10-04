
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Server {
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static Set<PlayerSerializable> players = Collections.synchronizedSet(new HashSet<>());
    private static Queue<Integer> freeIDs = new LinkedList<>(); // คิวเพื่อเก็บไอดีที่ว่าง
    private static int nextID = 0; // ตัวนับเพื่อให้ไอดีใหม่
    public static Map<Integer, CreateRoomFrame> roomFrames = new HashMap<>();

    public static void main(String[] args) {
        int port = 7777;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                int playerId;

                // ดึงไอดีจากคิวถ้ามีไอดีว่าง
                if (!freeIDs.isEmpty()) {
                    playerId = freeIDs.poll(); // ดึงไอดีที่ว่าง
                } else {
                    playerId = nextID++; // ใช้ไอดีใหม่
                }

                String playerName = "Player " + playerId;
                PlayerSerializable newPlayer = new PlayerSerializable(playerName, playerId, -1);
                players.add(newPlayer);

                System.out.println(playerName + " has joined the server.");

                ClientHandler clientHandler = new ClientHandler(socket, newPlayer);
                clients.add(clientHandler);
                new Thread(clientHandler).start(); // เริ่ม Thread สำหรับ ClientHandler
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removePlayer(PlayerSerializable player) {
        // เพิ่มไอดีที่ถูกลบกลับไปยังคิว
        freeIDs.add(player.getId()); // ให้ไอดีกลับไปยังคิวเพื่อใช้ใหม่
        players.remove(player);
        System.out.println(player.getName() + " has been removed from the server.");

    }

    public static void sendClientMessageToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void sendClientMessage(int rid, String message) {
        synchronized (clients) {  // ใช้ synchronized เพื่อป้องกันปัญหาหลาย thread
            for (ClientHandler client : clients) {
                if (client.getPlayer().getRoomID() == rid) {  // ตรวจสอบว่าผู้เล่นอยู่ในห้องที่ต้องการหรือไม่
                    client.sendMessage(message);  // ส่งข้อความไปยังผู้เล่นในห้องนั้น
                }
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        synchronized (clients) {
            clients.remove(clientHandler);
        }
    }

    public static void sendJoinRoom(int rid, int playerid, String playerName)
    {
        ClientManager clientManager = new ClientManager(); 
        CreateRoomFrame rframe = new CreateRoomFrame(new menuFrame(clientManager)); // ส่ง ClientManager ไปยัง menuFrame
        rframe.setTextForEmpty(playerid, playerName);
    }
    
    
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PlayerSerializable player;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static Set<Integer> existingRoomIDs = new HashSet<>();
    private ClientManager client = new ClientManager();

    
    public ClientHandler(Socket socket, PlayerSerializable player) {
        this.socket = socket;
        this.player = player;
    }
    public PlayerSerializable getPlayer() {
        return player;
    }


    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
    
            Object clientMessage;
            while (true) {
                try {
                    clientMessage = in.readObject();
                    if (clientMessage instanceof PlayerAction) {
                        handlePlayerAction((PlayerAction) clientMessage);
                    }

                } catch (SocketException e) {
                    // แสดงข้อความที่ชัดเจนเมื่อ Client ถูกตัดการเชื่อมต่อ
                    System.out.println(player.getName() + " has disconnected (Connection reset).");
                    Server.removePlayer(player);
                    break; // ออกจากลูป
                } catch (IOException e) {
                    System.out.println("I/O error: " + e.getMessage());
                    break; // ออกจากลูปเมื่อมีข้อผิดพลาดในการอ่าน
                } catch (ClassNotFoundException e) {
                    System.out.println("Class not found: " + e.getMessage());
                    break; // ออกจากลูปเมื่อไม่พบคลาส
                }
            }
    
        } catch (IOException e) {
            // กรณีที่เกิดข้อผิดพลาดขณะสร้าง ObjectOutputStream หรือ ObjectInputStream
            System.out.println(player.getName() + " has left the room.");
            Server.removePlayer(player);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    private void handlePlayerAction(PlayerAction action) {
        switch (action.getActionType()) {
            case SET_NAME:
                player.setName(action.getName());
                // System.out.println("client req name");
                sendMessage("Player name set to: " + action.getName());
                break;
            case JOIN_ROOM:
                int roomNumber = action.getRoomID();
                if (!IsRoomExists(roomNumber)) {
                    sendMessage("RoomNotExists");
                } else {
                    player.setRoomID(roomNumber);
                    
                    // ใช้ CreateRoomFrame เดิมถ้าห้องมีอยู่แล้ว
                    CreateRoomFrame rframe;
                    if (Server.roomFrames.containsKey(roomNumber)) {
                        rframe = Server.roomFrames.get(roomNumber);
                    } else {
                        rframe = new CreateRoomFrame(new menuFrame(client));
                        Server.roomFrames.put(roomNumber, rframe);
                    }
                    Menu menu = new Menu();
                    menu.showMainMenu(false, client);
                    menuFrame mf = new menuFrame(client);
                    mf.setVisible(false);
                    CreateRoomFrame roomFrame = new CreateRoomFrame(mf);
                    roomFrame.setTextForEmpty(1, player.getName()); // เรียกใช้ทันทีเมื่อเปิด CreateRoomFrame
                    roomFrame.setVisible(true);
                    rframe.setTextForEmpty(player.getId(), player.getName());
                    
                    sendMessage("RoomExists" + roomNumber);
                    Server.sendClientMessage(roomNumber, player.getName() + " has joined the room " + roomNumber);

                }
                break;
            
            case CREATE_ROOM:
                int newRoomID = setRoom(player.getName());
                player.setRoomID(newRoomID);
                
                // สร้าง CreateRoomFrame สำหรับห้องใหม่
                CreateRoomFrame rframe = new CreateRoomFrame(new menuFrame(client));
                Server.roomFrames.put(newRoomID, rframe);
                
                rframe.setTextForEmpty(player.getId(), player.getName());
                
                sendMessage("RoomCreated: " + newRoomID);
                Server.sendClientMessageToAll(player.getName() + " has created room " + newRoomID);
                break;
            
            case GET_NAME:
                sendMessage(player.getName());
                break;
            case EXIT_GAME:
                System.out.println(player.getName() + " Try Exit");
                Server.removePlayer(player);
            
                // ตรวจสอบว่า socket ยังไม่ถูกปิด
                if (out != null) {
                    try {
                        out.writeObject("Goodbye"); // ส่งข้อความก่อนปิด
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            
                try {
                    if (!socket.isClosed()) {
                        socket.close(); // ปิด socket
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Server.removeClient(this);
                    System.out.println(player.getName() + " has exited the game.");
                }
                break;            
            
        }
    }

    public void sendMessage(Serializable message) {
        if (out != null) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
    public int setRoom(String playerName) {
        int roomID;
        do {
            roomID = (int) (Math.random() * 900000) + 100000;
        } while (existingRoomIDs.contains(roomID));

        existingRoomIDs.add(roomID);
        System.out.println("Room ID: " + roomID);
        // สร้าง ClientManager
       // ClientManager clientManager = new ClientManager(); 
        CreateRoomFrame rframe = new CreateRoomFrame(new menuFrame(client)); // ส่ง ClientManager ไปยัง menuFrame
        rframe.setTextForEmpty(0, playerName);
        
        return roomID;
    }

    public boolean IsRoomExists(int id) {
        return existingRoomIDs.contains(id);
    }   
}
//ค่าจริงที่เก็บอยู่
class PlayerSerializable implements Serializable {
    private String name;
    private int id; 
    private int roomID;

    public PlayerSerializable(String name, int id, int roomID) {
        this.name = name;
        this.id = id;
        this.roomID = roomID;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getRoomID() {
        return roomID;
    }

}


//เอาไว้เก็บการทำงานจากฝั่ง client
class PlayerAction implements Serializable {
    public enum ActionType { 
        SET_NAME, 
        JOIN_ROOM, 
        CREATE_ROOM, 
        GET_NAME, 
        EXIT_GAME
    }

    private ActionType actionType;
    private String name;
    private int roomID;

    public PlayerAction(ActionType actionType) {
        this.actionType = actionType;
    }

    public PlayerAction(ActionType actionType, String name) {
        this.actionType = actionType;
        this.name = name;
    }

    public PlayerAction(ActionType actionType, int roomID) {
        this.actionType = actionType;
        this.roomID = roomID;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getName() {
        return name;
    }

    public int getRoomID() {
        return roomID;
    }
}

