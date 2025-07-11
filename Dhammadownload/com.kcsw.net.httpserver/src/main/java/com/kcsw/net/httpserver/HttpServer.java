package com.kcsw.net.httpserver;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

/**
 * Created by Chaw on 8/6/2018.
 */

public class HttpServer extends SimpleWebServer {
    private final static int PORT = 8080;

    public  static  String getServerIP() {
        return serverIP;
    }

    private  final  static String serverIP=getLocalIpAddress();
    //private  final  static File f= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/"+"dhammadownload.com");

    public HttpServer(File f) throws IOException {

        super(serverIP,PORT,f,false);


        start();
        System.out.println( "\nRunning! Point your browers to http://" + serverIP + ":" + String.valueOf(PORT) + "\n" );
    }

//    @Override
//    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        msg += "<p>We serve " + session.getUri() + " !</p>";
//        return newFixedLengthResponse( msg + "</body></html>\n" );
//    }


    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip=inetAddress.getHostAddress().toString();
                        if(ip.length()<15 && ip.startsWith("192.")) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

}
