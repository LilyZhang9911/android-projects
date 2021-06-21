import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Objects;

public class PlayerBullet {
    ImageView img;
    Boolean in_use = false;

    PlayerBullet() {
        img = new ImageView(new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/player_bullet.png"))));
        img.setFitWidth(6);
        img.setFitHeight(20);
        img.setVisible(false);
    }

    public void init_bullet(Player p) {
        img.setX(p.getX()+20);
        img.setY(p.getY());
        img.setVisible(true);
        in_use = true;
    }

    public void destroy_bullet() {
        img.setVisible(false);
        in_use = false;
    }

    public void move_bullet(double dy) {
        img.setY(img.getY() - dy);
    }

    public double getX() { return img.getX(); }
    public double getY() { return img.getY(); }
}
