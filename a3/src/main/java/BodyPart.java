import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.util.Vector;


public class BodyPart extends ImageView {
    private ImageView img;
    private double initial_x, initial_y;
    private double prev_x, prev_y;
    enum STATE {NONE, SELECTED, MOVING}
    String name; // for debugging purposes
    STATE cur_state;
    protected Vector<BodyPart> children;
    public BodyPart(Image i, double x, double y, String name) {
        img = new ImageView(i);
        img.setX(x);
        img.setY(y);
        initial_x = x;
        initial_y = y;
        prev_x = -1;
        prev_y = -1;
        this.name = name;
        children = new Vector<BodyPart> ();

        img.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                prev_x = -1;
                prev_y = -1;
                System.out.println(name + " pressed");
                cur_state = STATE.SELECTED;
            }
        });

        img.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (prev_x == -1) prev_x = event.getX();
                if (prev_y == -1) prev_y = event.getY();

                double dx = event.getX() - prev_x;
                double dy = event.getY() - prev_y;

                System.out.println(name + " dragged");
                System.out.println(dx);
                System.out.println(dy);
                translate (dx, dy);
                cur_state = STATE.MOVING;
                // change for moving
                prev_x = event.getX();
                prev_y = event.getY();
            }
        });

        img.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                cur_state = STATE.NONE;
            }
        });
    }

    public void add_children (BodyPart c) {
        children.add(c);
    }

    public ImageView get_img () {
        return img;
    }

    public void translate (double dx, double dy) {
        img.setX(img.getX() + dx);
        img.setY(img.getY() + dy);
        // move children
        for (BodyPart bp: children) {
            bp.translate(dx, dy);
        }
    }

}
