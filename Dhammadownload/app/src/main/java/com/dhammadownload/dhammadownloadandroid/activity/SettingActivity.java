package com.dhammadownload.dhammadownloadandroid.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dhammadownload.dhammadownloadandroid.R;
import com.dhammadownload.dhammadownloadandroid.common.Constants;
import com.dhammadownload.dhammadownloadandroid.common.SettingManager;
import com.dhammadownload.dhammadownloadandroid.common.StorageLocation;
import com.dhammadownload.dhammadownloadandroid.common.Utils;

import java.net.MalformedURLException;

public class SettingActivity extends Activity implements AdapterView.OnItemSelectedListener,View.OnFocusChangeListener {
    private static final String TAG = "SettingActivity";

    Spinner spinner;
    TextView txtStorageLocationSettingLabel;
    TextView txtSDCardStorageWarningMsg;

    TextView txtHomePageLabel;
    EditText txtHomePage;

    Typeface mmfontface;

    Button btnShare;

    LinearLayout llMainPageSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        mmfontface =Typeface.createFromAsset(getAssets(), Constants.standardFont);

        //Home Page
        txtHomePageLabel=(TextView)findViewById(R.id.txtHomePageLabel);
        txtHomePageLabel.setTypeface(mmfontface);
        txtHomePageLabel.setText("Main Page");

        txtHomePage=(EditText) findViewById(R.id.txtHomePage);
        txtHomePage.setTypeface(mmfontface);
        txtHomePage.setText(SettingManager.getInstance().getHomePage());
        txtHomePage.setOnFocusChangeListener(this);
        txtHomePage.setVisibility(View.VISIBLE);

        txtStorageLocationSettingLabel=(TextView)findViewById(R.id.txtStorageLocationSettingLabel);
        txtStorageLocationSettingLabel.setTypeface(mmfontface);

        txtSDCardStorageWarningMsg=(TextView)findViewById(R.id.txtSDCardStorageWarningMsg);
        txtSDCardStorageWarningMsg.setTypeface(mmfontface);
        txtSDCardStorageWarningMsg.setText(Constants.SDCardStorageWarningMsg);

        spinner = (Spinner) findViewById(R.id.storageLocations_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.storageLocations_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinner.setSelection(SettingManager.getInstance().getStorageLocation().ordinal(),true);

//        TelephonyManager tMgr = (TelephonyManager)SettingActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
//        String mPhoneNumber = tMgr.getLine1Number();
//        txtStorageLocationSettingLabel.setText(txtStorageLocationSettingLabel.getText() +
//                ((mPhoneNumber==null)? "null":mPhoneNumber)
//        );


        llMainPageSettings=(LinearLayout) findViewById(R.id.llMainPageSettings);

        btnShare=(Button) findViewById(R.id.btnShare);
        btnShare.setOnClickListener(buttonListener);

        llMainPageSettings.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {

        Log.d(TAG,"[onResume] Start.");
        super.onResume();  // Always call the superclass method first

        spinner.setSelection(SettingManager.getInstance().getStorageLocation().ordinal(),true);

        Log.d(TAG,"[onResume] End.");

    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==btnShare.getId()){
                    gotoServerActivity();
                }
            }
        };

    private void gotoServerActivity(){
        Intent intent=new Intent(this,ServerActivity.class);
        startActivity(intent);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        StorageLocation storageLocation;
        switch (pos){
            case 0: SettingManager.getInstance().setStorageLocation(StorageLocation.DEVICE);; break;
            case 1: sdCardOptionSelected(); break;
                default: SettingManager.getInstance().setStorageLocation(StorageLocation.DEVICE);
        }

        if(SettingManager.getInstance().getStorageLocation()==StorageLocation.SDCARD){
            txtSDCardStorageWarningMsg.setVisibility(View.VISIBLE);
        }else{
            txtSDCardStorageWarningMsg.setVisibility(View.INVISIBLE);
        }
    }

    private  void sdCardOptionSelected(){
        boolean isSDCardAvailable=Utils.isSDCardAvailable();
        if(!isSDCardAvailable){
            AlertDialog.Builder msgDialog = new AlertDialog.Builder(SettingActivity.this);
            msgDialog
                    .setTitle("Setting")
                    .setMessage(Constants.SDCardNotWritableMsg)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            spinner.setSelection(StorageLocation.DEVICE.ordinal(),false);
                        }
                    });

            msgDialog.show();
        }else{
            SettingManager.getInstance().setStorageLocation(StorageLocation.SDCARD);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void onFocusChange(View v, boolean hasFocus){
        if(v.getId()==txtHomePage.getId()){
            if(hasFocus==false){
                setHomePage();

            }
        }
    }

    private void setHomePage(){
        try {
            SettingManager.getInstance().setHomePage(txtHomePage.getText().toString());
        }catch (MalformedURLException ex){
            ex.printStackTrace();

            AlertDialog.Builder msgDialog = new AlertDialog.Builder(SettingActivity.this);
            msgDialog
                    .setTitle("Setting")
                    .setMessage("Please enter valid URL.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            txtHomePage.setText(SettingManager.getInstance().getHomePage());//Reset to previous value
                        }
                    });

            msgDialog.show();

        }
    }
}
