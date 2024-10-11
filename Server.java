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

    public static void sendJoinRoom(int rid, int playerid, String playerName) {
        System.out.println("Player " + playerName + " with ID " + playerid + " joined room " + rid);
        // ปิดหน้าเมนู
        mn.showMainMenu(false, client);
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
                    // Thread แยกสำหรับการส่งข้อมูล player แบบต่อเนื่อง
                    new Thread(() -> {
                        try {
                            while (!socket.isClosed()) {
                                out.writeObject(0);  // ส่งรหัสคำสั่ง
                                out.writeObject(player.getName());  // ส่งข้อมูล player
                                out.writeObject(player.getId());  // ส่งข้อมูล player
                                out.writeObject(player.getRoomID());  // ส่งข้อมูล player
                                out.writeObject(player.isOwner());  // ส่งข้อมูล player
                                out.flush();
                                Thread.sleep(1000);  // รอ 1 วินาที
                            }
                        } catch (IOException | InterruptedException e) {
                            System.out.println("Error while sending player data: " + e.getMessage());
                        }
                    }).start();

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
                                    createRoom();
                                    changeName(newName); 

                                } else if (commandType == 1) {
                                    changeName(newName); 
                                }
                                break;
                            case 3:
                                int checkRoomNumber = (int) in.readObject();
                                out.writeObject(3);
                                out.writeObject(Server.roomPlayers.containsKey(checkRoomNumber));
                                out.writeObject(checkRoomNumber);
                                out.flush();
                                break;
                            
                            default:
                                System.out.println("Unknown command ID: " + commandId);
                        }
                    }
                } catch (SocketException e) {
                    System.out.println(player.getName() + " has disconnected (Connection reset).");
                    Server.removePlayer(player);
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error: " + e.getMessage());
                    break;
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
        Server.roomPlayers.get(newRoomID).add(player.getName());
    
        sendMessage("RoomCreated: " + newRoomID);
        Server.sendClientMessageToAll(player.getName() + " has created room " + newRoomID);
        Server.sendJoinRoom(newRoomID, player.getId(), player.getName()); // ส่งพารามิเตอร์ครบ
    }
    
    
    
    public boolean joinRoom(int roomNumber) {
        player.setRoomID(roomNumber);
        
        // Update player list in the room
        Server.roomPlayers.putIfAbsent(roomNumber, new ArrayList<>());
        Server.roomPlayers.get(roomNumber).add(player.getName());
        
        sendMessage("RoomExists: " + roomNumber);
        Server.sendClientMessage(roomNumber, player.getName() + " has joined the room " + roomNumber);
        
        Server.sendJoinRoom(roomNumber, player.getId(), player.getName()); // ส่งพารามิเตอร์ครบ
        
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
        Server.sendClientMessage(roomID, oldName + " has changed their name to " + newName);
        
        // แจ้งผู้เล่นเอง
        sendMessage("Your name has been changed to " + newName);
    }
    
    public Player getPlayer()
    {
        return this.player;
    } 
}
