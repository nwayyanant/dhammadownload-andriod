package com.dhammadownload.dhammadownloadandroid.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.StorageLocation;
import com.dhammadownload.dhammadownloadandroid.entity.MediaInfo;

import java.util.ArrayList;

/**
 * Created by zawlinaung on 9/21/16.
 */
public class MediaInfoAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<MediaInfo> list = new ArrayList<MediaInfo>();
    private Context context;
    Typeface mmfontface;

    private static final String TAG = "MediaInfoAdapter";



    public MediaInfoAdapter(ArrayList<MediaInfo> list, Context context) {
        this.list = list;
        this.context = context;
        this.mmfontface = Typeface.createFromAsset(this.context.getAssets(), Constants.standardFont);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    //@Override
    public long getItemId(int pos) {

        return 0;
        //return list.get(pos).get;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        try{

                if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.media_info_view, null);
            }

            MediaInfo mediaInfo = list.get(position);
            if (mediaInfo !=null) {

                ImageView imgAuthor = (ImageView) view.findViewById(R .id.imgAuthorPhoto);

                if (mediaInfo.getProfileimage().trim() != "") {
                    Bitmap myBitmap = BitmapFactory.decodeFile(mediaInfo.getProfileimage());
                    imgAuthor.setImageBitmap(myBitmap);
                } else{
                    imgAuthor.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.dhammadownload_logo));
                }

                TextView txtAuthorName = (TextView)view.findViewById(R.id.txtAuthorName);
                txtAuthorName.setTypeface(mmfontface);
                //txtAuthorName.setTextSize(12);
                txtAuthorName.setText(mediaInfo.getAuthorname());
                //txtAuthorName.setText(String.valueOf(position) + " - " + mediaInfo.getAuthorname());

                TextView txtTitle = (TextView)view.findViewById(R.id.txtTitle);
                txtTitle.setTypeface(mmfontface);
                //txtTitle.setTextSize(12);
                txtTitle.setText(mediaInfo.getFilename());

                TextView txtStorageLocation = (TextView)view.findViewById(R.id.txtStorageLocation);
                txtStorageLocation.setTypeface(mmfontface);
                txtStorageLocation.setVisibility(View.GONE);
                if(mediaInfo.getStorageLocation()==StorageLocation.SDCARD){
                    txtStorageLocation.setVisibility(View.VISIBLE);
                    txtStorageLocation.setText("@ SD Card");
                }
            }

        }catch(Exception e){

            Log.e(TAG, "Exception: "+ Log.getStackTraceString(e));
        }
        return view;
    }

}