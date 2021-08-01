package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.ImageView;
import java.lang.Math;

import java.util.ArrayList;
import java.util.Stack;

@SuppressLint("AppCompatCustomView")
public class PDFimage extends ImageView {
    final private float MAX_X_OFFSET = 1200;
    final private float MAX_Y_OFFSET = 1200;

    private int pageNum = 0;

    // drawing path
    Path path = null;
    ArrayList<ArrayList<DrawPath>>  pen_paths;
    ArrayList<ArrayList<DrawPath>>  highlighter_paths;

    Stack<Operation> undo_stack;
    Stack<Operation> redo_stack;

    private Boolean pen_enabled, highlighter_enabled, eraser_enabled, cursor_enabled;
    private float cursor_x, cursor_y; // start value of the cursors
    private float translate_x = 0, translate_y = 0;

    // image to display
    Bitmap bitmap;
    Paint pen_paint, highlighter_paint;

    float x1, x2, y1, y2, old_x1, old_y1, old_x2, old_y2;
    float mid_x = -1f, mid_y = -1f, old_mid_x = -1f, old_mid_y = -1f;
    int p1_id, p1_index, p2_id, p2_index;

    Matrix matrix = new Matrix();
    Matrix inverse = new Matrix();

    private float[] inverted;


    // constructor
    public PDFimage(Context context, int total_pages) {
        super(context);

        pen_enabled = false;
        highlighter_enabled = false;
        eraser_enabled = false;
        cursor_enabled = true;

        pen_paint = new Paint();
        pen_paint.setColor(Color.BLACK);
        pen_paint.setStrokeWidth(5);
        pen_paint.setStyle(Paint.Style.STROKE);

        highlighter_paint = new Paint();
        highlighter_paint.setColor(Color.YELLOW);
        highlighter_paint.setStrokeWidth(30);
        highlighter_paint.setStyle(Paint.Style.STROKE);

        undo_stack = new Stack<>();
        redo_stack = new Stack<>();

        pen_paths = new ArrayList<>();
        highlighter_paths = new ArrayList<>();
        for (int i = 0; i < total_pages; i++) {
            pen_paths.add(new ArrayList<>());
            highlighter_paths.add(new ArrayList<>());
        }
    }

