import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

public class Ragdoll extends Application{

    void init_robin (Image i_head, Image i_body, Image i_lua, Image i_lla, Image i_lh,
                     Image i_rua, Image i_rla, Image i_rh,
                     Image i_lul, Image i_lll, Image i_lf,
                     Image i_rul, Image i_rll, Image i_rf,
                     Pane robin_pane) {
        BodyPart body = new BodyPart(i_body, 425, 400, "body");
        BodyPart head = new BodyPart(i_head, 410, 240, "head");
        BodyPart lua = new BodyPart(i_lua, 405, 410, "left upper arm");
        BodyPart lla = new BodyPart(i_lla, 354, 488, "left lower arm");
        BodyPart lh = new BodyPart(i_lh, 280, 487, "left hand");

        BodyPart rua = new BodyPart(i_rua, 545, 420, "right upper arm");
        BodyPart rla = new BodyPart(i_rla, 560, 533, "right lower arm");
        BodyPart rh = new BodyPart(i_rh, 556, 605, "right hand");

        BodyPart lul = new BodyPart(i_lul, 405, 593, "left upper leg");
        BodyPart lll = new BodyPart(i_lll, 430, 723, "left lower leg");
        BodyPart lf = new BodyPart(i_lf, 400, 790, "left foot");

         BodyPart rul = new BodyPart(i_rul, 490, 608, "right upper leg");
        BodyPart rll = new BodyPart(i_rll, 492, 725, "right lower leg");
        BodyPart rf = new BodyPart(i_rf, 500, 790, "right foot");

        robin_pane.getChildren().add(head.get_img());
        robin_pane.getChildren().add(body.get_img());

        // left arm
        robin_pane.getChildren().add(lua.get_img());
        robin_pane.getChildren().add(lla.get_img());
        robin_pane.getChildren().add(lh.get_img());
        // right arm
        robin_pane.getChildren().add(rua.get_img());
        robin_pane.getChildren().add(rla.get_img());
        robin_pane.getChildren().add(rh.get_img());
        // right leg
        robin_pane.getChildren().add(rul.get_img());
        robin_pane.getChildren().add(rll.get_img());
        robin_pane.getChildren().add(rf.get_img());
        // left leg
        robin_pane.getChildren().add(lul.get_img());
        robin_pane.getChildren().add(lll.get_img());
        robin_pane.getChildren().add(lf.get_img());

        body.add_children(head);
        body.add_children(lua);
        body.add_children(rua);
        body.add_children(lul);
        body.add_children(rul);

        lua.add_children(lla);
        lla.add_children(lh);
        rua.add_children(rla);
        rla.add_children(rh);
        lul.add_children(lll);
        lll.add_children(lf);
        rul.add_children(rll);
        rll.add_children(rf);
    }

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);
        stage.setTitle("Ragdoll");

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



        Pane robin_pane = new Pane();

        init_robin(robin_head, robin_body, robin_lua, robin_lla, robin_lh,
                robin_rua, robin_rla, robin_rh,
                robin_lul, robin_lll, robin_lf,
                robin_rul, robin_rll, robin_rf,
                robin_pane);

        Scene robin_scene = new Scene(robin_pane, 1024, 1280);
        stage.setScene(robin_scene);
        stage.show();
    }
}