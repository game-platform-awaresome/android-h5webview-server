package xysz.egret.com.xysz;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by yanjiaqi on 16/4/25.
 */
public class Downloader{

        public static boolean isSDCardAvailable() {
            if (android.os.Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED)) {
                return true;
            }
            return false;
        }

        public static int getAndroidSDKVersion() {
            int version = 0;
            try {
                version = Integer.valueOf(android.os.Build.VERSION.SDK);
            } catch (NumberFormatException e) {
            }
            return version;
        }

        public static void startDownloadApkWork(Context context, String downloadUrl){
            if(isSDCardAvailable() && getAndroidSDKVersion() >= 9){
                // 调用系统的下载管理器
                startDownloadApkBySystemDownloadManager(context, downloadUrl);
            } else {
                startDownloadApkByBrowser(context, downloadUrl);
            }
        }

        /**
         * 通过浏览器来进行下载
         * */
        public static void startDownloadApkByBrowser(Context context, String downloadUrl){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(downloadUrl));
            context.startActivity(intent);
        }

        /**
         * android2.3以后调用系统的下载管理程序
         * */
        public static void startDownloadApkBySystemDownloadManager(Context context, String downloadUrl){
//		Log.e("url", downloadUrl);
            try {
//			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();

                DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(uri);

                //设置允许使用的网络类型，这里是移动网络和wifi都可以
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                //禁止发出通知，既后台下载，如果要使用这一句必须声明一个权限：android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
                //request.setShowRunningNotification(false);

                // 发出通知，既后台下载
                request.setShowRunningNotification(true);

                // 显示下载界面
                request.setVisibleInDownloadsUi(true);
		    /*设置下载后文件存放的位置,如果sdcard不可用，那么设置这个将报错，因此最好不设置如果sdcard可用，下载后的文件
		     * 在/mnt/sdcard/Android/data/packageName/files目录下面，
		     * 如果sdcard不可用,设置了下面这个将报错，不设置，下载后的文件在/cache这个  目录下面
		     * */
                StringBuffer apkName = new StringBuffer();
                apkName.append(System.currentTimeMillis()).append(".apk");

//		    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + Constants.DK_SINGLE_DOWNLOAD_PATH, apkName.toString());
                request.setDestinationInExternalPublicDir("", apkName.toString());

                long id = downloadManager.enqueue(request);
                context.getSharedPreferences("download",Context.MODE_PRIVATE).edit().putString(String.valueOf(id),apkName.toString()).commit();
//                SharePreferenceUtil.getInstance(context).saveString(String.valueOf(id), apkName.toString());
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                startDownloadApkByBrowser(context, downloadUrl);
            }
        }



//        /**
//         * 跳转到浏览器h5的推荐页面
//         * */
//        public static void startRecommendPageByBrowser(Context context, String url){
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            StringBuffer completeUrl = new StringBuffer();
//            completeUrl.append(url).append("?")
//                    .append(Constants.PARAM_DK_SINGLE_FR).append("=").append(Constants.DK_SINGLE_FR).append("&")
//                    .append(Constants.PARAM_DK_SINGLE_GV).append("=").append(PhoneUtil.getGameVersionCode(context)).append("&")
//                    .append(Constants.PARAM_DK_SINGLE_SV).append("=").append(Constants.SDK_VERSION).append("&")
//                    .append(Constants.JSON_CHANNEL).append("=").append(DKSingleSDKSettings.SDK_CHANNELID).append("&")
//                    .append(Constants.JSON_APPID).append("=").append(DKSingleSDKSettings.SDK_APPID);
//
//            intent.setData(Uri.parse(completeUrl.toString()));
//            context.startActivity(intent);
//        }
}
