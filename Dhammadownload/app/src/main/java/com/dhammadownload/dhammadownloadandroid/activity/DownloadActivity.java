package com.dhammadownload.dhammadownloadandroid.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.SettingManager;
import com.dhammadownload.dhammadownloadandroid.common.StorageLocation;
import com.dhammadownload.dhammadownloadandroid.common.Utils;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class DownloadActivity extends Activity {

    private static final String TAG = "DownloadActivity";

    private MediaInfo mMediaInfo;

    String downloadURL = "";
    ImageView imgAuthor;
    TextView txtAuthorNme;
    TextView txtTitle;
    TextView txtDownloadSize;
    TextView txtDescription;
    TextView txtDownloadStausMessage;

    Button btnClose;
    Button btnDownload_Delete;
    Button btnOpen;

    String authorName = "";
    String authorRemoteURL = "";
    String folderPath = "";
    String authorImageURL = "";
    String authorImageLocalPath = "";
    String authorMainConfigURL = "";
    String authorMainConfigLocalPath = "";
    String authorMediaConfigURL = "";
    String authorMediaConfigLocalPath = "";

    // App-private download path (short + safe)
    String absoluteLocalFilePath = "";

    // Public Downloads entry for Android 10+
    Uri mediaStoreUri = null;

    // We’ll keep a stable, safe filename derived from URL so UI checks match before/after download
    String desiredFileName = "";

    String strFileDownloadMessage = "";
    Boolean mainFileDownload = true;
    Exception downloadAsyncTaskError;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    Typeface mmfontface;
    static DownloadFileFromURL downloadActivity;
    AlertDialog.Builder confirmDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "[onCreate] Start.");
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.download_layout);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                downloadURL = extras.getString("downloadURL");
                Log.d(TAG, "[onCreate] Download URL [" + downloadURL + "]");
            }

            btnClose = findViewById(R.id.btnDownloadClose);
            btnDownload_Delete = findViewById(R.id.btnDownloadDownload_Delete);
            btnOpen = findViewById(R.id.btnDownloadOpen);
            txtAuthorNme = findViewById(R.id.txtAuthorName);
            txtTitle = findViewById(R.id.txtTitle);
            imgAuthor = findViewById(R.id.imgViewAuthor);
            txtDownloadSize = findViewById(R.id.txtDownloadSize);
            txtDescription = findViewById(R.id.Desc);
            txtDownloadStausMessage = findViewById(R.id.txtDownloadStatusMsg);

            mmfontface = Typeface.createFromAsset(getAssets(), Constants.standardFont);

            mMediaInfo = Utils.convertToMeidaInfoFromUrl(downloadURL);
            authorName = mMediaInfo.getAuthorname();
            authorRemoteURL = Utils.getMainAuthorFolder(downloadURL, authorName);

            txtAuthorNme.setText(authorName);
            txtTitle.setText(Utils.getFileNameFromAnyURL(downloadURL));

            txtDescription.setTypeface(mmfontface);
            if (Utils.classifyRequestedFileType(Constants.supportedAudioFiles, downloadURL)) {
                txtDescription.setText(Constants.txtAudioFileDesc);
            } else if (Utils.classifyRequestedFileType(Constants.supportedVideoFiles, downloadURL)) {
                txtDescription.setText(Constants.txtVideoFileDesc);
            } else {
                txtDescription.setText(Constants.txtEbookFileDesc);
            }

            txtDownloadSize.setTypeface(mmfontface);
            txtDownloadSize.setText(Utils.getDownloadSize(downloadURL) + "-MB");
            txtDownloadStausMessage.setTypeface(mmfontface);

            initListenserAndDialog();

            // Prepare folders and small config files (app-private)
            createFolderAndDownloadConfigFiles();

            // Decide a SAFE file name up-front (from URL) and compute app-private path
            desiredFileName = buildSafeFilename(Utils.getFileNameFromAnyURL(downloadURL));
            File privateDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (privateDir != null && !privateDir.exists()) privateDir.mkdirs();
            File privateFile = new File(privateDir, desiredFileName);
            absoluteLocalFilePath = privateFile.getAbsolutePath();

            // Update UI based on whether file already exists
            File localFile = new File(absoluteLocalFilePath);
            if (localFile.exists()) {
                btnDownload_Delete.setText("DELETE");
                txtDownloadStausMessage.setText(
                        SettingManager.getInstance().getStorageLocation() == StorageLocation.DEVICE
                                ? Constants.fileDownloadedMessage
                                : Constants.fileDownloadedMessageForSDCard
                );
            } else {
                btnDownload_Delete.setText("DOWNLOAD");
                txtDownloadStausMessage.setText(
                        SettingManager.getInstance().getStorageLocation() == StorageLocation.DEVICE
                                ? Constants.fileToDownloadedMessage
                                : Constants.fileToDownloadedMessageForSDCard
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
            showDownloadError(e);
        }
        Log.d(TAG, "[onCreate] End.");
    }

    private void createFolderAndDownloadConfigFiles() {
        boolean isSDCardAvailable = Utils.isSDCardAvailable();
        if (SettingManager.getInstance().getStorageLocation() == StorageLocation.SDCARD && (!isSDCardAvailable)) {
            new AlertDialog.Builder(DownloadActivity.this)
                    .setTitle("Download")
                    .setMessage(Constants.SDCardNotWritableMsg)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", (dialog, which) ->
                            SettingManager.getInstance().setStorageLocation(StorageLocation.SDCARD))
                    .show();
        }

        folderPath = Utils.getLocalAppFolderPathFromWebURL(downloadURL);
        Utils.createFolder(folderPath);
        Utils.createNoMediaFile();
        Log.d(TAG, "[createFolderAndDownloadConfigFiles] Folders created [" + folderPath + "]");

        // Download small author/config assets into app-private cache
        authorImageURL = authorRemoteURL + "/" + authorName + Constants.authorImageExt;
        String authorLocalFolder = Utils.getLocalAppFolderPathFromWebURL(authorImageURL);
        authorImageLocalPath = authorLocalFolder + authorName + Constants.authorImageExt;
        Utils.downloadFile(authorImageURL, authorImageLocalPath);

        authorMainConfigURL = authorRemoteURL + "/" + Constants.authorMainConfigFile;
        authorMainConfigLocalPath = authorLocalFolder + Constants.authorMainConfigFile;
        Utils.downloadFile(authorMainConfigURL, authorMainConfigLocalPath);

        authorMediaConfigURL = authorRemoteURL + "/" + Constants.authorMediaConfig;
        authorMediaConfigLocalPath = authorLocalFolder + Constants.authorMediaConfig;
        Utils.downloadFile(authorMediaConfigURL, authorMediaConfigLocalPath);

        // Update UI from configs
        File authorMainConfigFile = new File(Utils.getDocumentDirectory() + authorMainConfigLocalPath);
        if (authorMainConfigFile.exists()) {
            txtAuthorNme.setTypeface(mmfontface);
            txtAuthorNme.setText(Utils.getProfileInfo(authorMainConfigLocalPath));
        }

        File mediaInfoConfigFile = new File(Utils.getDocumentDirectory() + authorMediaConfigLocalPath);
        String internetRemoteUrl = mMediaInfo.getInternetRemoteUrl();
        if (mediaInfoConfigFile.exists()) {
            txtTitle.setTypeface(mmfontface);
            txtTitle.setText(Utils.getMediaTitle(authorMediaConfigLocalPath, internetRemoteUrl));
        }

        File authorProfileImage = new File(Utils.getDocumentDirectory() + authorImageLocalPath);
        if (authorProfileImage.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(authorProfileImage.getAbsolutePath());
            imgAuthor.setImageBitmap(myBitmap);
        } else {
            imgAuthor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dhammadownload_logo));
        }
    }

    /***Initialize UI***/
    private void initListenserAndDialog() {
        btnClose.setOnClickListener(v -> finish());

        btnDownload_Delete.setOnClickListener(v -> {
            File localFile = new File(absoluteLocalFilePath);
            if (localFile.exists()) {
                confirmDialog = new AlertDialog.Builder(DownloadActivity.this);
                confirmDialog
                        .setTitle("Delete")
                        .setMessage("Are you sure?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Utils.deleteFileFromLocal(absoluteLocalFilePath);
                            btnDownload_Delete.setText("DOWNLOAD");
                            txtDownloadStausMessage.setText(Constants.fileToDownloadedMessage);
                            // NOTE: MediaStore copy (if any) is not auto-deleted here.
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                downloadActivity = new DownloadFileFromURL();
                mainFileDownload = true;
                strFileDownloadMessage = "Downloading. Please wait...";
                downloadActivity.execute(downloadURL);
            }
        });

        btnOpen.setOnClickListener(v -> {
            File localFile = new File(absoluteLocalFilePath);
            boolean isAudio = Utils.classifyRequestedFileType(Constants.supportedAudioFiles, downloadURL.toUpperCase(Locale.ROOT));
            boolean isVideo = Utils.classifyRequestedFileType(Constants.supportedVideoFiles, downloadURL.toUpperCase(Locale.ROOT));
            boolean isPdf   = Utils.classifyRequestedFileType(Constants.supportedEbookFiles, downloadURL.toUpperCase(Locale.ROOT));

            if (localFile.exists()) {
                if (isAudio) {
                    Intent audioPlayer = new Intent(DownloadActivity.this, AudioPlayerActivity.class);
                    audioPlayer.setAction(Intent.ACTION_VIEW);
                    audioPlayer.putExtra("authorProfileImage", Utils.getDocumentDirectory() + authorImageLocalPath);
                    audioPlayer.putExtra("authorName", txtAuthorNme.getText());
                    audioPlayer.putExtra("title", txtTitle.getText());
                    audioPlayer.putExtra("openMediaFile", absoluteLocalFilePath);
                    startActivity(audioPlayer);
                } else if (isVideo) {
                    try {
                        File openMediaFile = new File(absoluteLocalFilePath);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri uri = FileProvider.getUriForFile(
                                DownloadActivity.this,
                                getApplicationContext().getPackageName() + ".provider",
                                openMediaFile
                        );
                        intent.setDataAndType(uri, "video/mp4");
                        startActivity(intent);
                    } catch (ActivityNotFoundException ae) {
                        Toast.makeText(DownloadActivity.this,
                                "No video player App installed. Please install a video player.",
                                Toast.LENGTH_LONG).show();
                    }
                } else if (isPdf) {
                    Intent pdfViewer = new Intent(DownloadActivity.this, PdfViewerActivity.class);
                    pdfViewer.setAction(Intent.ACTION_VIEW);
                    pdfViewer.putExtra("openMediaFile", absoluteLocalFilePath);
                    startActivity(pdfViewer);
                }
            } else {
                // Open online
                if (isAudio) {
                    Intent audioPlayer = new Intent(DownloadActivity.this, AudioPlayerActivity.class);
                    audioPlayer.setAction(Intent.ACTION_VIEW);
                    audioPlayer.putExtra("authorProfileImage", Utils.getDocumentDirectory() + authorImageLocalPath);
                    audioPlayer.putExtra("authorName", txtAuthorNme.getText());
                    audioPlayer.putExtra("title", txtTitle.getText());
                    audioPlayer.putExtra("openMediaFile", downloadURL);
                    startActivity(audioPlayer);
                } else if (isVideo) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(downloadURL), "video/mp4");
                        startActivity(intent);
                    } catch (ActivityNotFoundException ae) {
                        Toast.makeText(DownloadActivity.this,
                                "No video player App installed. Please install a video player.",
                                Toast.LENGTH_LONG).show();
                    }
                } else if (isPdf) {
                    strFileDownloadMessage = "Loading file. Please wait...";
                    mainFileDownload = false;
                    downloadActivity = new DownloadFileFromURL();
                    downloadActivity.execute(downloadURL);
                }
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            if (downloadActivity != null) downloadActivity.cancel(true);
            File downloadFile = new File(absoluteLocalFilePath);
            if (downloadFile.exists()) {
                downloadFile.delete();
            }
            pDialog.cancel();
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == progress_bar_type) {
            pDialog.setMessage(strFileDownloadMessage);
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
            return pDialog;
        }
        return null;
    }

    private void showDownloadError(Exception downloadException) {
        if (downloadException != null) {
            new AlertDialog.Builder(DownloadActivity.this)
                    .setTitle("Error")
                    .setMessage("Unexpected Error Occurred.\nDetails: " + downloadException.getMessage())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        txtDownloadStausMessage.setText(Constants.DownloadErrorMsg);
                        btnDownload_Delete.setEnabled(false);
                        btnOpen.setEnabled(false);
                    })
                    .show();
        }
    }

    // ---------- MediaStore helpers ----------
    private Uri createMediaStoreItem(String displayName, String mime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
        if (!TextUtils.isEmpty(mime)) values.put(MediaStore.Downloads.MIME_TYPE, mime);
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        ContentResolver resolver = getContentResolver();
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        return resolver.insert(collection, values);
    }

    private void finalizeMediaStoreItem(Uri uri) {
        if (uri == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        getContentResolver().update(uri, values, null, null);
    }

    // ---------- Filename safety helpers ----------
    private static final int MAX_FILENAME_BYTES = 120; // conservative vs 255 max

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1) return name.substring(i);
        return "";
    }

    private String sanitizeBase(String s) {
        String cleaned = s.replaceAll("[/\\\\:*?\"<>|\\p{Cntrl}]+", " ").trim();
        cleaned = cleaned.replaceAll("\\s{2,}", " ");
        if (cleaned.length() > 200) cleaned = cleaned.substring(0, 200);
        return cleaned;
    }

    private String truncateUtf8ToBytes(String base, int maxBytes) {
        byte[] bytes = base.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return base;

        int hi = base.length();
        int lo = 0;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            byte[] b = base.substring(0, mid).getBytes(StandardCharsets.UTF_8);
            if (b.length <= maxBytes) lo = mid + 1;
            else hi = mid;
        }
        int cut = Math.max(1, lo - 1);
        return base.substring(0, cut);
    }

    private String buildSafeFilename(String desiredName) {
        if (TextUtils.isEmpty(desiredName)) desiredName = "downloaded_file";
        String ext = getExtension(desiredName);
        String base = sanitizeBase(ext.isEmpty()
                ? desiredName
                : desiredName.substring(0, desiredName.length() - ext.length()));
        int budget = Math.max(16, MAX_FILENAME_BYTES - ext.getBytes(StandardCharsets.UTF_8).length);
        base = truncateUtf8ToBytes(base, budget);
        if (base.isEmpty()) base = "file";
        return base + ext;
    }

    private String guessMimeType(String fileName) {
        String ext = "";
        int idx = fileName.lastIndexOf('.');
        if (idx >= 0 && idx < fileName.length() - 1) ext = fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        if (mime == null) {
            if ("mp3".equals(ext)) return "audio/mpeg";
            if ("mp4".equals(ext)) return "video/mp4";
            if ("pdf".equals(ext)) return "application/pdf";
            return "application/octet-stream";
        }
        return mime;
    }

    // ---------- File copy helper (for temp PDF etc.) ----------
    private void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new java.io.FileInputStream(src);
             OutputStream out = new java.io.FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }

    /**
     * Background Async Task to download file
     * - Writes to app-private Downloads (short safe path)
     * - On Android 10+ also writes to MediaStore (public Downloads)
     */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            Log.d(TAG, "[doInBackground] Start.");
            int count;
            InputStream input = null;
            OutputStream mediaStoreOut = null;
            OutputStream privateOut = null;

            try {
                downloadAsyncTaskError = null;

                // Always use the same safe filename we computed from URL so UI state is consistent
                String fileName = TextUtils.isEmpty(desiredFileName)
                        ? buildSafeFilename(Utils.getFileNameFromAnyURL(f_url[0]))
                        : desiredFileName;
                String mimeType = guessMimeType(fileName);

                // App-private mirror file
                File privateDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (privateDir != null && !privateDir.exists()) privateDir.mkdirs();
                File privateFile = new File(privateDir, fileName);
                absoluteLocalFilePath = privateFile.getAbsolutePath();
                privateOut = new FileOutputStream(privateFile);

                // MediaStore (public) on Android 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaStoreUri = createMediaStoreItem(fileName, mimeType);
                    if (mediaStoreUri != null) {
                        mediaStoreOut = getContentResolver().openOutputStream(mediaStoreUri, "w");
                    }
                }

                // Network
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();

                input = new BufferedInputStream(url.openStream(), 8192);
                byte[] data = new byte[8192];
                long total = 0;

                while (!isCancelled() && (count = input.read(data)) != -1) {
                    total += count;
                    if (lengthOfFile > 0) {
                        publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    }
                    privateOut.write(data, 0, count);
                    if (mediaStoreOut != null) mediaStoreOut.write(data, 0, count);
                }

                if (isCancelled()) throw new InterruptedException("Download canceled");

                privateOut.flush();
                if (mediaStoreOut != null) mediaStoreOut.flush();

            } catch (Exception e) {
                mainFileDownload = false;
                downloadAsyncTaskError = e;
                Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
                try { new File(absoluteLocalFilePath).delete(); } catch (Throwable ignore) {}
                if (mediaStoreUri != null) {
                    try { getContentResolver().delete(mediaStoreUri, null, null); } catch (Throwable ignore) {}
                    mediaStoreUri = null;
                }
            } finally {
                try { if (input != null) input.close(); } catch (Throwable ignore) {}
                try { if (privateOut != null) privateOut.close(); } catch (Throwable ignore) {}
                try { if (mediaStoreOut != null) mediaStoreOut.close(); } catch (Throwable ignore) {}
            }

            Log.d(TAG, "[doInBackground] End.");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            Log.d(TAG, "[onPostExecute] Start.");
            try {
                dismissDialog(progress_bar_type);
                showDownloadError(downloadAsyncTaskError);

                if (mediaStoreUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    finalizeMediaStoreItem(mediaStoreUri);
                }

                if (mainFileDownload) {
                    btnDownload_Delete.setText("DELETE");
                    txtDownloadStausMessage.setText(Constants.fileDownloadedMessage);

                    boolean isAudio = Utils.classifyRequestedFileType(Constants.supportedAudioFiles, downloadURL.toUpperCase(Locale.ROOT));
                    boolean isVideo = Utils.classifyRequestedFileType(Constants.supportedVideoFiles, downloadURL.toUpperCase(Locale.ROOT));
                    boolean isPdf   = Utils.classifyRequestedFileType(Constants.supportedEbookFiles, downloadURL.toUpperCase(Locale.ROOT));

                    if (isAudio) {
                        Intent audioPlayer = new Intent(DownloadActivity.this, AudioPlayerActivity.class);
                        audioPlayer.setAction(Intent.ACTION_VIEW);
                        audioPlayer.putExtra("authorProfileImage", Utils.getDocumentDirectory() + authorImageLocalPath);
                        audioPlayer.putExtra("authorName", txtAuthorNme.getText());
                        audioPlayer.putExtra("title", txtTitle.getText());
                        audioPlayer.putExtra("openMediaFile", absoluteLocalFilePath);
                        startActivity(audioPlayer);
                    } else if (isVideo) {
                        try {
                            File openMediaFile = new File(absoluteLocalFilePath);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri uri = FileProvider.getUriForFile(
                                    DownloadActivity.this,
                                    getApplicationContext().getPackageName() + ".provider",
                                    openMediaFile
                            );
                            intent.setDataAndType(uri, "video/mp4");
                            startActivity(intent);
                        } catch (ActivityNotFoundException ae) {
                            Toast.makeText(DownloadActivity.this,
                                    "No video player App installed. Please install a video player.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else if (isPdf) {
                        Intent pdfViewer = new Intent(DownloadActivity.this, PdfViewerActivity.class);
                        pdfViewer.setAction(Intent.ACTION_VIEW);
                        pdfViewer.putExtra("openMediaFile", absoluteLocalFilePath);
                        startActivity(pdfViewer);
                    }
                } else {
                    openOnlinePdf();
                }

                if (downloadActivity != null) downloadActivity = null;

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
            Log.d(TAG, "[onPostExecute] End.");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (downloadActivity != null) downloadActivity.cancel(true);
            if (mediaStoreUri != null) {
                try { getContentResolver().delete(mediaStoreUri, null, null); } catch (Throwable ignore) {}
                mediaStoreUri = null;
            }
        }
    }

    public void openOnlinePdf() {
        try {
            Log.d(TAG, "[openOnlinePdf] Start.");

            String tempPdfPath = Utils.getPDFTempFile();
            File tempFile = new File(tempPdfPath);
            File downloadedFile = new File(absoluteLocalFilePath);

            if (tempFile.exists()) tempFile.delete();
            if (downloadedFile.exists()) {
                // Safe copy instead of renameTo (works across volumes / scoped storage)
                copyFile(downloadedFile, tempFile);

                Intent pdfViewer = new Intent(this, PdfViewerActivity.class);
                pdfViewer.setAction(Intent.ACTION_VIEW);
                pdfViewer.putExtra("openMediaFile", tempFile.getPath());
                startActivity(pdfViewer);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + Log.getStackTraceString(e));
        }
        Log.d(TAG, "[openOnlinePdf] End.");
    }
}
