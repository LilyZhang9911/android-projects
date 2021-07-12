import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

import java.util.Vector;


public class BodyPart extends ImageView {
    private static double HEAD_THETA_MAX = 50;
    private static double LOWER_ARM_THETA_LEFT_BOUND = 135;
    private static double LOWER_ARM_THETA_RIGHT_BOUND = 225;
    private static double HAND_THETA_LEFT_BOUND = 35; // also used for feet with 35 degree limit
    private static double HAND_THETA_RIGHT_BOUND = 325;
    private static double LEG_THETA_LEFT_BOUND =85;
    private static double LEG_THETA_RIGHT_BOUND = 265;

    private static double LEFT_UPPER_LEG_HEIGHT  = 132;
    private static double RIGHT_UPPER_LEG_HEIGHT  = 118;
    private static double LEFT_LOWER_LEG_HEIGHT  = 70;
    private static double RIGHT_LOWER_LEG_HEIGHT  = 78;

    // note upper arm has no limit
    private double rotation_pivot_x, rotation_pivot_y;
    private ImageView imgview;
    private Image img;
    private double initial_x, initial_y;
    enum PART {BODY, HEAD, UPPER_ARM, LOWER_ARM, HAND, UPPER_LEG, LOWER_LEG, FOOT}
    enum ROTATE_DIR {LEFT, RIGHT, UP, DOWN}
    String name; // for debugging purposes
    PART cur_part;
    private Affine matrix;
    private Affine translate_matrix;
    private Affine rotate_matrix;
    private Affine scale_matrix;
    BodyPart parent;
    protected Vector<BodyPart> children;
    double total_theta;
    double scale_factor;
    public BodyPart(Image i, double x, double y, PART part, String name) {
        imgview = new ImageView(i);
        img = i;
        initial_x = x;
        initial_y = y;
        cur_part = part;
        this.name = name;
        parent = null;
        children = new Vector<BodyPart> ();
        total_theta = 0;
        translate_matrix = new Affine();
        translate_matrix.prependTranslation(x, y);
        scale_matrix = new Affine();
        rotate_matrix = new Affine();
        matrix = new Affine();
        scale_factor = 1.0;

        if (part == PART.HEAD) {
            rotation_pivot_x = 0; // pivot is image size / 2 for head
            rotation_pivot_y = 0;
        }
    }

    public BodyPart(Image i, double x, double y, PART part, String name, double pivot_x, double pivot_y) {
        imgview = new ImageView(i);
        img = i;
        initial_x = x;
        initial_y = y;
        cur_part = part;
        this.name = name;
        parent = null;
        children = new Vector<BodyPart> ();
        total_theta = 0;
        translate_matrix = new Affine();
        translate_matrix.prependTranslation(x, y);
        rotate_matrix = new Affine();
        scale_matrix = new Affine();
        matrix = new Affine();
        rotation_pivot_x = pivot_x; // pivot is image size / 2 for head
        rotation_pivot_y = pivot_y;
        scale_factor = 1.0;
    }

    public void add_children (BodyPart c) {
        children.add(c);
        c.parent = this;
    }

    public void draw (GraphicsContext gc) {
        Affine oldMatrix = gc.getTransform();
        gc.setTransform(getFullMatrix());
        gc.drawImage(img, 0, 0);
        int children_count = children.size();
        for (int i = children_count - 1; i >= 0; i--) {
            children.get(i).draw(gc);
        }
        gc.setTransform(oldMatrix);
    }

    public Affine getFullMatrix() {
        Affine result = new Affine();
        // append all translations then rotations
        Affine translate = getFullTranslateMatrix();
        Affine scale = getFullScaleMatrix();
        Affine rotate = getFullRotateMatrix();

        result.append(rotate);
        result.append(translate);
        if (cur_part != PART.FOOT) { // feet do no scale
            result.append(scale);
        }
        return result;
    }

    public Affine getFullTranslateMatrix() {
        Affine fullMatrix = getLocalTranslateMatrix().clone();
        if (parent != null) {
            fullMatrix.prepend(parent.getFullTranslateMatrix());
        }
        return fullMatrix;
    }

    public Affine getLocalTranslateMatrix() {
        return translate_matrix;
    }

