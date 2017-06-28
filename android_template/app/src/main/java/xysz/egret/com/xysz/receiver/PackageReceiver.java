package xysz.egret.com.xysz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import xysz.egret.com.xysz.Log;

/**
 * Created by jkd2972 on 2017/6/26.
 */

public class PackageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("yanjiaqi","onReceive "+intent.getAction());
        //接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            System.out.println("安装了:" +packageName + "包名的程序");
        }
        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            System.out.println("卸载了:"  + packageName + "包名的程序");
        }
    }
}
