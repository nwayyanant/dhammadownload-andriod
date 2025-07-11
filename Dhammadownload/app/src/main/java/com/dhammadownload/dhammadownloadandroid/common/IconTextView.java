package com.dhammadownload.dhammadownloadandroid.common;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.appcompat.widget.AppCompatTextView;

import com.dhammadownload.dhammadownloadandroid.R;

public class IconTextView extends AppCompatTextView {

    private Context context;

    public IconTextView(Context context) {
        super(context);
        this.context = context;
        createView();
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        createView();
    }

    private void createView(){
        setGravity(Gravity.CENTER);

        Typeface textTypeFace=Typeface.createFromAsset(getContext().getAssets(), getResources().getString(R.string.fontPath));

        setTypeface(textTypeFace);
    }
}
