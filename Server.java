
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Server {
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static Set<PlayerSerializable> players = Collections.synchronizedSet(new HashSet<>());
    private static Queue<Integer> freeIDs = new LinkedList<>(); // คิวเพื่อเก็บไอดีที่ว่าง
    private static int nextID = 0; // ตัวนับเพื่อให้ไอดีใหม่

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
    
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PlayerSerializable player;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private RoomManager rm = new RoomManager();

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
            while ((clientMessage = in.readObject()) != null) {
                if (clientMessage instanceof PlayerAction) {
                    handlePlayerAction((PlayerAction) clientMessage);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            Server.sendClientMessage(player.getRoomID(), player.getName() + " has left the room.");
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
                sendMessage("Player name set to: " + action.getName());
                break;
            case JOIN_ROOM:
                int roomNumber = action.getRoomID();
                if (!rm.IsRoomExists(roomNumber)) {
                    sendMessage("RoomNotExists");
                } else {
                    player.setRoomID(roomNumber);
                    sendMessage("RoomExists");
                    //if (player.getRoomID() == )
                    Server.sendClientMessage(roomNumber, player.getName() + " has joined the room " + roomNumber);
                }
                break;
            case CREATE_ROOM:
                int newRoomID = rm.setRoom();
                player.setRoomID(newRoomID);
                sendMessage("RoomCreated: " + newRoomID);
                Server.sendClientMessageToAll(player.getName() + " has created room " + newRoomID);
                break;
            case GET_NAME:
                sendMessage(player.getName());
                break;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
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


class RoomManager {
    private static Set<Integer> existingRoomIDs = new HashSet<>();

    public int setRoom() {
        int roomID;
        do {
            roomID = (int) (Math.random() * 900000) + 100000;
        } while (existingRoomIDs.contains(roomID));

        existingRoomIDs.add(roomID);
        System.out.println("Room ID: " + roomID);
        return roomID;
    }

    public boolean IsRoomExists(int id) {
        return existingRoomIDs.contains(id);
    }
}

//เอาไว้เก็บการทำงานจากฝั่ง client
class PlayerAction implements Serializable {
    public enum ActionType { SET_NAME, JOIN_ROOM, CREATE_ROOM, GET_NAME}

    private ActionType actionType;
    private String name;
    private int roomID;

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
