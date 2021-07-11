import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;

public class Ragdoll extends Application{
    final int screen_width = 1024;
    final int screen_height = 1280;
    double prev_x, prev_y;
    BodyPart root = null;
    Canvas canvas;
    BodyPart selected = null;

    BodyPart init_robin (Image i_head, Image i_body, Image i_lua, Image i_lla, Image i_lh,
                     Image i_rua, Image i_rla, Image i_rh,
                     Image i_lul, Image i_lll, Image i_lf,
                     Image i_rul, Image i_rll, Image i_rf
                     //Pane robin_pane
                     ) {
       /* BodyPart body = new BodyPart(i_body, 425, 400, BodyPart.PART.BODY, "body");
        BodyPart head = new BodyPart(i_head, 410, 240, BodyPart.PART.HEAD,  "head");

        BodyPart lua = new BodyPart(i_lua, 405, 410, BodyPart.PART.HEAD,  "left upper arm");
        BodyPart lla = new BodyPart(i_lla, 354, 488, BodyPart.PART.HEAD,  "left lower arm");
        BodyPart lh = new BodyPart(i_lh, 280, 487, BodyPart.PART.HEAD,  "left hand");

        BodyPart rua = new BodyPart(i_rua, 405, 420, BodyPart.PART.HEAD,  "right upper arm");
        BodyPart rla = new BodyPart(i_rla, 390, 533, BodyPart.PART.HEAD,  "right lower arm");
        BodyPart rh = new BodyPart(i_rh, 396, 605, BodyPart.PART.HEAD,  "right hand");

        BodyPart lul = new BodyPart(i_lul, 405, 593, BodyPart.PART.HEAD,  "left upper leg");
        BodyPart lll = new BodyPart(i_lll, 430, 723, BodyPart.PART.HEAD,  "left lower leg");
        BodyPart lf = new BodyPart(i_lf, 400, 790, BodyPart.PART.HEAD,  "left foot");

        BodyPart rul = new BodyPart(i_rul, 490, 608, BodyPart.PART.HEAD,  "right upper leg");
        BodyPart rll = new BodyPart(i_rll, 492, 725, BodyPart.PART.HEAD,  "right lower leg");
        BodyPart rf = new BodyPart(i_rf, 500, 790, BodyPart.PART.HEAD,  "right foot");
        */
        BodyPart body = new BodyPart(i_body, 425, 400, BodyPart.PART.BODY, "body");
        BodyPart head = new BodyPart(i_head, -15, -160, BodyPart.PART.HEAD,  "head", 87, 162);

        BodyPart lua = new BodyPart(i_lua, -15, 30, BodyPart.PART.UPPER_ARM,  "left upper arm", 34, 0);
        BodyPart lla = new BodyPart(i_lla, -3, 113, BodyPart.PART.LOWER_ARM,  "left lower arm", 10, 0);
        BodyPart lh = new BodyPart(i_lh, -10, 67, BodyPart.PART.HAND,  "left hand", 19, 0);

        BodyPart rua = new BodyPart(i_rua, 120, 20, BodyPart.PART.UPPER_ARM,  "right upper arm");
        BodyPart rla = new BodyPart(i_rla, 15, 113, BodyPart.PART.LOWER_ARM,  "right lower arm", 10, 0);
        BodyPart rh = new BodyPart(i_rh, -4, 67, BodyPart.PART.HAND,  "right hand", 19, 0);

        BodyPart lul = new BodyPart(i_lul, -20, 193, BodyPart.PART.UPPER_LEG,  "left upper leg", 46, 0);
        BodyPart lll = new BodyPart(i_lll, 25, 130, BodyPart.PART.LOWER_LEG,  "left lower leg", 24, 0);
        BodyPart lf = new BodyPart(i_lf, -30, 67, BodyPart.PART.FOOT,  "left foot", 24, 0);

        BodyPart rul = new BodyPart(i_rul, 65, 208, BodyPart.PART.UPPER_LEG,  "right upper leg", 35, 0);
        BodyPart rll = new BodyPart(i_rll, 2, 117, BodyPart.PART.LOWER_LEG,  "right lower leg", 26, 0);
        BodyPart rf = new BodyPart(i_rf, 8, 65, BodyPart.PART.FOOT,  "right foot", 25, 0);


        body.add_children(rua);
        body.add_children(lua);
        body.add_children(rul);
        body.add_children(lul);


        body.add_children(head);

        lua.add_children(lla);
        lla.add_children(lh);
        rua.add_children(rla);
        rla.add_children(rh);
        lul.add_children(lll);
        lll.add_children(lf);
        rul.add_children(rll);
        rll.add_children(rf);

        return body;
    }

