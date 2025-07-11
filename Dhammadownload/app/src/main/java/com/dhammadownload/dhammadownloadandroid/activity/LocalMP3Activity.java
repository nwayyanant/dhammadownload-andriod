package com.dhammadownload.dhammadownloadandroid.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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

/**
 * Created by zawlinaung on 9/15/16.
 */
@SuppressWarnings("deprecation")
public class LocalMP3Activity extends SwipeListViewActivity {

    private static final String TAG = "LocalMP3Activity";

    private ListView mListView;
    MediaInfoAdapter mediaInfoAdapter;
    Button btnEdit;
    //String appHomeFolder;
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
            mListView = (ListView) findViewById(R.id.listView1);
            list = new ArrayList<MediaInfo>();

            mmfontface = Typeface.createFromAsset(getAssets(), Constants.standardFont);

            txtHeader = (TextView) findViewById(R.id.txtHeader);
            txtHeader.setText(Constants.locaMP3ListHeader);
            txtHeader.setTypeface(mmfontface);

            //appHomeFolder = Utils.getDocumentDirectory() + "/" + Constants.mainFolder;
            //Utils.getListOfAllLocalDownloadedMedia(Constants.supportedAudioFiles, appHomeFolder, list);
            Utils.getListOfAllDownloadedMedia(Constants.supportedAudioFiles, list);

            mediaInfoAdapter = new MediaInfoAdapter(list, this);
            mListView.setAdapter(mediaInfoAdapter);

            txtEmptyListView=(TextView)findViewById(R.id.txtEmptyListView);
            txtEmptyListView.setText(
                    TextUtils.makeTextPartBold( Constants.localMP3ListEmptyMsg,
                            Constants.localTextToBold));
            txtEmptyListView.setTypeface(mmfontface);
            mListView.setEmptyView(txtEmptyListView);

            registerForContextMenu(mListView);

            //Put up the Yes/No message box
            confirmDialog = new AlertDialog.Builder(this);
            confirmDialog
                    .setTitle("Delete")
                    .setMessage("Are you sure?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            MediaInfo mediaInfo = (MediaInfo) list.get(selectedIndex);
                            if (mediaInfo != null) {

                                File deleteFile = new File(mediaInfo.getPhysicallocation());
                                if (deleteFile.exists()) {
                                    deleteFile.delete();
                                }
                            }
                            list.remove(selectedIndex);
                            mediaInfoAdapter.notifyDataSetChanged();

                        }
                    })
                    .setNegativeButton("No", null);                    //Do nothing on no

        } catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));

        }

        Log.d(TAG,"[onCreate] End.");
    }




    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    try{

        list.clear();
        //Utils.getListOfAllLocalDownloadedMedia(Constants.supportedAudioFiles,appHomeFolder,list);
        Utils.getListOfAllDownloadedMedia(Constants.supportedAudioFiles,list);
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
        //
    }

    @Override
    public void onItemClickListener(ListAdapter listAdapter,int position) {

        selectedIndex = position;

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select action.");
        menu.add(0,v.getId(), 0, "Play");//groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0,v.getId(),0,"Share");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        try{

                String openMediaFilePath="";

                if(item.getTitle()=="Play"){

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


                        }
                    }

                }
                else if(item.getTitle()=="Delete"){

                    confirmDialog.show();

                }else if(item.getTitle()=="Share"){

                    MediaInfo mediaInfo = list.get(selectedIndex);
                    Utils.share(mediaInfo,this);

                }else{
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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LocalMP3Activity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}
