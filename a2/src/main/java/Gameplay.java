import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;

public class Gameplay {
    enum DIR {LEFT, RIGHT, NONE}

    // top row stats
    private static int SCORE = 0;
    private static int LIVES = 3;
    private static int LEVEL = 1;
    private static Text score = new Text();
    private static Text lives = new Text();
    private static Text level = new Text();

    // gameplay elements
    private static Scene game_scene;
    private static ArrayList<Enemy> enemies;
    private static ArrayList<EnemyBullet> e_bullets; // max of 4 bullets
    private static ArrayList<PlayerBullet> p_bullets;  // max of 3 bullets
    private static DIR alien_move = DIR.RIGHT;
    private static Player player;
    private static DIR player_dir;
    private static Boolean fire = false;

    // gameplay stats
    private static Boolean game_over = false;
    private static Boolean game_won = false;
    private static int enemies_killed = 0;
    private static Boolean respawn = false;
    private static final int ENEMIES_COUNT = 50;
    private static double PLAYER_SPEED = 1.0;
    private static final int MAX_ENEMY_BULLET = 4;
    private static final int MAX_PLAYER_BULLET = 4;
    private static int cur_bullets = 0; // keep track of all active enemy bullets
    private static final int ENEMY_FIRE_RATE = 1000;
    private static double ENEMY_BULLET_SPEED = 0.4;
    private static double PLAYER_BULLET_SPEED = 0.4;
    private static final int PLAYER_FIRE_RATE = 150;
    private static int player_cur_bullets = 0; // keep track of active player bullets

    static double ENEMY_SPEED = 0.3; // initial = 0.3, + 0.05 when on enemy is killed

    // dimensions
    private static final double SCREEN_WIDTH = 800;
    private static final double SCREEN_HEIGHT = 600;
    private static final double ENEMY_WIDTH = 40;
    private static final double ENEMY_HEIGHT= 30;
    private static final double BULLET_HEIGHT = 20;
    private static final double ENEMY_BULLET_WIDTH = 8;
    private static final double PLAYER_BULLET_WIDTH = 6;
    private static final double PLAYER_WIDTH = 40;
    private static final double PLAYER_HEIGHT = 30;
    private static final double ROW_HEIGHT = 15;


    public static void init_top_row_text () {
        score.setText("Score: " + SCORE);
        score.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        score.setFill(Color.WHITE);
        score.setX(30);
        score.setY(30);

        lives.setText ("Lives: " + LIVES);
        lives.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        lives.setFill(Color.WHITE);
        lives.setX(590);
        lives.setY(30);

        level.setText ("Level: " + LEVEL);
        level.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        level.setFill(Color.WHITE);
        level.setX(695);
        level.setY(30);
    }

    public static void init_enemies () {
        enemies = new ArrayList<Enemy>();
        int x = 155, y = 90;
        // top layer of green enemies
        for (int i = 0; i < 10; i++) {
            enemies.add(new Enemy(3, x, y));
            x += 50;
        }
        x = 155;
        y += 40;
        // middle 2 layers of blue enemies
        for (int i = 0; i < 20; i++) {
            if (i == 10) {
                y += 40;
                x = 155;
            }
            enemies.add(new Enemy(2, x, y));
            x += 50;
        }
        x = 155;
        y += 40;
        // bottom 2 layers of purple enemies
        for (int i = 0; i <20; i++) {
            if (i == 10) {
                y += 40;
                x = 155;
            }
            enemies.add(new Enemy(1, x, y));
            x += 50;
        }
    }

    public static void init_bullets() {
        e_bullets = new ArrayList<EnemyBullet>();
        for (int i = 0; i < MAX_ENEMY_BULLET; i++) { // max 4 bullets
            e_bullets.add(new EnemyBullet());
        }
        p_bullets = new ArrayList<PlayerBullet>();
        for (int i = 0; i < MAX_PLAYER_BULLET; i++) {
            p_bullets.add(new PlayerBullet());
        }
    }