    void init_robin_canvas_manip(Canvas canvas, BodyPart root) {
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            double x = event.getX();
            double y = event.getY();
            BodyPart hit = root.hitTest(x, y);
            if (hit != null) {
                selected = hit;
                //System.out.println("hit " + selected.name);
                prev_x = event.getX();
                prev_y = event.getY();
            }
        });

      /*  canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            double x = event.getX();
            double y = event.getY();
            BodyPart hit = root.hitTest(x, y);
            if (hit != null) {
                selected = hit;
                System.out.println("clicked on " + selected.name);
                prev_x = event.getX();
                prev_y = event.getY();
            }
        }); */

        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (selected != null) {
                double e_x = event.getX();
                double e_y  = event.getY();
                double dx = e_x - prev_x;
                double dy = e_y - prev_y;
                double distance = Math.sqrt (Math.pow(e_x - prev_x, 2) + Math.pow(e_y - prev_y, 2));
                BodyPart.ROTATE_DIR x_dir, y_dir;
                if (prev_x > e_x) {
                    x_dir = BodyPart.ROTATE_DIR.RIGHT;
                } else {
                    x_dir = BodyPart.ROTATE_DIR.LEFT;
                }

                if (prev_y > e_y) {
                    y_dir = BodyPart.ROTATE_DIR.UP;
                } else {
                    y_dir = BodyPart.ROTATE_DIR.DOWN;
                }

                double theta = Math.atan(distance);
                selected.process_move(dx, dy, theta, x_dir, y_dir);
            }

            draw (canvas, root); // draw in new position
            prev_x = event.getX();
            prev_y = event.getY();
        });

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            selected = null;
        });

    }

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);
        stage.setTitle("Ragdoll");

        ToggleButton reset = new ToggleButton("Reset");
        reset.setOnAction(event -> {
            if (root != null) {
                root.reset();
                draw (canvas, root); // draw in new position
            }
        });

        ToggleButton quit = new ToggleButton("Quit");
        quit.setOnAction(event -> {
            System.exit(0);
        });

        Image robin_body = new Image (getClass().getResourceAsStream("Christopher Robin/body.png"));
        Image robin_head = new Image (getClass().getResourceAsStream("Christopher Robin/head.png"));
        Image robin_lua = new Image (getClass().getResourceAsStream("Christopher Robin/left upper arm.png"));
        Image robin_lla = new Image (getClass().getResourceAsStream("Christopher Robin/left lower arm.png"));
        Image robin_lh = new Image (getClass().getResourceAsStream("Christopher Robin/left hand.png"));
        Image robin_rua = new Image (getClass().getResourceAsStream("Christopher Robin/right upper arm.png"));
        Image robin_rla = new Image (getClass().getResourceAsStream("Christopher Robin/right lower arm.png"));
        Image robin_rh = new Image (getClass().getResourceAsStream("Christopher Robin/right hand.png"));
        Image robin_lul = new Image (getClass().getResourceAsStream("Christopher Robin/left upper leg.png"));
        Image robin_lll = new Image (getClass().getResourceAsStream("Christopher Robin/left lower leg.png"));
        Image robin_lf = new Image (getClass().getResourceAsStream("Christopher Robin/left foot.png"));
        Image robin_rul = new Image (getClass().getResourceAsStream("Christopher Robin/right upper leg.png"));
        Image robin_rll = new Image (getClass().getResourceAsStream("Christopher Robin/right lower leg.png"));
        Image robin_rf = new Image (getClass().getResourceAsStream("Christopher Robin/right foot.png"));

        BodyPart robin_root = init_robin(robin_head, robin_body, robin_lua, robin_lla, robin_lh,
                robin_rua, robin_rla, robin_rh,
                robin_lul, robin_lll, robin_lf,
                robin_rul, robin_rll, robin_rf);
        root = robin_root;

        Canvas robin_canvas = new Canvas(screen_width, screen_height);
        canvas = robin_canvas;

        init_robin_canvas_manip(robin_canvas, robin_root);
        Scene scene = new Scene (new VBox(new HBox(quit, reset), robin_canvas));

        draw(robin_canvas, robin_root);

        stage.setScene(scene);
        stage.show();
    }

    private void draw(Canvas canvas, BodyPart root) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        root.draw(gc);
    }

}