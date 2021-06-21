import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class Player {
    ImageView img;
    Player() {
        img = new ImageView(new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/player.png"))));
        img.setX(380);
        img.setY(565);
        img.setFitWidth(40);
        img.setFitHeight(30);
    }

    public void move (double dx) {
        img.setX(img.getX() + dx);
    }

    public void reset() {
        img.setX(380);
        img.setY(565);
    }

    public double getX() { return img.getX(); }
    public double getY() { return img.getY(); }
}
