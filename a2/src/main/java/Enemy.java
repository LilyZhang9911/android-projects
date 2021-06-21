import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Objects;

public class Enemy {
    ImageView img;
    Boolean killed;
    int type;
    Enemy(int type, int x, int y) {
        if (type == 1) { // purple
            img = new ImageView(new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/enemy1.png"))));
        } else if (type == 2) { // blue
            img = new ImageView(new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/enemy2.png"))));
        } else { // green
            img = new ImageView(new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/enemy3.png"))));
        }
        img.setX(x);
        img.setY(y);
        img.setFitWidth(40);
        img.setFitHeight(30);
        killed = false;
        this.type = type;
    }
    public void move (double dx, double dy) {
        img.setX(img.getX() + dx);
        img.setY(img.getY() + dy);
    }
    public double getX() {
        return img.getX();
    }
    public double getY() {
        return img.getY();
    }
    public int getType() {return type; }
    public void setKilled() {
        killed = true;
        img.setVisible(false);
    }
}
