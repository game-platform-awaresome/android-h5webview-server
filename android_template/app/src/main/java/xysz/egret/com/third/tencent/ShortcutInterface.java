package xysz.egret.com.third.tencent;

/**
 * Created by egret0 on 5/5/17.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import xysz.egret.com.xysz.ContainerActivity;

public class ShortcutInterface {

    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    public static final String EXTRA_MESSAGE = "egret_webView_extra_message";

    private Context context;

    public ShortcutInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void createShortcut(String json){
        Log.d(">>>>>", json);

        String name = "Egret";
        String iconUrl = "";
        String url = "";

        try {
            JSONObject jsonObj = new JSONObject(json);
            name = jsonObj.getString("name");
            iconUrl = jsonObj.getString("icon");
            url = jsonObj.getString("url");
            Log.d(">>>>>", name);
            Log.d(">>>>>", iconUrl);
            Log.d(">>>>>", url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        URL imgUrl = null;
        Bitmap icon = null;
        try {
            imgUrl = new URL(iconUrl);
            HttpURLConnection urlConn = (HttpURLConnection) imgUrl
                    .openConnection();
            urlConn.setDoInput(true);
            urlConn.connect();
            InputStream is = urlConn.getInputStream();
            icon = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(this.context, ContainerActivity.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launcherIntent.putExtra(EXTRA_MESSAGE, url);

        Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
        addShortcutIntent.putExtra("duplicate", false);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        context.sendBroadcast(addShortcutIntent);
    }

}