    public static void init_gameplay_scene (ArrayList<Enemy> enemies) {
        Pane gameplay_elements = new Pane();
        // add enemies
        for (int i = 0; i < ENEMIES_COUNT; i++) {
            gameplay_elements.getChildren().add(enemies.get(i).img);
        }
        // add enemy bullets
        for (int i = 0; i < MAX_ENEMY_BULLET; i++) {
            gameplay_elements.getChildren().add(e_bullets.get(i).img);
        }
        // add score, lives, and level
        gameplay_elements.getChildren().add(score);
        gameplay_elements.getChildren().add(lives);
        gameplay_elements.getChildren().add(level);
        gameplay_elements.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        // add player
        player = new Player();
        gameplay_elements.getChildren().add(player.img);

        // add player bullets
        for (int i = 0; i < MAX_PLAYER_BULLET; i++) {
            gameplay_elements.getChildren().add(p_bullets.get(i).img);
        }

        game_scene = new Scene(gameplay_elements, 800, 600);
    }

    public static void move_enemy () {
        // check for game over condition
        // last alien shows bottom row
        if (enemies.get(0).getY() >= SCREEN_HEIGHT - ENEMY_HEIGHT - PLAYER_HEIGHT) {
            game_over = true;
            return;
        }
        // 9th element is the right most alien
        if (alien_move == DIR.RIGHT) {
            if (enemies.get(9).getX() <= SCREEN_WIDTH - ENEMY_WIDTH) { // keep moving to the left
                for (int i = 0; i < ENEMIES_COUNT; i++) {
                    enemies.get(i).move(ENEMY_SPEED, 0);
                }
            } else { // hit the right wall
                // move down
                for (int i = 0; i < ENEMIES_COUNT; i++) {
                    enemies.get(i).move(0, ROW_HEIGHT);
                }
                // switch direction
                alien_move = DIR.LEFT;
            }
        } else { // aliens moving to the left
            // 0th element is the left most alien
            if (enemies.get(0).getX() >= 0) { // keep moving to the left
                for (int i = 0; i < ENEMIES_COUNT; i++) {
                    enemies.get(i).move(-ENEMY_SPEED, 0);
                }
            } else { // hit the left wall
                // move down
                for (int i = 0; i < ENEMIES_COUNT; i++) {
                    enemies.get(i).move(0, ROW_HEIGHT);
                }
                // switch direction
                alien_move = DIR.RIGHT;
            }
        }
    }

    public static void move_player() {
        if (player_dir == DIR.NONE) { return; }
        double cur_x = player.getX();
        if(player_dir == DIR.RIGHT) {
            player.move(Math.min(SCREEN_WIDTH - PLAYER_WIDTH - cur_x, PLAYER_SPEED));
        } else if (player_dir == DIR.LEFT) {
            player.move(-Math.min(cur_x, PLAYER_SPEED));
        }
    }

    public static void move_bullets() {
        EnemyBullet cur;
        for (int i = 0; i < MAX_ENEMY_BULLET; i++) {
            cur = e_bullets.get(i);
            if (cur.in_use) {
                if (cur.getY() + ENEMY_BULLET_SPEED > SCREEN_HEIGHT - BULLET_HEIGHT) {
                    cur.destroy_bullet();
                    cur_bullets--;
                } else {
                    cur.move_bullet(ENEMY_BULLET_SPEED);
                }
            }
        }
        PlayerBullet p_cur;
        for (int i = 0; i < MAX_PLAYER_BULLET; i++) {
            p_cur = p_bullets.get(i);
            if (p_cur.in_use) {
                if (p_cur.getY() - PLAYER_BULLET_SPEED <= 0) {
                    p_cur.destroy_bullet();
                    player_cur_bullets--;
                } else {
                    p_cur.move_bullet(PLAYER_BULLET_SPEED);
                }
            }
        }
    }

    public static void check_enemy_bullet_collision() {
        EnemyBullet cur;
        Boolean hit = false;
        for (int i = 0; i < MAX_ENEMY_BULLET; i++) {
            cur = e_bullets.get(i);
            // contains bottom left or bottom right point
            if (cur.in_use && (player.img.contains(cur.getX(), cur.getY() + BULLET_HEIGHT)||
                            player.img.contains(cur.getX() + ENEMY_BULLET_WIDTH, cur.getY() + BULLET_HEIGHT))) {
                hit = true;
                // remove current bullet
                e_bullets.get(i).destroy_bullet();
                break;
            }
        }
        if (hit) {
            LIVES--;
            if (LIVES < 0) {
                game_over = true;
            } else {
                respawn = true;
            }
        }
    }

