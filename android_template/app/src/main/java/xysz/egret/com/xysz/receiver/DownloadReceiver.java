package xysz.egret.com.xysz.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import xysz.egret.com.xysz.ExecutorLab;
import xysz.egret.com.xysz.Log;
import xysz.egret.com.xysz.NetClass;

/**
 * Created by yanjiaqi on 16/4/26.
 */
public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("yanjiaqi","onReceive "+intent.getAction());
        String action = intent.getAction();
        if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,0);
            String apkName = context.getSharedPreferences("download",Context.MODE_PRIVATE).getString(String.valueOf(id),"");
            if(apkName != null && !"".equals(apkName)){
                File path = Environment.getExternalStoragePublicDirectory("");
                File apk = new File(path.getAbsolutePath()+File.separator+apkName);
                installApk(context,apk);
            }
        }
    }

    //安装apk
    private void installApk(Context context,File file){
        if(file.toString().endsWith(".apk")){
            Log.d("yanjiaqi","install "+file.getAbsolutePath());
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    private void send(){
        ExecutorLab.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
//                new NetClass().postRequest()
            }
        });
    }
}
