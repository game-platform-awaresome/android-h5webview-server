package xysz.egret.com.xysz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.webkit.ValueCallback;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanjiaqi on 16/4/24.
 */
public class Util {
    public static boolean DEBUG = false;
    public static Map<String, String> cookies = new HashMap<>();
    public static String URL_STATISTICS_ICON_OPEN = "http://gameanalysis.egret.com/qimiAppStat.php?act=pushGameIcon";
    public static String URL_STATISTICS_GAME_OPEN = "http://gameanalysis.egret.com/qimiAppStat.php?act=installGame";
    public static String URL_STATISTICS_INSTALL = "http://gameanalysis.egret.com/downStat.php";
    public static String URL_UPDATE = "http://gamecenter.egret-labs.org/Api.adversion?";
    public static String getMacAddress(Context context){
        String macAddress = null;
        WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
        if (null != info) {
            macAddress = info.getMacAddress();
        }
        Log.d("yanjiaqi", "mac:" + macAddress);
        if(macAddress == null){
            macAddress = "";
        }
        return macAddress;
    }

    public static String getIMEI(Context context) {
        String s = "";
        TelephonyManager telephonymanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonymanager != null) {
            s = telephonymanager.getDeviceId();
            if(s == null){
                s = "";
            }
        }
        Log.d("yanjiaqi", "imei:" + s);
        return s;
    }

    public static String getAndroidId(Context context) {
        String s = "";
        s = android.provider.Settings.Secure.getString(context.getContentResolver(), "android_id");
        if(s == null){
            s = "";
        }
        Log.d("yanjiaqi", "did:" + s);
        return s;
    }

    public static String getCurrentNetType(Context context) {
        String type = "";

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        if (packageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, packageName) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null) {
                type = "null";
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                type = "wifi";
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int subType = info.getSubtype();
                if (subType == TelephonyManager.NETWORK_TYPE_CDMA
                        || subType == TelephonyManager.NETWORK_TYPE_GPRS
                        || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                    type = "2g";
                } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS
                        || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                    type = "3g";
                } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                    type = "4g";
                }
            }
        }

        return type;
    }

    public static String[] getSimOperatorAndIMSI(Context context) {
        String[] ret = {"null", "0"};
        if (context == null) {
            return ret;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        if (packageManager.checkPermission(
                Manifest.permission.READ_PHONE_STATE, packageName) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                ret[0] = tm.getSimOperator();
            }
            ret[1] = tm.getSubscriberId();
        }
        return ret;
    }

    public static String getUrlParams(Context context) {
        String network = getCurrentNetType(context);
        String brand = android.os.Build.BRAND;
        String model = android.os.Build.MODEL;
        String deviceId = getAndroidId(context);
        String imei = getIMEI(context);

        String[] simInfo = getSimOperatorAndIMSI(context);
        String imsi = simInfo[1];
        String mac = getMacAddress(context);
        String ret = "network=" + network + "&brand=" + brand + "&model=" + model +
                "&deviceId=" + deviceId + "&imei=" + imei + "&imsi=" + imsi +
                "&mac=" + mac + "&wd=1";
//        ret += "&appId=1020&adFrom=jrttapp";
        ret = ret.replaceAll(" ", "_");
        return ret;
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 返回当前SDK版本
     * @return
     */
    public static int getAndroidSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

}
