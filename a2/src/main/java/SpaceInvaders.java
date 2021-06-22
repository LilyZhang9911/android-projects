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
import javafx.scene.shape.Rectangle;

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

    static Scene game_end (Image alien, Text score_data, Text game_end_type) {
        Pane game_over_elements = new Pane();
        game_over_elements.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        game_end_type.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        game_end_type.setFill(Color.WHITE);
        game_end_type.setX(260);
        game_end_type.setY(200);

        Text instructions = new Text ("ENTER - Start New Game\nI - Back to Instructions\nQ - Quit Game\n" +
                "1 or 2 or 3 - Start New Game at a specified level");
        instructions.setFont(Font.font("Verdana", 20));
        instructions.setTextAlignment(TextAlignment.CENTER );
        instructions.setFill(Color.WHITE);
        instructions.setX(160);
        instructions.setY(340);

        ImageView alien_icon = new ImageView(alien);
        alien_icon.setX(370);
        alien_icon.setY(550);
        alien_icon.setFitWidth(60);
        alien_icon.setFitHeight(40);

        score_data.setX(270);
        score_data.setY(260);
        score_data.setFill(Color.WHITE);
        score_data.setTextAlignment(TextAlignment.CENTER);
        score_data.setFont(Font.font("Verdana", FontWeight.BOLD, 25));

        // add children
        game_over_elements.getChildren().add(game_end_type);
        game_over_elements.getChildren().add(instructions);
        game_over_elements.getChildren().add(alien_icon);
        game_over_elements.getChildren().add(score_data);

        Scene game_over_scene = new Scene(game_over_elements, 800, 600);
        return game_over_scene;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle ("Space Invaders");
        stage.setResizable(false);


        Image logo_img = new Image (getClass().getResourceAsStream("images/logo.png"));
        Image alien = new Image (getClass().getResourceAsStream("images/enemy3.png"));
        Text score_data = new Text("");
        Scene intro_scene = intro_page(logo_img);
        Text game_end_type = new Text(""); // either "You win!" or "Game Over!"
        Scene game_end_scene = game_end(alien, score_data, game_end_type);

        //stage.setScene(game_over_scene);
        stage.setScene(intro_scene);

        Gameplay g = new Gameplay();
        intro_scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DIGIT1 || event.getCode() == KeyCode.ENTER) {
                g.start_game(stage, 1, intro_scene, game_end_scene, score_data, game_end_type);
            } else if (event.getCode() == KeyCode.DIGIT2) {
                g.start_game(stage, 2, intro_scene, game_end_scene, score_data, game_end_type);
            } else if (event.getCode() == KeyCode.DIGIT3) {
                g.start_game(stage, 3, intro_scene, game_end_scene, score_data, game_end_type);
            } else if (event.getCode() == KeyCode.Q) {
                System.exit(0);
            }
        });
        // we can access it afterwards System.out.println(g.highest_score);
        game_end_scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DIGIT1 || event.getCode() == KeyCode.ENTER) {
                g.start_game(stage, 1, intro_scene, game_end_scene, score_data, game_end_type);
            } else if (event.getCode() == KeyCode.DIGIT2) {
                g.start_game(stage, 2, intro_scene, game_end_scene, score_data, game_end_type);
            } else if (event.getCode() == KeyCode.DIGIT3) {
                g.start_game(stage, 3, intro_scene, game_end_scene, score_data, game_end_type);
            } else if (event.getCode() == KeyCode.Q) {
                System.exit(0);
            } else if (event.getCode() == KeyCode.I) {
                stage.setScene(intro_scene);
            }
        });

        stage.show();
    }
}
