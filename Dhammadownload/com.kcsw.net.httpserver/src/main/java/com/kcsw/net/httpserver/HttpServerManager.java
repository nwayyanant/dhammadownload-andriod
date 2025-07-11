package com.kcsw.net.httpserver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HttpServerManager {

    private Intent mServiceIntent;
    private HttpServerService mHttpServerService;
    private Context mCtx;

    public enum ServerStatus{RUNNING,STOP};

    private ServerStatus mServerStatus;
    public  ServerStatus getServerStatus(){
        return mServerStatus;
    }

    public String getServerIP(){
        return HttpServer.getServerIP();
    }

    private static  HttpServerManager ourInstance;

    public static HttpServerManager getInstance() {
        return ourInstance;
    }

    public static void initialize(Context context){
        if(ourInstance==null){
            ourInstance=new HttpServerManager(context);
        }
    }

    private HttpServerManager(Context context) {
        this.mCtx=context;
    }

    private Context getCtx(){
        return mCtx;
    }

    public void startServer(String rootFolder){

        mHttpServerService = new HttpServerService(getCtx());
        mServiceIntent = new Intent(getCtx(), mHttpServerService.getClass());
        if (!isMyServiceRunning(mHttpServerService.getClass())) {

            mServiceIntent.putExtra("ip", HttpServer.getLocalIpAddress());
            mServiceIntent.putExtra("rootpath",rootFolder);
            getCtx().startService(mServiceIntent);

            Log.i(HttpServerManager.class.getName(), "Server Running at" + HttpServer.getServerIP());
        }else{
            Log.i(HttpServerManager.class.getName(),"Server Already Running at" + HttpServer.getServerIP());
        }

        mServerStatus=ServerStatus.RUNNING;
    }

    public void stopServer(){
        if(mServiceIntent!=null) {
            getCtx().stopService(mServiceIntent);
            mServiceIntent=null;
            mServerStatus=ServerStatus.STOP;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getCtx().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i (HttpServerManager.class.getName(), "isMyServiceRunning?" + true+"");
                return true;
            }
        }
        Log.i (HttpServerManager.class.getName(),"isMyServiceRunning?" + false+"");
        return false;
    }
}
