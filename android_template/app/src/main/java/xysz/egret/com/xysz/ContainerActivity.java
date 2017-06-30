package xysz.egret.com.xysz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieStore;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import xysz.egret.com.third.tencent.ShortcutInterface;
import xysz.egret.com.third.tencent.TencentMMManager;
import xysz.egret.com.xysz.view.HTML5WebView;
import xysz.egret.com.xysz.view.Navigation;

/**
 * Created by yanjiaqi on 16/1/12.
 */
public class ContainerActivity extends Activity{
    private static final String FLAG_SHORTCUT_URL = "gamecenter_url";
    private static final String FLAG_SHORTCUT_ICON = "qimi_icon";
    private static final String FLAG_SPLASH_IMG = "loading";
    String startUrl = null;
    String startUrlDomain = null;
    FrameLayout mContainer = null;
    HTML5WebView mWebView = null;
    String state = null;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case HTML5WebView.MSG_JAVA_CALL_JS:
                    if(mWebView != null && msg.obj != null){
//                        Log.d("YANJIAQI","state:" + state + ":" + (String)msg.obj);
                        mWebView.loadUrl((String)msg.obj);
                    }
                    break;
                case HTML5WebView.MSG_OTHER_PROTOCAL:
                    callWeixin((String) msg.obj);
                    break;
                case HTML5WebView.MSG_PAYMENT_ENTER:
                    setCookieForDomainDynamic(startUrl,startUrlDomain);
                    showBackBtn((Uri)msg.obj);
                    break;
                case HTML5WebView.MSG_PAYMENT_LEAVE:
                    hideBackBtn();
                    break;
                case HTML5WebView.MSG_URL_LOADED:
                    String urlKey = (String)msg.obj;
                    if(mWebView.callBacks.containsKey(urlKey)){
                        HTML5WebView.IUrlFinishLoaded callBack = mWebView.callBacks.get(urlKey);
                        callBack.onFinishLoad(urlKey);
                        mWebView.callBacks.remove(urlKey);
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("yanjiaqi","onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.main);
        //测试splash页
//        ((ImageView)findViewById(R.id.id_top_cover)).setVisibility(View.VISIBLE);
        mContainer = (FrameLayout)findViewById(R.id.id_top);
        if(mWebView == null){
            mWebView = new HTML5WebView(this,handler);
            mContainer.addView(mWebView);
        }
        startWithIntent(initSplashView());
//        startWithIntent(false);
        //创建快捷方式
        int shortcut_url_id = getResources().getIdentifier(FLAG_SHORTCUT_URL, "string", getPackageName());
        int shortcut_icon_id = getResources().getIdentifier(FLAG_SHORTCUT_ICON,"drawable",getPackageName());
        if(shortcut_url_id != 0 && shortcut_icon_id != 0){
            Parcelable icon = Intent.ShortcutIconResource.fromContext(this,shortcut_icon_id);
            String gamecenter_url = getResources().getString(shortcut_url_id);
            Uri uri = Uri.parse(gamecenter_url);
            addShortcut(icon, "奇米游戏", uri);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("yanjiaqi", "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        startWithIntent(initSplashView());
    }

    private boolean initSplashView(){
        int splash_id = getResources().getIdentifier(FLAG_SPLASH_IMG,"drawable",getPackageName());
        boolean isSplashShow = false;
        if(splash_id != 0){
            isSplashShow = true;
            ImageView iv_splash = ((ImageView)findViewById(R.id.id_top_cover));
            iv_splash.setImageResource(splash_id);
            iv_splash.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv_splash.setVisibility(View.VISIBLE);
        }
        return isSplashShow;
    }

    @Override
    protected void onResume() {
        Log.i("yanjiaqi", "onResume:" + startUrl);
        super.onResume();
        this.state = "resume";
//        TencentMMManager.getInstance()
//                .init(this)
//                .wxLogin("onReceive");
    }

    @Override
    protected void onPause() {
        Log.i("yanjiaqi", "onPause");
        super.onPause();
        saveCookieForDomainFromDynamic(startUrl, startUrlDomain);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.state = "stop";
    }

    private void rebootWebView(){
        if(mWebView == null){
            mWebView = new HTML5WebView(this);
            mContainer.addView(mWebView);
            setCookieForDomainDynamic(startUrl,startUrlDomain);
        }
        mWebView.loadUrl(startUrl);
    }

    private void deleteWebView(){
        mContainer.removeAllViews();
        //暂停网页
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mWebView.pauseTimers();
            mWebView.onPause();
            mWebView.destroy();
            mWebView = null;
        }
    }

    private void setCookieForDomainDynamic(String url,String domain){
        Iterator<String> it = Util.cookies.keySet().iterator();
        while(it.hasNext()) {
            String urlKey = it.next();
            if(urlKey.indexOf(domain)!= -1){
                CookieManager.getInstance().setCookie(url,
                        Util.cookies.get(urlKey) + ";Max-Age=157680000" + ";Domain=" + domain + ";Path=/");
            }
        }
    }

    private void saveCookieForDomainFromDynamic(String url,String domain){
        Iterator<String> it = Util.cookies.keySet().iterator();
        int index = 0;
        while(it.hasNext()) {
            String urlKey = it.next();
            if(urlKey.indexOf(domain)!= -1){
                getSharedPreferences("cookie",MODE_PRIVATE).edit().putString(
                        index + "_" + url, Util.cookies.get(urlKey) + ";Max-Age=157680000" + ";Domain=" + domain + ";Path=/").commit();
                index ++;
//                CookieManager.getInstance().setCookie(url,
//                        Util.cookies.get(urlKey)+";Max-Age=157680000"+";Domain="+domain+";Path=/");
            }
            getSharedPreferences("cookie",MODE_PRIVATE).edit().putInt(url+"_numbers=",index).commit();
        }
    }

    private void setCookieForDomainStatic(String url){
        SharedPreferences sp = getSharedPreferences("cookie", MODE_PRIVATE);
        int cookie_num = sp.getInt(url+"_numbers=",-1);
        if(cookie_num > -1){
            for(int i=0;i<cookie_num;i++){
                CookieManager.getInstance().setCookie(url, sp.getString(i+"_"+url,null));
            }
        }
    }

    private void startWithIntent(boolean isNeedCover){
        int url_id = getResources().getIdentifier("game_url", "string", getPackageName());
        String url = getResources().getString(url_id);
        Intent intent = getIntent();
        android.util.Log.d(">>>>> ", intent.toString());

        String message = intent.getStringExtra(ShortcutInterface.EXTRA_MESSAGE);
        android.util.Log.d(">>>>> ", "" + message);
        if(intent!=null && message!=null){
            url = message;

//            下面注释的是错的
//            //打开游戏中心
//            url = intent.getData().toString();
//
//            android.util.Log.d(">>>>> ", url);
//
//            //去掉egret协议头
//            url = "http"+url.substring(5);
//            if(!upload(Util.URL_STATISTICS_ICON_OPEN,wrapAsParamPair("url",url))){
//                return;
//            }
//            try{
//                url = url+"&dId="+URLEncoder.encode(Util.getAndroidId(this),"utf-8") +
//                        "&mac="+URLEncoder.encode(Util.getMacAddress(this),"utf-8") +
//                        "&imei="+URLEncoder.encode(Util.getIMEI(this),"utf-8");
//            }catch(Exception e){
//            }
////            playSplashAnim();
        }else{
            //打开游戏中心
            if(!upload(Util.URL_STATISTICS_GAME_OPEN, wrapAsParamPair("url", url))){
                return;
            }
            checkGameVersion();
        }
        String host = Uri.parse(url).getHost();
        String domain = host;
        if(host.contains(".")){
            domain = host.substring(host.indexOf("."));
        }
        Log.d("yanjiaqi","load url domain:"+domain);

//        startUrl = url + Util.getUrlParams(this);
        startUrl = url+(url.contains("?")? "&":"?") + Util.getUrlParams(this);

        android.util.Log.d(">>>>> >", startUrl);

        startUrlDomain = domain;
        setCookieForDomainStatic(startUrl);

//        Util.cookies.put(url, getSharedPreferences("cookie", MODE_PRIVATE).getString(url, null));
//        CookieManager.getInstance().setCookie(url, Util.cookies.get(url));

        //以下在生命周期函数中载入
        if(isNeedCover){
            mWebView.loadUrl(startUrl, new HTML5WebView.IUrlFinishLoaded() {
                @Override
                public void onFinishLoad(String url) {
                    ((ImageView)findViewById(R.id.id_top_cover)).setVisibility(View.GONE);
                }
            });
        }else{
            mWebView.loadUrl(startUrl);//
        }


//        downloadApk("http://10.0.2.53/egret/temp/touchTest/bin-release/native/160425174726/game_code_160425174726.zip");
//        downloadApk("https://www.baidu.com/link?url=XG2lixM7fY1lDNUgWFLukNxuZbJ3iZI8RMfLdgTCmyS8IBiTIk7Wz6ckNOLicQSuDUyf-gaZ8_-OCo5O3kbBrPrPNdX68amAYXkR23_UDqfypsBSMumm5nlMdQfTcxSF&wd=&eqid=dc1aec6e0006c06100000002571de43b");
    }
    /** 在桌面添加快捷方式
      * @param      icon         快捷方式图标
      * @param      name      快捷方式名称
      * @param      uri           快捷方式的intent Uri
     ****/
    public void addShortcut(Parcelable icon, String name, Uri uri){
        Intent intentAddShortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //添加名称
        intentAddShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        //添加图标
        intentAddShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        //不允许重复创建
        intentAddShortcut.putExtra("duplicate", false);
        //设置Launcher的Uri数据
//        Intent intentLauncher = new Intent("com.egret.action.openqimi");
//        intentLauncher.setAction(Intent.ACTION_MAIN);
//        intentLauncher.addCategory(Intent.CATEGORY_DEFAULT);
//        intentLauncher.setClassName(this, "ContainerActivity");
//        intentLauncher.setData(uri);

//        Intent intentLauncher = new Intent(Intent.ACTION_MAIN);
//        intentLauncher.setClassName("com.egret.yugongyishan", "com.egret.yugongyishan.ContainerActivity");
//        intentLauncher.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intentLauncher.setData(uri);

        // 设置关联程序
//        Intent intentLauncher = new Intent(Intent.ACTION_MAIN);
//        intentLauncher.setClass(ContainerActivity.this, ContainerActivity.class);
//        intentLauncher.addCategory(Intent.CATEGORY_LAUNCHER);
//        intentLauncher.setData(uri);
        Intent intentLauncher = new Intent();
        intentLauncher.setAction(Intent.ACTION_VIEW);
        intentLauncher.setData(uri);

        //添加快捷方式的启动方法
        intentAddShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intentLauncher);
        sendBroadcast(intentAddShortcut);
    }
    //唤起微信支付
    private void callWeixin(String url) {

        android.util.Log.d("_____", url);

        if (url == null || url.indexOf("weixin://") == -1) {
            return;
        }

        try {
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setData(Uri.parse(url));
            startActivity(it);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ContainerActivity.this);
            builder.setTitle("支付提示").setCancelable(false)
                    .setMessage("微信支付仅支持6.0.2 及以上版本，请将微信更新至最新版本")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    }).show();
        }
    }

    public boolean upload(String url,String extraParams){
        String charset = "utf-8";
        try{
            String params = "&dId="+URLEncoder.encode(Util.getAndroidId(this),charset) +
                    "&mac="+URLEncoder.encode(Util.getMacAddress(this),charset) +
                    "&imei="+URLEncoder.encode(Util.getIMEI(this),charset);
            final String urlStr = extraParams!=null?
                    url + params + "&"+extraParams:url + params;
//            uploadUrl = urlStr;
            ExecutorLab.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    NetClass net = new NetClass();
                    Log.d("yanjiaqi", "send:" + urlStr);
                    String ret = net.getRequest(urlStr);
                    Log.d("yanjiaqi", "return:" + ret);
                }
            });
        }catch(Exception e){
            Log.d("yanjiaqi",e.getMessage());
            AlertDialog.Builder builder = new AlertDialog.Builder(ContainerActivity.this);
            builder.setTitle("权限提示").setCancelable(false)
                    .setMessage("请手动打开所有权限!")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    }).show();
            return false;
        }
        return true;
    }

    public void downloadApk(String urlStr){
        Downloader.startDownloadApkWork(this,urlStr);
//        DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
//        Uri uri = Uri.parse(urlStr);
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//        request.setDescription("愚公移山新版本下载");
//        request.setDestinationInExternalPublicDir("","yugong.apk");
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setMimeType("application/vnd.android.package-archive");
//        request.setMimeType("application/apk");
        // 设置为可被媒体扫描器找到
//        request.allowScanningByMediaScanner();
//        // 设置为可见和可管理
//        request.setVisibleInDownloadsUi(true);
//        long reference = downloadManager.enqueue(request);
    }

    public String wrapAsParamPair(String key,String val){
        String ret = null;
        try {
            ret = key+"="+URLEncoder.encode(val,"utf-8");
        }catch(Exception e){
        }
        return ret;
    }

    public void checkGameVersion(){
        int game_id_id = getResources().getIdentifier("game_id", "string", getPackageName());
        String game_id = game_id_id != 0?getResources().getString(game_id_id):null;
        final String urlStr = Util.URL_UPDATE +
                    (game_id != null?
                wrapAsParamPair("id",game_id)+"&":"")+
                wrapAsParamPair("dId",URLEncoder.encode(Util.getAndroidId(this)))+"&"+
                wrapAsParamPair("mac",URLEncoder.encode(Util.getMacAddress(this)))+"&"+
                wrapAsParamPair("imei",URLEncoder.encode(Util.getIMEI(this)));
        ExecutorLab.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                NetClass net = new NetClass();
                Log.d("yanjiaqi", "send:" + urlStr);
                String ret = net.getRequest(urlStr);
                Log.d("yanjiaqi", "return:" + ret);
                checkGameVersionRet(ret);
            }
        });
    }

    private void checkGameVersionRet(String retVal){
        String VERSION = "version";
        String DATA = "data";
        String URL = "url";
        try{
            JSONObject obj = new JSONObject(retVal);
            if(obj.has(DATA)){
                JSONObject data = obj.getJSONObject(DATA);
                if(data.has(VERSION)){
                    Double targetVersion = Double.parseDouble(data.getString(VERSION));
                    PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(),0);
                    Double currentVersion = Double.parseDouble(pi.versionName);
                    if(currentVersion<targetVersion){
                        if(data.has(URL)){
                            downloadApk(data.getString(URL));
//                            downloadApk("http://img.open.egret.com/upload/ygys.apk");
                        }
                    }
                }
            }
        }catch (Exception e){
            Log.d("yanjiaqi",e.getMessage());
        }
    }

    public void showBackBtn(final Uri jumpUri){
        Button back_btn = (Button)findViewById(R.id.id_back_btn);
        back_btn.setVisibility(View.VISIBLE);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mWebView.loadUrl(startUrl);
                mWebView.loadUrl(jumpUri.toString());
            }
        });
    }

    public void hideBackBtn(){
        Button back_btn = (Button)findViewById(R.id.id_back_btn);
        back_btn.setVisibility(View.GONE);
        back_btn.setOnClickListener(null);
    }

    public void playSplashAnim(){
//        VideoView vv = (VideoView)findViewById(R.id.id_videoview);
//        vv.setVideoURI(Uri.parse("file:///android_asset/splash.3gp"));
//        mVideoView.setVideoURI(Uri.parse(“file:///android_asset/video.3gp”));
//        vv.setMediaController(null);
//        vv.requestFocus();
//        vv.start();
    }

    public void onUrlLoaded(String url){

    }

    /*
    * 返回文件选择
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (mWebView != null && requestCode == mWebView.FILECHOOSER_RESULTCODE) {
            if(mWebView.mUploadMessage!=null){
                Uri result = intent == null || resultCode != RESULT_OK ? null
                        : intent.getData();
                mWebView.mUploadMessage.onReceiveValue(result);
                mWebView.mUploadMessage = null;
            }
        }
    }

    public Handler getHandler(){
        return handler;
    }
}