    private void update_rotate() {
        Affine fullMatrix = getFullTranslateMatrix();
        Affine inverse = new Affine();
        try {
            inverse = fullMatrix.createInverse();
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
        rotate_matrix.setToIdentity();
        rotate_matrix.prepend(inverse);
        rotate_matrix.prependTranslation(-rotation_pivot_x, -rotation_pivot_y); // use bottom centre of head as pivot
        rotate_matrix.prependRotation(total_theta);
        rotate_matrix.prependTranslation(rotation_pivot_x, rotation_pivot_y);
        rotate_matrix.prepend(fullMatrix);
        //update_matrix();
    }

    public Affine getFullRotateMatrix() {
        Affine fullMatrix = getLocalRotateMatrix().clone();
        if (parent != null) {
            fullMatrix.prepend(parent.getFullRotateMatrix());
        }
        return fullMatrix;
    }

    public Affine getLocalRotateMatrix() {
        update_rotate();
        return rotate_matrix;
    }

    private void update_scale() {
        scale_matrix.setToIdentity();
        scale_matrix.prependScale(1, scale_factor);
    }

    public Affine getLocalScaleMatrix() {
        update_scale();
        return scale_matrix;
    }
    public Affine getFullScaleMatrix() {
        Affine fullMatrix = getLocalScaleMatrix().clone();
        if (parent != null) {
            fullMatrix.prepend(parent.getFullScaleMatrix());
        }
        return fullMatrix;
    }


    protected BodyPart hitTest (double x, double y) {
        Point2D pointAtOrigin = new Point2D(0, 0);
        try {
            Point2D mouse_click = new Point2D(x, y);
            pointAtOrigin = getFullMatrix().createInverse().transform(mouse_click);

        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        BodyPart result;
        for (BodyPart bp: children) {
            result = bp.hitTest(x, y);
            if (result != null) return result;
        }
        if (imgview.contains(pointAtOrigin.getX(), pointAtOrigin.getY())) return this;
        return null;
    }

    // return value used to check when to stop scaling
    public void process_move (double dx, double dy, double theta, ROTATE_DIR x_dir, ROTATE_DIR y_dir) {
        if (cur_part == PART.BODY) {
            translate(dx, dy);
        } else if (cur_part == PART.HEAD) {
            rotate_head (theta, x_dir);
        } else if (cur_part == PART.UPPER_ARM || cur_part == PART.LOWER_ARM ||
                cur_part == PART.HAND || cur_part == PART.UPPER_LEG ||
                cur_part == PART.LOWER_LEG || cur_part == PART.FOOT) {
            rotate_arm_leg (theta, x_dir, y_dir);
        }
    }

    private void translate (double dx, double dy) {
        translate_matrix.prependTranslation(dx, dy);
    }

    // return false when it can no longer be scaled
    public Boolean scale (ROTATE_DIR dir) {
        if ((scale_factor >= 1.5 && dir == ROTATE_DIR.DOWN) ||
                (scale_factor <= 0.5 && dir == ROTATE_DIR.UP)) {
            return false;
        }
        if (cur_part == PART.UPPER_LEG) {
            double translate_height_lower_leg, translate_height_foot;
            if (name == "left upper leg") {
                translate_height_lower_leg = LEFT_UPPER_LEG_HEIGHT;
                translate_height_foot = LEFT_LOWER_LEG_HEIGHT;
            } else  {// name == "right upper leg"
                translate_height_lower_leg = RIGHT_UPPER_LEG_HEIGHT;
                translate_height_foot = RIGHT_LOWER_LEG_HEIGHT;
            }
            if (dir == ROTATE_DIR.UP) {
                scale_factor -= 0.01;
                children.get(0).translate(0, -translate_height_lower_leg * 0.01); // move legs
                children.get(0).children.get(0).translate(0, -translate_height_foot * 0.01); // move feet
            } else {
                scale_factor += 0.01;
                children.get(0).translate(0, translate_height_lower_leg * 0.01);
                children.get(0).children.get(0).translate(0, translate_height_foot * 0.01);
            }
        } else if (cur_part == PART.LOWER_LEG) {
            double translate_height_lower_leg;
            if (name == "left upper leg") {
                translate_height_lower_leg = LEFT_LOWER_LEG_HEIGHT;
            } else  {// name == "right upper leg"
                translate_height_lower_leg = RIGHT_LOWER_LEG_HEIGHT;
            }
            if (dir == ROTATE_DIR.UP) {
                scale_factor -= 0.01;
                children.get(0).translate(0, -translate_height_lower_leg * 0.01); // move legs
            } else {
                scale_factor += 0.01;
                children.get(0).translate(0, translate_height_lower_leg * 0.01);
            }
        }
        return true;
    }

    private void rotate_head (double theta, ROTATE_DIR dir) {
        double limit = 0;
        limit = HEAD_THETA_MAX;
        // determine direction
        if (dir == ROTATE_DIR.RIGHT) {
            theta = -theta;
        }
        double remaining; // remaining degrees to rotate by
        if (dir == ROTATE_DIR.RIGHT) {
            remaining = - limit - total_theta;
        } else {
            remaining = limit - total_theta;
        }
        if (dir == ROTATE_DIR.LEFT && theta > remaining) {
            total_theta = limit;
        } else if (dir == ROTATE_DIR.RIGHT && theta < remaining) {
            total_theta = -limit;
        } else {
            total_theta += theta;
        }
    }

    private void rotate_arm_leg (double theta, ROTATE_DIR x_dir, ROTATE_DIR y_dir) {
        double parent_theta = 0;
         if (cur_part == PART.LOWER_ARM) {
             parent_theta = parent.total_theta;
         } else if (cur_part == PART.HAND) {
             parent_theta = parent.total_theta + parent.parent.total_theta;
         } else if (cur_part == PART.LOWER_LEG) {
             parent_theta = parent.total_theta;
         } else if (cur_part == PART.FOOT) {
             parent_theta = parent.total_theta + parent.parent.total_theta;
         }

        theta = Math.abs(theta);
        int case_type = 0; // determine type of rotation
        ROTATE_DIR total_dir;
        if (x_dir == ROTATE_DIR.LEFT && y_dir == ROTATE_DIR.DOWN) {
            case_type = 1;
        } else if (x_dir == ROTATE_DIR.LEFT && y_dir == ROTATE_DIR.UP) {
            case_type = 2;
        } else if (x_dir == ROTATE_DIR.RIGHT && y_dir == ROTATE_DIR.DOWN) {
            case_type = 3;
        } else { // (x_dir == ROTATE_DIR.RIGHT && y_dir == ROTATE_DIR.UP)
            case_type = 4;
        }

        // set theta in range 0 to 360
        total_theta += 360;
        total_theta %= 360;

        double quadrant_theta = total_theta + parent_theta;
        quadrant_theta += 360;
        quadrant_theta %= 360;
    //    System.out.println("quadrant theta is " + quadrant_theta);

        if (quadrant_theta < 45) {
         //   System.out.println("45 type " + case_type);
            if (case_type == 1 || case_type == 2) {
                theta = -theta;
            }
        } else if (quadrant_theta < 90) {
           // System.out.println("90 type " + case_type);
            if (case_type == 1 || case_type == 3) {
                theta = -theta;
            }
        } else if (quadrant_theta < 135) {
          //  System.out.println("135 type " + case_type);
            if (case_type == 1 || case_type == 3) {
                theta = -theta;
            }
        } else if (quadrant_theta < 180) {
          //  System.out.println("180 type " + case_type);
            if (case_type == 3 || case_type == 4) {
                theta = -theta;
            }
        } else if (quadrant_theta < 225) {
       //     System.out.println("225 type " + case_type);
            if (case_type == 3 || case_type == 4) {
                theta = -theta;
            }
        }
        else if (quadrant_theta < 270) {
          //  System.out.println("270 type " + case_type);
            if (case_type == 2|| case_type == 3 || case_type == 4) {
                theta = -theta;
            }
        } else if (quadrant_theta < 315) {
          //  System.out.println("315 type " + case_type);
            if (case_type == 2 || case_type == 4) {
                theta = -theta;
            }
        } else { // quadrant_theta < 360
          //  System.out.println("360 type " + case_type);
            if (case_type == 1 || case_type == 2) {
                theta = -theta;
            }
        }
        total_theta += theta;
        // Note: upper arm has not rotation limit
        // adjustments for rotation limits below
        if (cur_part == PART.LOWER_ARM) {
            if (total_theta < 180 && total_theta > LOWER_ARM_THETA_LEFT_BOUND) {
                total_theta = LOWER_ARM_THETA_LEFT_BOUND;
            } else if (total_theta > 180 && total_theta < LOWER_ARM_THETA_RIGHT_BOUND) {
                total_theta = LOWER_ARM_THETA_RIGHT_BOUND;
            }
        } else if (cur_part == PART.HAND || cur_part == PART.FOOT) {
            if (total_theta < 180 && total_theta > HAND_THETA_LEFT_BOUND) {
                total_theta = HAND_THETA_LEFT_BOUND;
            } else if (total_theta > 180 && total_theta < HAND_THETA_RIGHT_BOUND) {
                total_theta = HAND_THETA_RIGHT_BOUND;
            }
        } else if (cur_part == PART.UPPER_LEG || cur_part == PART.LOWER_LEG) {
            if (total_theta < 180 && total_theta > LEG_THETA_LEFT_BOUND) {
                total_theta = LEG_THETA_LEFT_BOUND;
            } else if (total_theta > 180 && total_theta < LEG_THETA_RIGHT_BOUND) {
                total_theta = LEG_THETA_RIGHT_BOUND;
            }
        }

    }

    public void reset() {
        matrix = new Affine();
        total_theta = 0;
        translate_matrix.setToIdentity();
        translate_matrix.prependTranslation(initial_x, initial_y);
        scale_factor = 1;
        scale_matrix.setToIdentity();
        rotate_matrix.setToIdentity();
        for (BodyPart bp: children) {
            bp.reset();
        }
    }

}
