package net.codebot.pdfviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import java.lang.Math;

public class DrawPath {
    private Path path;
    private Boolean valid;
    public DrawPath(Path p) {
        path = p;
        valid = true;
    }
    public Boolean is_valid() { return valid; }
    public Path get_path() { return path; }
    public void remove() { valid = false; }
    public void restore() {valid = true; }
}
