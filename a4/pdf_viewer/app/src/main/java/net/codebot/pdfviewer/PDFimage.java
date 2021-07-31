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

    final String LOGNAME = "pdf_image";
    final private float MAX_X_OFFSET = 1200;
    final private float MAX_Y_OFFSET = 1200;

    private int pageNum = 0;
    //private GestureDetector gd;
    private ScaleGestureDetector gd;

    // drawing path
    Path path = null;
    ArrayList<ArrayList<DrawPath>>  pen_paths;
    ArrayList<ArrayList<DrawPath>>  highlighter_paths;

    Stack<Operation> undo_stack;
    Stack<Operation> redo_stack;

    private Boolean pen_enabled, highlighter_enabled, eraser_enabled, cursor_enabled;
    private Boolean scale_mod = false; // true when scale happens to disable translation because of pinch motion
    private float cursor_x, cursor_y; // start value of the cursors
    private float translate_x = 0, translate_y = 0, scale = 1;

    // image to display
    Bitmap bitmap;
    Paint pen_paint, highlighter_paint;


    // constructor
    public PDFimage(Context context, int total_pages) {
        super(context);
        gd = new ScaleGestureDetector(context, new ScaleListener());

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
        //gd.onTouchEvent(event);
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
                //getPoints(path);
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
                } else if (cursor_enabled) {
                    translate_x += event.getX() - cursor_x;
                    translate_y += event.getY() - cursor_y;
                }
                break;
        }
        return true;
    }

    public float get_original_x(float x){
        return x - translate_x;
    }

    public float get_original_y(float y) {
        return y - translate_y;
    }

    public void reset_translate_scale() {
        translate_x = 0;
        translate_y = 0;
        scale = 1;
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
        canvas.scale(scale, scale);
        canvas.translate(translate_x, translate_y);
        // draw background
        if (bitmap != null) {
            this.setImageBitmap(bitmap);
        }
        // draw pen
        for (DrawPath path : pen_paths.get(pageNum)) {
            if (path.is_valid()) {
                canvas.drawPath(path.get_path(), pen_paint);
            }
        }
        // draw highlighter
        for (DrawPath path : highlighter_paths.get(pageNum)) {
            if (path.is_valid()) {
                canvas.drawPath(path.get_path(), highlighter_paint);
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
        int len = p1_path.size();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
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

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (cursor_enabled) {
                float mScaleFactor = detector.getScaleFactor();
                mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
                scale = mScaleFactor;
                scale_mod = true;
            }
            return true;
        }
    }

}