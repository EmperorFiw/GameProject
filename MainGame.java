
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientManager client = new ClientManager();
            client.connectToServer();
            Menu menu = new Menu();
            menu.showMainMenu(true, client); // ส่ง client ไปยัง Menu

        });
    }

}

class ClientManager {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    JFrame frame = new JFrame();

    public ObjectOutputStream getOutputStream() {
        return out;
    }

    // Setter สำหรับ ObjectOutputStream
    public void setOutputStream(ObjectOutputStream out) {
        this.out = out;
    }

    // Getter สำหรับ ObjectInputStream
    public ObjectInputStream getInputStream() {
        return in;
    }

    // Setter สำหรับ ObjectInputStream
    public void setInputStream(ObjectInputStream in) {
        this.in = in;
    }

    // Getter สำหรับ Socket
    public Socket getSocket() {
        return socket;
    }

    // Setter สำหรับ Socket
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void connectToServer() {
        try {
            socket = new Socket("localhost", 7777);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());


            new Thread(() -> {
                try {
                    Object messageFromServer;
                    while ((messageFromServer = in.readObject()) != null) {
                        if (messageFromServer instanceof String) {
                            String message = (String) messageFromServer;
                            System.out.println(message);
                            if ("RoomExists".equals(message)) {
                                String numberPart = message.replaceAll("[^0-9]", "");
                                sendJoinRoom(numberPart);
                                System.out.println("Joining room " + numberPart);
                            } else if ("RoomNotExists".equals(message)) {
                                System.out.println("Room not found");
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(frame, "Room ID not found");
                                });
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

    private void sendJoinRoom(String roomNumber) {
        PlayerAction action = new PlayerAction(PlayerAction.ActionType.JOIN_ROOM, Integer.parseInt(roomNumber));
        sendAction(action);
    }

    public void changeName(String name, String type) {
        PlayerAction action = new PlayerAction(PlayerAction.ActionType.SET_NAME, name);
        sendAction(action);
        if ("join".equals(type))
        {
            joinRoom();
        }
        else if ("create".equals(type))
        {
            PlayerAction create = new PlayerAction(PlayerAction.ActionType.CREATE_ROOM, name);
            sendAction(create);
        }
    }
 
    private void joinRoom() {
        String roomNumber = JOptionPane.showInputDialog(frame, "Enter room number:");
        if (roomNumber != null && !roomNumber.isEmpty()) {
            sendJoinRoom(roomNumber);
        }
    }

    public void sendAction(PlayerAction action) {
        if (out != null) {
            try {
                out.writeObject(action);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Output stream is null, cannot send action.");
        }
    }
    
}
