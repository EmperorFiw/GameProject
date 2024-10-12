import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int id;
    private int roomID;
    private boolean isOwner = false;
    private String[] allPlayerInRoom = {"Empty", "Empty", "Empty", "Empty"}; // แก้ไขให้เป็น String[]


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

    public void setId(int id)
    {
        this.id = id;
    }

    public String getPlayerInRoomFromIndex(int index)
    {
        return this.allPlayerInRoom[index];
    }

   public void addInNameRoom(int index, String name)
    {
        if (index < 4)
            this.allPlayerInRoom[index] = name;
        else
            this.allPlayerInRoom[index-4] = name;
    } 
    @Override
    public String toString() {
        return "Player{name='" + name + "', id=" + id + ", roomID=" + roomID + ", isOwner=" + isOwner + "}";
    }

}
