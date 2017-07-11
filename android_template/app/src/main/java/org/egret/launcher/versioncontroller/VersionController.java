package org.egret.launcher.versioncontroller;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by egret0 on 6/16/17.
 */

public class VersionController {

    private static Context _context;

    public static void sendData(String data, Context context) {
        _context = context;

        sendDataToNative(data, context);
    }

    public static void pauseApp() {
        pauseAppNative();
    }

    public static void resumeApp() {
        resumeAppNative();
    }

    public static void httpPost(final String urlStr, final String data) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                Log.d("VersionController", urlStr);
//                Log.d("VersionController", data);

                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("POST");

                    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", data.length()+"");

                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data.getBytes());

                    int responseCode = connection.getResponseCode();
                    if  (responseCode ==200) {
                        InputStream inptStream = connection.getInputStream();
                        String str = dealResponseResult(inptStream);
//                        Log.d("VersionController>>>>>1", str);
                    } else {
                        InputStream inptStream = connection.getInputStream();
                        String str = dealResponseResult(inptStream);
//                        Log.d("VersionController>>>>>2", str);
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }

    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    public static String getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)_context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels/dm.xdpi,2);
        double y = Math.pow(dm.heightPixels/dm.ydpi,2);
        double screenInches = Math.sqrt(x+y);

        return ("" + screenInches);
    }

    public static String getRAMMemory() {
        ActivityManager am = (ActivityManager)_context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        return ("" + mi.totalMem + "," + mi.availMem);
    }

    private static native int sendDataToNative(String data, Context context);

    private static native void pauseAppNative();

    private static native void resumeAppNative();

    static {
        System.loadLibrary("versioncontroller");
    }
}
