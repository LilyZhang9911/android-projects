package net.codebot.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provide this code.

public class MainActivity extends AppCompatActivity {
    final String LOGNAME = "pdf_viewer";
    final String FILENAME = "shannon1948.pdf";
    final int FILERESID = R.raw.shannon1948;

    // manage the pages of the PDF, see below
    PdfRenderer pdfRenderer;
    private ParcelFileDescriptor parcelFileDescriptor;
    private PdfRenderer.Page currentPage;

    private LinearLayout pdf;
    private Button prev, next;
    private Button undo, redo, pen, highlighter, eraser, cursor;

    private int pausedPage = -1;
    private Boolean initComplete = false;

    // custom ImageView class that captures strokes and draws them over the image
    PDFimage pageImage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init buttons
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        pen = findViewById(R.id.pen);
        highlighter = findViewById(R.id.highlighter);
        cursor = findViewById(R.id.cursor);
        cursor.setSelected(true); // initially in cursor mode
        eraser = findViewById(R.id.eraser);
        undo = findViewById(R.id.undo);
        redo = findViewById(R.id.redo);

        pen.setOnClickListener((v) -> {
            pen.setSelected(true);
            highlighter.setSelected(false);
            eraser.setSelected(false);
            cursor.setSelected(false);

            pageImage.setPen();
        });

        highlighter.setOnClickListener((v) -> {
            pen.setSelected(false);
            highlighter.setSelected(true);
            eraser.setSelected(false);
            cursor.setSelected(false);

            pageImage.setHighlighter();

        });

        eraser.setOnClickListener((v) -> {
            pen.setSelected(false);
            highlighter.setSelected(false);
            eraser.setSelected(true);
            cursor.setSelected(false);

            pageImage.setEraser();
        });

        cursor.setOnClickListener((v) -> {
            pen.setSelected(false);
            highlighter.setSelected(false);
            eraser.setSelected(false);
            cursor.setSelected(true);

            pageImage.setCursor();
        });

        undo.setOnClickListener((v)->{
            int op_page = pageImage.undo();
            renderPage(op_page);
        });
        redo.setOnClickListener((v)->{
            int op_page = pageImage.redo();
            renderPage(op_page);
        });

        // init renderer
        try {
            openRenderer(this);
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening PDF");
        }

        pdf = findViewById(R.id.pdf);
        pageImage = new PDFimage(this, pdfRenderer.getPageCount());
        pageImage.setMinimumWidth(1000);
        pageImage.setMinimumHeight(2000);
        pdf.addView(pageImage);

        // start on page 0
        renderPage(0);

        View.OnClickListener buttons = (v) -> {
           //if (pdfRenderer != null && currentPage != null) {
            int curPage = currentPage.getIndex();
            if (v == prev && curPage > 0) {
                renderPage(curPage-1);
            } else if (v == next && curPage < pdfRenderer.getPageCount()-1){
                renderPage(curPage+1);
            }
        };
        prev.setOnClickListener(buttons);
        next.setOnClickListener(buttons);
        initComplete = true;

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void renderPage(int pageNum) {
        if (currentPage != null) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(pageNum);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // turn prev and next off on the first and last page
        if (currentPage.getIndex() > 0) {
            prev.setEnabled(true);
        }
        if (currentPage.getIndex() + 1 < pdfRenderer.getPageCount()) {
            next.setEnabled(true);
        }
        pageImage.setImage(bitmap, pageNum);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openRenderer(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), FILENAME);
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            InputStream asset = this.getResources().openRawResource(FILERESID);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    // do this before you quit!
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeRenderer() throws IOException {
        if (null != currentPage) {
            // save page before closing
            pausedPage = currentPage.getIndex();
            currentPage.close();
            currentPage = null;
        }
        pdfRenderer.close();
        parcelFileDescriptor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStop() {
        super.onStop();
        try {
            closeRenderer();
        } catch (IOException ex) {
            Log.d(LOGNAME, "Unable to close PDF renderer");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        if (initComplete) {
            initComplete = false; // skip first onResume call
            return;
        }
        // init renderer
        try {
            openRenderer(this);
        } catch (IOException exception) {
            Log.d(LOGNAME, "Error opening PDF");
        }
        // start on page 0
        renderPage(pausedPage);
    }

}
