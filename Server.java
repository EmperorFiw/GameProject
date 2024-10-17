import java.io.EOFException;
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
import java.util.Random;
import java.util.Set;

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

public class Server {
    private Player player;
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static Queue<Integer> freeIDs = new LinkedList<>(); // Queue for storing available IDs
    private static int nextID = 0; // Counter for generating new IDs
    public static Map<Integer, List<String>> roomPlayers = new HashMap<>(); // Room player lists
    private static ClientManager client = new ClientManager();
    private static MainMenu mn = new MainMenu();
    private static Game game = new Game();
    public static void main(String[] args) {
        int port = 7777;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                int playerId;

                // Get an ID from the queue if available
                if (!freeIDs.isEmpty()) {
                    playerId = freeIDs.poll(); // Retrieve available ID
                } else {
                    playerId = nextID++; // Use a new ID
                }

                String playerName = "Player " + playerId;

                System.out.println(playerName + " has joined the server.");

                ClientHandler clientHandler = new ClientHandler(socket, playerName, playerId, client);
                clients.add(clientHandler);
                
                new Thread(clientHandler).start(); // Start a new thread for ClientHandler
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removePlayer(Player player) {
        // Add the removed player's ID back to the queue
        freeIDs.add(player.getId());
        if (player.isOwner())
        {
            player.setOwner(false);
        }
        clients.removeIf(client -> client.getPlayer().getId() == player.getId());
        if (clients.removeIf(client -> client.getPlayer().getRoomID() != -1))
        {
            updatePlayerInRoomByIndex(player, player.getName());
        }
        System.out.println(player.getName() + " has been removed from the server.");
    }

    public static void sendClientMessageToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    public static void sendClientMessage(int rid, String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getPlayer().getRoomID() == rid) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        synchronized (clients) {
            clients.remove(clientHandler);
        }
    }

    public static void sendJoinRoom(Player player, int rid, int playerid, String playerName) {
        System.out.println("Player " + playerName + " with ID " + playerid + " joined room " + rid);
    
        // เพิ่มชื่อผู้เล่นใหม่ลงใน roomPlayers
        synchronized (roomPlayers) {
            roomPlayers.putIfAbsent(rid, new ArrayList<>()); // ตรวจสอบให้แน่ใจว่าห้องนั้นถูกสร้างขึ้นแล้ว
            if (!roomPlayers.get(rid).contains(playerName)) {
                roomPlayers.get(rid).add(playerName); // เพิ่มชื่อผู้เล่นในห้อง
                System.out.println(playerName + " added to room " + rid);
                updatePlayerInRoom(player, playerName, rid); // เรียกใช้ updatePlayerInRoom
            } else {
                System.out.println(playerName + " already in room " + rid);
            }
        }
    
        // แสดงจำนวนผู้เล่นในแต่ละห้อง
        Map<Integer, Integer> roomCounts = Server.countPlayersInRooms();
        for (Map.Entry<Integer, Integer> entry : roomCounts.entrySet()) {
            System.out.println("Room ID: " + entry.getKey() + ", Player Count: " + entry.getValue());
        }
    
        // ปิดหน้าเมนู
        mn.showMainMenu(false, client);
    }
    
