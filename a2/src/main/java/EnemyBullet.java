import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class EnemyBullet {
    ImageView img;
    Image type1 = new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/bullet1.png")));
    Image type2 = new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/bullet2.png")));
    Image type3 = new Image (Objects.requireNonNull(getClass().getResourceAsStream("images/bullet3.png")));
    Boolean in_use;
    EnemyBullet() {
        // set a place holder
        img = new ImageView(type1);
        img.setFitWidth(8);
        img.setFitHeight(20);
        img.setVisible(false);
        in_use = false;
    }
    public void init_bullet (Enemy e) {
        in_use = true;
        int type = e.getType();
        if (type == 1) {
            img.setImage(type1);
        } else if (type == 2) {
            img.setImage(type2);
        } else { // type 3
            img.setImage(type3);
        }
        // create bullet based on enemy location
        img.setX(e.getX() + 20); // enemies fire from the middle
        img.setY(e.getY() + 25);
        img.setVisible(true);
    }

    public void move_bullet (double dy) {
        img.setY(img.getY() + dy);
    }

    public double getX() { return img.getX(); }
    public double getY() { return img.getY(); }

    public void destroy_bullet() {
        in_use = false;
        img.setVisible(false);
    }
}
