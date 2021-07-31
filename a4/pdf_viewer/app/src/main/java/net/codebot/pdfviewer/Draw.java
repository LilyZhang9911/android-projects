package net.codebot.pdfviewer;

public class Draw implements Operation {
    private Boolean type; // pen or highlighter, true for pen, false for highlighter
    private int pageNum, idx; //pageNum and index in the array
    Draw(Boolean type, int pageNum, int idx) {
        this.type = type;
        this.pageNum = pageNum;
        this.idx = idx;
    }

    public Boolean isDraw() {
        return true;
    }

    public Boolean isPen() { return type; }

    public int getPageNum() { return pageNum; }

    public int getIdx() { return idx; }

    public String getStr() {
        String s = Boolean.toString(type) + " Page: " + Integer.toString(pageNum) + " idx: "
                + Integer.toString(idx);
        return s;
    }

}
