package com.kcsw.net.httpserver;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Chaw on 8/13/2018.
 */

public class HttpServerService extends Service {
    private final static String IntentServiceName="HttpServerService";

    HttpServer myServer;
    Context ctx;

    public HttpServerService(Context context){
        super();
        this.ctx=context;
    }

    public HttpServerService(){
        super();
    }



    @Override
    public int onStartCommand (Intent intent,
                               int flags,
                               int startId) {

        Log.i(HttpServerService.class.getName(),"HTTPServerService onStartCommand startId=" + String.valueOf(startId));

        Bundle b=intent.getExtras();
        String rootpath=b.getString("rootpath");

        File rootFile=new File(rootpath);

        try {
            myServer = new HttpServer(rootFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");

        if(myServer!=null) {
            myServer.stop();
        }

        //Intent broadcastIntent = new Intent("p2ptest.kcsw.com.p2ptest.HttpServerServiceRestarterBroadcastReceiver");
        //sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent i){
        return null;
    }




}
