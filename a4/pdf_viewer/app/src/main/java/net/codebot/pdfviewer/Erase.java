package net.codebot.pdfviewer;

import android.graphics.PathMeasure;

import java.util.ArrayList;

public class Erase implements Operation{
    private int pageNum;
    private ArrayList<Integer> pen_idx;
    private ArrayList<Integer> highlighter_idx;
    public Erase(int pageNum) {
        this.pageNum = pageNum;
        pen_idx = new ArrayList<>();
        highlighter_idx = new ArrayList<>();
    }

    public void addPenPath(int i) {
        pen_idx.add(i);
    }

    public void addHighlighterPath(int i) {
        highlighter_idx.add(i);
    }

    public String getStr() {
        String s = Integer.toString(pageNum) + "\nPen: ";
        for (int i = 0; i < pen_idx.size(); i++) {
            s += Integer.toString(pen_idx.get(i)) + " ";
        }
        s += "\nHighlighter: ";
        for (int i = 0; i < highlighter_idx.size(); i++) {
            s += Integer.toString(highlighter_idx.get(i)) + " ";
        }
        s += "\n";
        return s;
    }

    public Boolean isDraw() { return false; }

    public int getPageNum () { return pageNum; }
    public ArrayList<Integer> getPen_idx () { return pen_idx;}
    public ArrayList<Integer> getHighlighter_idx() { return highlighter_idx; }
}
