import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SpaceInvaders extends Application {

    static Scene intro_page (Image logo_img) {
        Pane intro_elements = new Pane();
        intro_elements.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        ImageView logo = new ImageView(logo_img);
        logo.setX(150);
        logo.setY(30);

        Text instructions_title = new Text ("Instructions");
        instructions_title.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        instructions_title.setX(250);
        instructions_title.setY(330);

        Text instructions = new Text ("ENTER - Start Game\n A or \u25c0, D or \u25b6 - Move ship left or right\n" +
                "SPACE - Fire!\nQ - Quit Game\n1 or 2 or 3 - Start game at a specific level");
        instructions.setFont(Font.font("Verdana", 16));
        instructions.setTextAlignment(TextAlignment.CENTER );
        instructions.setX(220);
        instructions.setY(400);

        Text name = new Text ("Implemented by Lily Zhang for CS349. University of Waterloo, S21");
        name.setFont(Font.font("Verdana", 10));
        //instructions.setTextAlignment(TextAlignment.CENTER);
        name.setX(230);
        name.setY(575);

        // add children
        intro_elements.getChildren().add(logo);
        intro_elements.getChildren().add(instructions_title);
        intro_elements.getChildren().add(instructions);
        intro_elements.getChildren().add(name);

        Scene intro_scene = new Scene(intro_elements, 800, 600);
        return intro_scene;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle ("Space Invaders");
        stage.setResizable(false);


        Image logo_img = new Image (getClass().getResourceAsStream("images/logo.png"));
        Scene intro_scene = intro_page(logo_img);
        stage.setScene(intro_scene);

        Gameplay g = new Gameplay();
        intro_scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DIGIT1 || event.getCode() == KeyCode.ENTER) {
                g.start_game(stage, 1, intro_scene);
            } else if (event.getCode() == KeyCode.DIGIT2) {
                g.start_game(stage, 2, intro_scene);
            } else if (event.getCode() == KeyCode.DIGIT3) {
                g.start_game(stage, 3, intro_scene);
            } else if (event.getCode() == KeyCode.Q) {
                System.exit(0);
            }
        });
        stage.show();
    }
}