    public static void check_player_bullet_collision() {
        PlayerBullet cur;
        for (int i = 0; i < MAX_PLAYER_BULLET; i++) {
            cur = p_bullets.get(i);
            if (cur.in_use) {
                // check collision with each enemy starting from the bottom
                for (int j = ENEMIES_COUNT - 1; j >= 0; j--) {
                    if (!enemies.get(j).killed && (enemies.get(j).img.contains(cur.getX(), cur.getY()) ||
                            enemies.get(j).img.contains(cur.getX() + PLAYER_BULLET_WIDTH, cur.getY()))) {
                        cur.destroy_bullet();
                        player_cur_bullets--;
                        enemies.get(j).setKilled();
                        enemies_killed++;
                        if (enemies_killed == ENEMIES_COUNT) {
                            game_won = true;
                        }
                    }
                }
            }
        }
    }

    public static void update_stats() {
        score.setText("Score: " + SCORE);
        lives.setText ("Lives: " + LIVES);
    }

    public static void respawn() {
        if (LIVES == 0) {
            game_over = true;
        }
        player.reset();
        // find a spot with no bullets;
        Boolean moved = false;
        EnemyBullet cur;
        while (moved) {
            moved = false;
            for (int i = 0; i < MAX_ENEMY_BULLET; i++) {
                cur = e_bullets.get(i);
                if (cur.in_use && player.img.contains(cur.getX(), cur.getY())) {
                    player.move(8); // since bullet width is 8
                    moved = true;
                    break;
                }
            }
        }
        respawn = false;
    }

    // set bullet if there there are less than 4 bullets on screen
    public static void set_bullet (Enemy e) {
        if (e.killed) { return; }
        if (cur_bullets < 4) {
            for (int i = 0; i < MAX_ENEMY_BULLET; i++) {
                if (!e_bullets.get(i).in_use) {
                    e_bullets.get(i).init_bullet(e);
                    break;
                }
            }
            cur_bullets++;
        }
    }

    // player fires bullet
    public static void fire_bullet() {
        if (player_cur_bullets < MAX_PLAYER_BULLET) {
            for (int i = 0; i < MAX_PLAYER_BULLET; i++) {
                if (!p_bullets.get(i).in_use) {
                    p_bullets.get(i).init_bullet(player);
                    player_cur_bullets++;
                    fire = false; // set back to false after firing
                    break;
                }
            }
        }
    }

    public static void start_game (Stage stage, int start_level) {
        LEVEL = start_level;
        LIVES = 3;
        SCORE = 0;
        init_top_row_text();
        init_enemies();
        init_bullets();
        init_gameplay_scene(enemies);

        // ADD: SET SPEED BASED ON LEVEL

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle (long now) {
                if (game_over) {
                    this.stop();
                    return;
                } else if (game_won) {
                    System.out.println("game won!");
                    this.stop();
                    return;
                }
                if (respawn) {
                    respawn(); // respawn() will set respawn = false
                } else {
                    move_enemy();
                    move_player();
                    move_bullets();
                    check_enemy_bullet_collision();
                    check_player_bullet_collision();
                    update_stats();
                }
            }
        };

        // set input for player movement
        game_scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    player_dir = DIR.LEFT;
                    break;
                case RIGHT:
                    player_dir = DIR.RIGHT;
                    break;
                case SPACE:
                    fire = true;
                    break;
                case Q: // Q to quit the game
                    System.exit(0);
            }
        });

        game_scene.setOnKeyReleased(event -> {
            player_dir = DIR.NONE;
            fire = false;
        });
        // timer for spawning enemy bullets
        Timer t = new Timer();
        t.schedule (new TimerTask() {
            @Override
            public void run() {
                // randomly choose an enemy
                Random rand = new Random();
                int index = rand.nextInt(50);
                set_bullet (enemies.get(index));
            }
        }, 0, ENEMY_FIRE_RATE);

        // timer for player fire
        Timer player_t = new Timer();
        player_t.schedule (new TimerTask() {
            @Override
            public void run() {
                if (fire) {
                    fire_bullet();
                }
            }
        }, 0, PLAYER_FIRE_RATE);

        //enemies.get(0).setKilled();
        timer.start();
        stage.setScene(game_scene);
        stage.show();
    }
}