    public static void updatePlayerInRoom(Player player, String name, int rid) {
        // วนลูปผ่าน clients
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getPlayer().getRoomID() == rid) {
                // ดึงรายชื่อผู้เล่นทั้งหมดในห้อง
                List<String> playersInRoom = roomPlayers.get(rid); 
                
                if (playersInRoom != null) {
                    // แสดงชื่อผู้เล่นทั้งหมดในห้อง
                    for (int i = 0; i < playersInRoom.size(); i++) {
                        String playerName = playersInRoom.get(i);
                        //System.out.println("Player in room " + rid + ": " + playerName);
                        clientHandler.sendRoomData(i, playerName);
                        
                        player.addInNameRoom(i, playerName); // ใช้ playerName แทน player.getName()
                    }
                } else {
                    System.out.println("No players in room " + rid);
                }
            }
        }
    }
    

    public static List<Integer> getRoomIDsOfAllPlayers() {
        List<Integer> roomIDs = new ArrayList<>();
        synchronized (clients) {
            for (ClientHandler client : clients) {
                Player player = client.getPlayer(); // ดึง Player object
                int roomID = player.getRoomID(); // ดึง roomID ของผู้เล่น
                if (!roomIDs.contains(roomID)) { // ตรวจสอบว่า roomID ยังไม่มีในลิสต์หรือไม่
                    roomIDs.add(roomID); // เพิ่ม roomID ลงในลิสต์
                }
            }
        }
        return roomIDs; // คืนค่าลิสต์ของ roomID
    }

    public static Map<Integer, Integer> countPlayersInRooms() {
        Map<Integer, Integer> roomPlayerCount = new HashMap<>();
        
        // ดึงหมายเลขห้องของผู้เล่นทั้งหมด
        List<Integer> allRoomIDs = getRoomIDsOfAllPlayers();
        
        synchronized (clients) {
            for (Integer roomID : allRoomIDs) {
                int count = 0;
    
                // วนลูปผู้เล่นใน clients
                for (ClientHandler client : clients) {
                    Player player = client.getPlayer(); // ดึงข้อมูลผู้เล่นจาก ClientHandler
                    if (player.getRoomID() == roomID) {
                        count++; // เพิ่มจำนวนผู้เล่นในห้องนี้
                    }
                }
    
                roomPlayerCount.put(roomID, count); // บันทึกจำนวนผู้เล่นในห้องนั้นลงใน Map
            }
        }
        return roomPlayerCount; // คืนค่า Map ที่เก็บจำนวนผู้เล่นในแต่ละห้อง
    }
    

    

    public static void updatePlayerInRoomByIndex(Player player, String name) {
        // ดึงรายชื่อผู้เล่นทั้งหมดในห้อง
        List<String> playersInRoom = roomPlayers.get(player.getRoomID());
        
        if (playersInRoom != null) {
            // ค้นหาตำแหน่งของผู้เล่นในลิสต์
            int index = playersInRoom.indexOf(name);
            if (index != -1) {
                // แทนที่ชื่อผู้เล่นด้วย "Empty"
                playersInRoom.remove(index);
                
                // ส่งข้อมูลไปยัง client
                for (ClientHandler clientHandler : clients) {
                    if (clientHandler.getPlayer().getRoomID() == player.getRoomID()) {
                        // วนลูปเช็คแต่ละช่องใน allPlayerInRoom
                        for (int i = 0; i < 4; i++) {
                            // ตรวจสอบว่าชื่อในช่องนี้ตรงกับ player.getName()
                            if (player.getPlayerInRoomFromIndex(i).equals(player.getName())) {
                                // ถ้าตรงกัน ส่งข้อมูล index ที่เจอไปยัง client
                                clientHandler.sendRoomData(i, "Empty");  // ส่งข้อมูลอัปเดตให้ client
                            }
                        }
                    }
                }
                
    
                System.out.println("Player " + name + " removed from room.");
            } else {
                System.out.println("Player " + name + " not found in room.");
            }
        }
    }
    
    

    public static boolean isRoomFull(int rid) {
        synchronized (roomPlayers) {
            List<String> playersInRoom = roomPlayers.get(rid);
            if (playersInRoom != null) {
                return playersInRoom.size() >= 4;
            }
        }
        return false; 
    }
    
    public static void startGame(int rid)
    {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getPlayer().getRoomID() == rid) {
                    client.sendMessage("Game Start!!");
                    client.hideMainFrame(rid);
                    client.playGame(game, rid);
                    client.getPlayer().setInGame(true);
                    //game.playGame(client.getPlayerObject());
                }
            }
        }
    }
    
    public static void getTarget(Player player)
    {
        Random random = new Random();
        int target = random.nextInt(player.getCountPlayer());
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getPlayer().getInGame()) {
                    client.sendTarget(target);
                }
            }
        }
        
    }

}

