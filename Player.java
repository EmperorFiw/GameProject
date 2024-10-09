import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int id;
    private int roomID;
    private boolean isOwner;


    public Player(String name, int id, int roomID) {
        this.name = name;
        this.id = id;
        this.roomID = roomID;
    }

    public Player(String name, int id, int roomID, boolean isOwner) {
        this.name = name;
        this.id = id;
        this.roomID = roomID;
        this.isOwner = isOwner;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public boolean isOwner()
    {
        return isOwner;
    }

    public void setOwner(boolean owner)
    {
        this.isOwner = owner;
    }
}