    // capture touch events (down/move/up) to create a path
    // and use that to create a stroke that we can draw
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getPointerCount()) {
            case 2:
                p1_id = event.getPointerId(0);
                p1_index = event.findPointerIndex(p1_id);
                inverted = new float[] { event.getX(p1_index), event.getY(p1_index)};
                inverse.mapPoints(inverted);

                if (old_x1 < 0 || old_y1 < 0) {
                    old_x1 = x1 = inverted[0];
                    old_y1 = y1 = inverted[1];
                } else {
                    old_x1 = x1;
                    old_y1 = y1;
                    x1 = inverted[0];
                    y1 = inverted[1];
                }

                // point 2
                p2_id = event.getPointerId(1);
                p2_index = event.findPointerIndex(p2_id);

                // mapPoints returns values in-place
                inverted = new float[] { event.getX(p2_index), event.getY(p2_index) };
                inverse.mapPoints(inverted);

                // first pass, initialize the old == current value
                if (old_x2 < 0 || old_y2 < 0) {
                    old_x2 = x2 = inverted[0];
                    old_y2 = y2 = inverted[1];
                } else {
                    old_x2 = x2;
                    old_y2 = y2;
                    x2 = inverted[0];
                    y2 = inverted[1];
                }

                // midpoint
                mid_x = (x1 + x2) / 2;
                mid_y = (y1 + y2) / 2;
                old_mid_x = (old_x1 + old_x2) / 2;
                old_mid_y = (old_y1 + old_y2) / 2;

                // distance
                float d_old = (float) Math.sqrt(Math.pow((old_x1 - old_x2), 2) + Math.pow((old_y1 - old_y2), 2));
                float d = (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));

                // pan and zoom during MOVE event
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // pan == translate of midpoint
                    float dx = mid_x - old_mid_x;
                    float dy = mid_y - old_mid_y;
                    matrix.preTranslate(dx, dy);

                    // zoom == change of spread between p1 and p2
                    float scale = d/d_old;
                    scale = Math.max(0, scale);
                    float [] pts = new float[9];
                    matrix.getValues(pts);
                    // limit scale factor
                    if (scale > 1 && pts[0] <= 3) {
                        matrix.preScale(scale, scale, mid_x, mid_y);
                    } else if (scale < 1 && pts[0] > 0.5) {
                        matrix.preScale(scale, scale, mid_x, mid_y);
                    }



                // reset on up
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    old_x1 = -1f;
                    old_y1 = -1f;
                    old_x2 = -1f;
                    old_y2 = -1f;
                    old_mid_x = -1f;
                    old_mid_y = -1f;
                }
                break;

            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // pressed
                        if (cursor_enabled) {
                            cursor_x = event.getX();
                            cursor_y = event.getY();
                        }
                        path = new Path();
                        path.moveTo(get_original_x(event.getX()), get_original_y(event.getY()));
                        break;
                    case MotionEvent.ACTION_MOVE: // pressed and moving
                        if (cursor_enabled) {
                            translate_x += event.getX() - cursor_x;
                            translate_y += event.getY() - cursor_y;
                            cursor_x = event.getX();
                            cursor_y = event.getY();
                        }
                        path.lineTo(get_original_x(event.getX()), get_original_y(event.getY()));
                        break;
                    case MotionEvent.ACTION_UP: // released
                        if (pen_enabled) {
                            pen_paths.get(pageNum).add(new DrawPath(path));
                            add_undo(new Draw(true, pageNum, pen_paths.get(pageNum).size() - 1));
                        } else if (highlighter_enabled) {
                            highlighter_paths.get(pageNum).add(new DrawPath(path));
                            add_undo(new Draw(false, pageNum, highlighter_paths.get(pageNum).size() - 1));
                        } else if (eraser_enabled) {
                            Erase e = new Erase(pageNum);
                            erase(path, pen_paths.get(pageNum), e, true);
                            erase(path, highlighter_paths.get(pageNum), e, false);
                            add_undo(e);
                        } /*else if (cursor_enabled) {
                            translate_x += event.getX() - cursor_x;
                            translate_y += event.getY() - cursor_y;
                        } */
                        path = null; // reset path
                        break;
                }
                break;
        }

        return true;
    }

    public float get_original_x(float x){
        float point [] = new float[] {x, 0};
        matrix.invert(inverse);
        inverse.mapPoints(point);
        return point[0] - translate_x;
    }

    public float get_original_y(float y) {
        float point [] = new float[] {0, y};
        matrix.invert(inverse);
        inverse.mapPoints(point);
        return point[1] - translate_y;
    }

    public void reset_translate_scale() {
        translate_x = 0;
        translate_y = 0;
        matrix = new Matrix();
    }

    public void add_undo(Operation o) {
        undo_stack.push(o);
        // clear redo stack
        redo_stack = new Stack<>();

        if (undo_stack.size() >= 10) {
            Stack<Operation> new_undo = new Stack<>();
            Stack<Operation> temp_stack = new Stack<>();
            // transfer first 5 onto new stack and get rid of the rest
            for (int i = 0; i < 5; i++) {
                temp_stack.push(undo_stack.peek());
                undo_stack.pop();
            }
            while (!temp_stack.empty()) {
                new_undo.push(temp_stack.peek());
                temp_stack.pop();
            }
            undo_stack = new_undo;
        }
    }

    // set image as background
    public void setImage(Bitmap bitmap, int pn) {
        reset_translate_scale(); // reset all translations/scaling
        this.pageNum = pn;
        this.bitmap = bitmap;
    }

    public void setPen() {
        eraser_enabled = false;
        pen_enabled = true;
        highlighter_enabled = false;
        cursor_enabled = false;
    }

    public void setHighlighter() {
        eraser_enabled = false;
        pen_enabled = false;
        highlighter_enabled = true;
        cursor_enabled = false;
    }

    public void setEraser() {
        eraser_enabled = true;
        pen_enabled = false;
        highlighter_enabled = false;
        cursor_enabled = false;
    }

    public void setCursor() {
        eraser_enabled = false;
        pen_enabled = false;
        highlighter_enabled = false;
        cursor_enabled = true;
    }

    // return page number with the operation
    public int undo() {
        if (undo_stack.empty()) return pageNum;
        Operation o = undo_stack.peek();
        undo_stack.pop();
        redo_stack.push(o);
        int op_page = 0;
        if (o.isDraw()) {
            Draw undo_op = (Draw) o;
            op_page = undo_op.getPageNum();
            if (undo_op.isPen()) {
                pen_paths.get(undo_op.getPageNum()).get(undo_op.getIdx()).remove();
            } else {
                highlighter_paths.get(undo_op.getPageNum()).get(undo_op.getIdx()).remove();
            }
        } else { // undo erase
            Erase undo_op = (Erase) o;
            ArrayList<Integer> pen_idx = undo_op.getPen_idx();
            int len = pen_idx.size();
            int undo_pageNum = undo_op.getPageNum();
            op_page = undo_pageNum;
            for (int i = 0; i < len; i++) {
                pen_paths.get(undo_pageNum).get(pen_idx.get(i)).restore();
            }
            ArrayList<Integer> highlighter_idx = undo_op.getHighlighter_idx();
            len = highlighter_idx.size();
            for (int i = 0; i < len; i++) {
                highlighter_paths.get(undo_pageNum).get(highlighter_idx.get(i)).restore();
            }
        }
        return op_page;
    }

    public int redo() {
        if (redo_stack.empty()) return pageNum;
        Operation o = redo_stack.peek();
        redo_stack.pop();
        undo_stack.push(o);

        int op_page = 0;
        if (o.isDraw()) {
            Draw redo_op = (Draw) o;
            op_page = redo_op.getPageNum();
            if (redo_op.isPen()) {
                pen_paths.get(redo_op.getPageNum()).get(redo_op.getIdx()).restore();
            } else {
                highlighter_paths.get(redo_op.getPageNum()).get(redo_op.getIdx()).restore();
            }
        } else { // undo erase
            Erase undo_op = (Erase) o;
            ArrayList<Integer> pen_idx = undo_op.getPen_idx();
            int len = pen_idx.size();
            int undo_pageNum = undo_op.getPageNum();
            op_page = undo_op.getPageNum();
            for (int i = 0; i < len; i++) {
                pen_paths.get(undo_pageNum).get(pen_idx.get(i)).remove();
            }
            ArrayList<Integer> highlighter_idx = undo_op.getHighlighter_idx();
            len = highlighter_idx.size();
            for (int i = 0; i < len; i++) {
                highlighter_paths.get(undo_pageNum).get(highlighter_idx.get(i)).remove();
            }
        }
        return op_page;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        // adjust translate so that the image does not go out of bound of the page

        if (translate_x > MAX_X_OFFSET) {
            translate_x = MAX_X_OFFSET;
        } else if (translate_x < -MAX_X_OFFSET) {
            translate_x = -MAX_X_OFFSET;
        }

        if (translate_y > MAX_Y_OFFSET) {
            translate_y = MAX_Y_OFFSET;
        } else if (translate_y < -MAX_Y_OFFSET) {
            translate_y = -MAX_Y_OFFSET;
        }

        matrix.postTranslate(translate_x, translate_y);
        canvas.setMatrix(matrix);
        //canvas.scale(scale, scale);
        //canvas.translate(translate_x, translate_y);
        // draw background
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
        }
        matrix.postTranslate(-translate_x, -translate_y);

        // draw highlighter
        for (DrawPath path : highlighter_paths.get(pageNum)) {
            if (path.is_valid()) {
                canvas.drawPath(path.get_path(), highlighter_paint);
            }
        }
        // draw current path
        if (highlighter_enabled && path != null) {
            canvas.drawPath(path, highlighter_paint);
        } else if (pen_enabled && path != null) {
            canvas.drawPath(path, pen_paint);
        }

        // draw pen
        for (DrawPath path : pen_paths.get(pageNum)) {
            if (path.is_valid()) {
                canvas.drawPath(path.get_path(), pen_paint);
            }
        }
        super.onDraw(canvas);
        canvas.restore();
    }

    private ArrayList<ArrayList<Float>> getPoints(Path p) {
        ArrayList<ArrayList<Float>> pointArray = new ArrayList<>();
        PathMeasure pm = new PathMeasure(p, false);
        float length = pm.getLength();
        float distance = 0f;
        float speed = length / 20;
        int counter = 0;
        float[] aCoordinates = new float[2];

        while ((distance < length) && (counter < 20)) {
            // get point from the path
            pm.getPosTan(distance, aCoordinates, null);
            pointArray.add(new ArrayList<Float>());
            pointArray.get(counter).add(aCoordinates[0]);
            pointArray.get(counter).add(aCoordinates[1]);
            counter++;
            distance = distance + speed;
        }
        return pointArray;
    }

    //determine if two points intersect
    private Boolean intersect (Path p1, Path p2) {
        ArrayList<ArrayList<Float>> p1_path = getPoints(p1);
        ArrayList<ArrayList<Float>> p2_path = getPoints(p2);
        int len1 = p1_path.size();
        int len2 = p2_path.size();
        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                if ((Math.abs (p2_path.get(i).get(0) - p1_path.get(j).get(0)) <= 15) &&
                        (Math.abs (p2_path.get(i).get(1) - p1_path.get(j).get(1)) <= 15)) {
                    return true;
                }
            }
        }
        return false;
    }

    // erase all paths that intersect with erase path
    // store all erased paths in undo action e
    private void erase(Path erase_path, ArrayList<DrawPath> paths, Erase e, Boolean is_pen) {
        int len = paths.size();
        for (int i = 0; i < len; i++) {
            if (intersect(erase_path, paths.get(i).get_path())) {
                paths.get(i).remove();
                if (is_pen) {
                    e.addPenPath(i);
                } else {
                    e.addHighlighterPath(i);
                }
            }
        }
    }
}