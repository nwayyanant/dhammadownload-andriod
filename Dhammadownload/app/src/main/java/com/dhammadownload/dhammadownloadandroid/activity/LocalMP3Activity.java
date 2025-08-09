package com.dhammadownload.dhammadownloadandroid.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.adapter.MediaInfoAdapter;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.StorageLocation;
import com.dhammadownload.dhammadownloadandroid.common.TextUtils;
import com.dhammadownload.dhammadownloadandroid.common.Utils;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;

import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class LocalMP3Activity extends SwipeListViewActivity {

    private static final String TAG = "LocalMP3Activity";

    private ListView mListView;
    MediaInfoAdapter mediaInfoAdapter;
    Button btnEdit;
    ArrayList<MediaInfo> list;
    TextView txtHeader;
    TextView txtEmptyListView;

    Typeface mmfontface;
    int selectedIndex;

    AlertDialog.Builder confirmDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            Log.d(TAG,"[onCreate] Start.");

            super.onCreate(savedInstanceState);
            setContentView(R.layout.local_mp3_layout);
            mListView = findViewById(R.id.listView1);
            list = new ArrayList<>();

            mmfontface = Typeface.createFromAsset(getAssets(), Constants.standardFont);

            txtHeader = findViewById(R.id.txtHeader);
            txtHeader.setText(Constants.locaMP3ListHeader);
            txtHeader.setTypeface(mmfontface);

            // IMPORTANT:
            // Ensure Utils.getListOfAllDownloadedMedia(...) scans the app-private Downloads dir:
            // getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            // Utils.getListOfAllDownloadedMedia(Constants.supportedAudioFiles, list);
            Utils.getListFromAppPrivateDownloads(this, Constants.supportedAudioFiles, list);

            mediaInfoAdapter = new MediaInfoAdapter(list, this);
            mListView.setAdapter(mediaInfoAdapter);

            txtEmptyListView = findViewById(R.id.txtEmptyListView);
            txtEmptyListView.setText(
                    TextUtils.makeTextPartBold(Constants.localMP3ListEmptyMsg, Constants.localTextToBold)
            );
            txtEmptyListView.setTypeface(mmfontface);
            mListView.setEmptyView(txtEmptyListView);

            registerForContextMenu(mListView);

            // Confirm delete dialog
            confirmDialog = new AlertDialog.Builder(this);
            confirmDialog
                    .setTitle("Delete")
                    .setMessage("Are you sure?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            MediaInfo mediaInfo = list.get(selectedIndex);
                            if (mediaInfo != null) {
                                File deleteFile = new File(mediaInfo.getPhysicallocation());
                                if (deleteFile.exists()) {
                                    boolean ok = deleteFile.delete();
                                    Log.d(TAG, "Delete local file: " + ok + " -> " + deleteFile.getAbsolutePath());
                                    // Also try to delete a matching MediaStore entry (Android 10+)
                                    deleteFromMediaStoreIfExists(deleteFile);
                                }
                            }
                            list.remove(selectedIndex);
                            mediaInfoAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e(TAG, "Delete error: " + Log.getStackTraceString(e));
                            Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null); // Do nothing on "No"

        } catch(Exception e){
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }

        Log.d(TAG,"[onCreate] End.");
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            list.clear();
            // Refresh from app-private Downloads
            // Utils.getListOfAllDownloadedMedia(Constants.supportedAudioFiles, list);

            Utils.getListFromAppPrivateDownloads(this, Constants.supportedAudioFiles, list);

            mediaInfoAdapter.notifyDataSetChanged();
        } catch(Exception e){
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
    }

    @Override
    public ListView getListView() {
        return mListView;
    }

    @Override
    public void getSwipeItem(boolean isRight, int position) {
        // no-op
    }

    @Override
    public void onItemClickListener(ListAdapter listAdapter, int position) {
        selectedIndex = position;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select action.");
        menu.add(0, v.getId(), 0, "Play");
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Share");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        try{
            String title = String.valueOf(item.getTitle());
            if ("Play".equals(title)) {
                MediaInfo mediaInfo = list.get(selectedIndex);
                if (mediaInfo != null) {
                    File openMediaFile = new File(mediaInfo.getPhysicallocation());
                    if (openMediaFile.exists()) {
                        Intent audioPlayer = new Intent(this, AudioPlayerActivity.class);
                        audioPlayer.setAction(Intent.ACTION_VIEW);
                        audioPlayer.putExtra("authorProfileImage", mediaInfo.getProfileimage());
                        audioPlayer.putExtra("authorName", mediaInfo.getAuthorname());
                        audioPlayer.putExtra("title", mediaInfo.getFilename());
                        audioPlayer.putExtra("openMediaFile", mediaInfo.getPhysicallocation());
                        startActivity(audioPlayer);
                    } else {
                        Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if ("Delete".equals(title)) {
                confirmDialog.show();
            } else if ("Share".equals(title)) {
                MediaInfo mediaInfo = list.get(selectedIndex);
                Utils.share(mediaInfo, this);
            } else {
                return false;
            }
        } catch(Exception e){
            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"[onDestroy] Start.");
        super.onDestroy();
        Log.d(TAG,"[onDestroy] End.");
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> LocalMP3Activity.this.finish())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Try to remove the public (Downloads) copy if it exists in MediaStore.
     * We match by DISPLAY_NAME (filename) and (if available) size.
     * Safe to no-op on pre-Android 10.
     */
    private void deleteFromMediaStoreIfExists(File file) {
        if (file == null || !file.exists()) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        try {
            String displayName = file.getName();
            long size = file.length();

            ContentResolver cr = getContentResolver();
            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            // Try to match by DISPLAY_NAME and SIZE to reduce false positives
            String selection;
            String[] selectionArgs;
            if (size > 0) {
                selection = MediaStore.Downloads.DISPLAY_NAME + "=? AND " + MediaStore.Downloads.SIZE + "=?";
                selectionArgs = new String[]{ displayName, String.valueOf(size) };
            } else {
                selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
                selectionArgs = new String[]{ displayName };
            }

            try (android.database.Cursor c = cr.query(
                    collection,
                    new String[]{ MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME, MediaStore.Downloads.SIZE },
                    selection,
                    selectionArgs,
                    null
            )) {
                if (c != null) {
                    while (c.moveToNext()) {
                        long id = c.getLong(0);
                        Uri item = ContentUris.withAppendedId(collection, id);
                        int rows = cr.delete(item, null, null);
                        Log.d(TAG, "Deleted MediaStore item rows=" + rows + " name=" + displayName + " size=" + size);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "MediaStore delete failed (safe to ignore): " + Log.getStackTraceString(e));
        }
    }
}
