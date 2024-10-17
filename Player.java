import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int id;
    private int roomID;
    private boolean isOwner = false;
    private int playerHealth = 100;
    private int playerDamage = 20;
    private boolean isInGame = false;
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

    public int getCountPlayer()
    {
        int count =0;
        for (int i=0;i<this.allPlayerInRoom.length;i++)
        {
            if (!this.allPlayerInRoom[i].equals("Empty"))
            {
                count ++;
            }
        }
        return count;
    }

    public void addInNameRoom(int index, String name) {
        int actualIndex = index < 4 ? index : index - 4;
        
        if (this.allPlayerInRoom[actualIndex].equals("Empty")) {
            this.allPlayerInRoom[actualIndex] = name; 
        } else {
            this.allPlayerInRoom[actualIndex] = name;
        }
    }

    public void setPlayerHealth(int hp)
    {
        this.playerHealth = hp;
    }
    
    public int getPlayerHealth()
    {
        return this.playerHealth;
    }
    public void setInGame(boolean is)
    {
        this.isInGame = is;
    }
    public Boolean getInGame()
    {
        return this.isInGame;
    }

    @Override
    public String toString() {
        return "Player{name='" + name + "', id=" + id + ", roomID=" + roomID + ", isOwner=" + isOwner + "}";
    }

    public void setTarget(int id2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTarget'");
    }

}
