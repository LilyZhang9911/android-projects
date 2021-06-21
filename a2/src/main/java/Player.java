import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class Player {
    ImageView img;
    int initial_x, initial_y;
    Player() {
        img = new ImageView(new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/player.png"))));
        initial_x = 380;
        initial_y = 565;
        img.setX(initial_x);
        img.setY(initial_y);
        img.setFitWidth(40);
        img.setFitHeight(30);
    }

    public void move (double dx) {
        img.setX(img.getX() + dx);
    }

    public void reset() {
        img.setX(initial_x);
        img.setY(initial_y);
    }

    public double getX() { return img.getX(); }
    public double getY() { return img.getY(); }
}
