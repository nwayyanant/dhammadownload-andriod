/*
PDF Viewer Activity to display PDF inside the App without Third Party App
Chnage History
--------------
27/Jan/2020 - Initial version
 */

package com.dhammadownload.dhammadownloadandroid.activity;


import android.app.Activity;
import android.content.Context;
//import android.graphics.pdf.PdfRenderer;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.dhammadownload.dhammadownloadandroid.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class PdfViewerActivity extends Activity {

    private static final String TAG = "PdfViewerActivity";



    Integer pageNumber = 0;
    String pdfFileName;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try{

        Log.d(TAG,"[onCreate] Start.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer);

        ImageView pdfImageView = null;
        //get extras

        Bundle extras = getIntent().getExtras();
        String openMediaFile = extras.getString("openMediaFile");

            File openPdfFile = new File(openMediaFile);

            Bitmap pageBitmap = renderPdfPage(this, openMediaFile, pageNumber);
            pdfImageView.setImageBitmap(pageBitmap);
            //pdfView = (PDFView)findViewById(R.id.pdfView);
            //pdfView.fromUri(Uri.fromFile(openPdfFile)).load();

//            pdfView.fromUri(Uri.fromFile(openPdfFile))
//                    .defaultPage(pageNumber)
//                    .enableSwipe(true)
//                    .swipeHorizontal(false)
//                    //.onPageChange(this)
//                    .enableAnnotationRendering(true)
//                    //.onLoad(this)
//                    .scrollHandle(new DefaultScrollHandle(this))
//                    .load();


            //openRenderer(this,openMediaFile);


        }catch(Exception e)
        {
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }



        Log.d(TAG,"[onCreate] End.");
    }

//    @Override
//    public void onPageChanged(int page, int pageCount) {
//        pageNumber = page;
//        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
//    }


//    @Override
//    public void loadComplete(int nbPages) {
//        PdfDocument.Meta meta = pdfView.getDocumentMeta();
//        printBookmarksTree(pdfView.getTableOfContents(), "-");
//
//    }

//    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
//        for (PdfDocument.Bookmark b : tree) {
//
//            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));
//
//            if (b.hasChildren()) {
//                printBookmarksTree(b.getChildren(), sep + "-");
//            }
//        }
//    }




    @Override
    public void onDestroy() {

        Log.d(TAG,"[onDestroy] Start.");

        super.onDestroy();

        Log.d(TAG,"[onDestroy] End.");

    }


    public void doClose(View view){

        finish();

    }

    public Bitmap renderPdfPage(Context context, String filePath, int pageIndex) throws IOException {

        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
        PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        page.close();
        pdfRenderer.close();
        fileDescriptor.close();

        return bitmap;
    }
}