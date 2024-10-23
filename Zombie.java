import java.io.Serializable;

public class Zombie implements Serializable {
    private int id;
    private int health;
    private int positionX;
    private int positionY;

    public Zombie(int id, int initialHealth, int positionX, int positionY) {
        this.id = id;
        this.health = initialHealth;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public int getId() {
        return id;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public boolean isDeath()
    {
        if (getHealth() <= 0)
            return true;

        return false;
    }

    public void setPosition(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }
}