class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static Set<Integer> existingRoomIDs = new HashSet<>();
    private Player player; // Use Player instead of ClientManager
    private ClientManager client;

    public ClientHandler(Socket socket, String playerName, int playerId, ClientManager client) {
        this.socket = socket;
        this.player = new Player(playerName, playerId, -1, false); // Create new Player
        this.client = client;
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
                    if (clientMessage instanceof Integer) {
                        int commandId = (Integer) clientMessage;

                        switch (commandId) {
                            case 0: // create room
                                createRoom();
                                break;
                            case 1: // Join
                                int roomNumber = (int) in.readObject(); // Get the room number from the client
                                joinRoom(roomNumber);

                                break;
                            
                            case 2: // change name
                                int commandType = (Integer) in.readObject(); // Get the type (0 for creating room, 1 for changing name)
                                String newName = (String) in.readObject(); // Get the new name from the client
                            
                                if (commandType == 0) {
                                    changeName(newName); 
                                    createRoom();

                                } else if (commandType == 1) {
                                    changeName(newName); 
                                }
                                break;
                            case 3:
                                int checkRoomNumber = (int) in.readObject();
                                
                                out.writeObject(3);
                                if (Server.roomPlayers.containsKey(checkRoomNumber))
                                {
                                    if (Server.isRoomFull(checkRoomNumber))
                                    {
                                        out.writeObject(false);
                                    }
                                    else
                                    {
                                        out.writeObject(true);
                                    }
                                }
                                else
                                {
                                    out.writeObject(false);
                                }
                                out.writeObject(checkRoomNumber);
                                out.flush();
                                break;
                            case 4: // start game
                                int getRid = (int) in.readObject();
                                Server.startGame(getRid);
                                break;
                            case 5:
                                Server.getTarget(player);
                                break;
                            case 99:
                            {
                                Player cplayer = (Player) in.readObject();//client.getPlayerData();
                                Server.updatePlayerInRoomByIndex(cplayer, cplayer.getName());
                                cplayer.setRoomID(-1);
                                System.out.println(cplayer.getName()+ " has leave the room.");  
                                break;
                            }
                            default:
                                System.out.println("Unknown command ID: " + commandId);
                        }
                    }
                    // Thread แยกสำหรับการส่งข้อมูล player แบบต่อเนื่อง
                    new Thread(() -> {
                        try {
                            int i=0;
                            boolean running = true;
                            while (running && !socket.isClosed()) {
                                synchronized(out) {
                                    if (i == 4)
                                        i = 0;
                                    if (player.getInGame())
                                        running = false;

                                    out.writeObject(0);  // ส่ง Integer
                                    out.writeObject(player.getName());  // ส่ง String
                                    out.writeObject(player.getId());  // ส่ง Integer
                                    out.writeObject(player.getRoomID());  // ส่ง Integer
                                    out.writeObject(player.isOwner());  // ส่ง Boolean
                                    out.writeObject(i);  // ส่ง Integer
                                    out.writeObject(player.getPlayerInRoomFromIndex(i));  // ส่ง String
                                    out.flush();
                                    i++;
                                }
                                
                                Thread.sleep(1000);  // รอ 1 วินาที
                            }
                        } catch (IOException | InterruptedException e) {
                            System.out.println("Error while sending player data: " + e.getMessage());
                        }
                        
                    }).start();
                } catch (SocketException e) {
                    System.out.println(player.getName() + " has disconnected (Connection reset).");
                    
                    Server.removePlayer(player);
                    break;
                } catch (EOFException eof) {
                    System.out.println(player.getName() + " Client disconnected or reached end of file.");
                    Server.removePlayer(player);
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
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

    public void sendMessage(Serializable message) {
        if (out != null) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRoomData(int index, Serializable name) {
        if (out != null) {
            try {
                player.addInNameRoom(index, (String)name);
                out.writeObject(1);
                out.writeObject(index);
                out.writeObject(name);
                out.flush();


             } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public int setRoom() {
        int roomID;
        do {
            roomID = (int) (Math.random() * 900000) + 100000;
        } while (Server.roomPlayers.containsKey(roomID)); // ตรวจสอบจาก Server
    
        player.setOwner(true);
        player.setRoomID(roomID); // ตรวจสอบให้แน่ใจว่าตั้งค่า roomID ให้กับ player
        System.out.println("Room ID: " + roomID);
    
        return roomID;
    }
    
    public Player getPlayerObject()
    {
        return this.player;
    }

    public void createRoom() {
        int newRoomID = setRoom(); // No arguments needed
        player.setRoomID(newRoomID);
    
        // Update player list in the room
        Server.roomPlayers.putIfAbsent(newRoomID, new ArrayList<>()); // Access roomPlayers via Server
        //Server.roomPlayers.get(newRoomID).add(player.getName());
    
        sendMessage("RoomCreated: " + newRoomID);
        //Server.sendClientMessageToAll(player.getName() + " has created room " + newRoomID);
        Server.sendJoinRoom(player, newRoomID, player.getId(), player.getName()); // ส่งพารามิเตอร์ครบ
    }
    
    
    
    public boolean joinRoom(int roomNumber) {
        if (Server.isRoomFull(roomNumber)) { // ตรวจสอบห้องว่าเต็มแล้วหรือยัง
            sendMessage("Room is full, cannot join.");
            return false; // คืนค่าหากห้องเต็ม
        }
    
        player.setRoomID(roomNumber);
        Server.roomPlayers.putIfAbsent(roomNumber, new ArrayList<>());
    
        sendMessage("RoomExists: " + roomNumber);
        Server.sendClientMessage(roomNumber, player.getName() + " has joined the room " + roomNumber);
        Server.sendJoinRoom(player, roomNumber, player.getId(), player.getName());
        
        return true; // คืนค่าประสบความสำเร็จ
    }
    
    
    public boolean roomExists(int roomNumber) {
        return Server.roomPlayers.containsKey(roomNumber);
    }
    
    public void changeName(String newName) {
        String oldName = player.getName(); // เก็บชื่อเดิม
        player.changeName(newName); // เรียก method changeName ใน class Player เพื่ออัพเดตชื่อ
    
        // ส่งข้อความให้ทุกคนในห้องว่าผู้เล่นได้เปลี่ยนชื่อ
        int roomID = player.getRoomID();

        // แจ้งผู้เล่นเอง
        sendMessage("Your name has been changed to " + newName);
    }

    public void hideMainFrame(int rid)
    {
        try {
            out.writeObject(4);
            out.writeObject(rid);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }
    
    public Player getPlayer()
    {
        return this.player;
    } 

    public void playGame(Game game, int rid)
    {
        try {
            out.writeObject(5);
            out.writeObject(rid);
            out.writeObject(game);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    public void sendTarget(int index)
    {
        try {
            out.writeObject(6);
            out.writeObject(index);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }
}
