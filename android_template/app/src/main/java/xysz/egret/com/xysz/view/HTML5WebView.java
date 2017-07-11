package xysz.egret.com.xysz.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
//import android.util.Log;
import xysz.egret.com.third.tencent.Interface;
import xysz.egret.com.third.tencent.ShortcutInterface;
import xysz.egret.com.xysz.ContainerActivity;
import xysz.egret.com.xysz.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

import xysz.egret.com.xysz.Util;


public class HTML5WebView extends WebView {

    public static final int MSG_JAVA_CALL_JS = 44;
    public static final int MSG_PAYMENT_ENTER = 45;
    public static final int MSG_PAYMENT_LEAVE = 46;
    public static final int MSG_OTHER_PROTOCAL = 47;
    public static final int MSG_URL_LOADED = 48;
    public static final int FILECHOOSER_RESULTCODE = 117109;

    private boolean isCacheing = false;
    public Context mContext;
    private MyWebChromeClient mWebChromeClient;
    public MyWebViewClient mWebViewClient;
    private View mCustomView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback 	mCustomViewCallback;
    public ValueCallback<Uri> mUploadMessage;

    public Uri lastUri = null;
    public Map<String,IUrlFinishLoaded> callBacks = new HashMap<>();
    public Handler handler;
//    private FrameLayout	mContentView;
//    private FrameLayout	mBrowserFrameLayout;
//    private FrameLayout	mLayout;

    static final String LOGTAG = "HTML5WebView";

    private void init(Context context) {
        mContext = context;
//        mLayout = new FrameLayout(context);
//
//        mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(mContext).inflate(getRID("custom_screen","layout"), null);
//        mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(getRID("main_content","id"));
//        mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(getRID("fullscreen_custom_content","id"));

//        mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

        mWebChromeClient = new MyWebChromeClient(this);
        setWebChromeClient(mWebChromeClient);
        mWebViewClient = new MyWebViewClient(this);
        setWebViewClient(mWebViewClient);
        
        // Configure the webview
        WebSettings s = getSettings();
        s.setBuiltInZoomControls(false);
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSavePassword(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);

        // enable navigator.geolocation
        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");

        // enable Web Storage: localStorage, sessionStorage
        s.setDomStorageEnabled(true);
//        mContentView.addView(this);
        this.addJavascriptInterface(new Interface(context,this),"dakaGameCenter");
        this.addJavascriptInterface(new ShortcutInterface(context), "shortcut");
    }
    public HTML5WebView(Context context,Handler handler){
        super(context);
        this.handler = handler;
        init(context);
    }
    public HTML5WebView(Context context) {
        super(context);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void URLFilterAction(String url){
        if(handler != null){
            Message msg = new Message();
            msg.what = MSG_URL_LOADED;
            msg.obj = url;
            handler.sendMessage(msg);
        }
        Uri uri = Uri.parse(url);
        if(lastUri != null){
            Log.d("yanjiaqi","last:"+lastUri.getHost()+" current:"+uri.getHost());
            if(!"web.iapppay.com".equals(lastUri.getHost()) && "web.iapppay.com".equals(uri.getHost())){
                //进入支付页
                if(handler != null){
                    Message msg = new Message();
                    msg.what = MSG_PAYMENT_ENTER;
                    msg.obj = lastUri;
                    handler.sendMessage(msg);
                }
            }else
            if("web.iapppay.com".equals(lastUri.getHost()) && !"web.iapppay.com".equals(uri.getHost())){
                //离开支付页
                if(handler != null){
                    handler.sendEmptyMessage(MSG_PAYMENT_LEAVE);
                }
            }
        }
        lastUri = uri;
    }

    public void loadUrl(String url,IUrlFinishLoaded callBack){
        this.callBacks.put(url,callBack);
        this.loadUrl(url);
    }

    //一次只能cache一个网页
    public boolean loadCacheUrl(String url){
        if(!isCacheing){
            isCacheing = true;
//            this.startTimeOutCounter();
            this.loadUrl(url);
            return true;
        }
        return false;
    }


    public Runnable getTimeOutCounterRunnable(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        //超时等待三分钟
                        wait(180000);
                    }
                    if(isCacheing && handler!= null){
//                    Log.d("ReactWebActivity","Time Out Error url:"+HTML5WebView.this.getUrl());
                        Message message = new Message();
                        message.what = 3;
                        message.obj = "网页加载超时,请检查网络后尝试!";
                        handler.sendMessage(message);
                        isCacheing = false;
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        return runnable;
//        if(handler!=null){
//            handler.post(runnable);
//        }
    }
//    public FrameLayout getLayout() {
//        return mLayout;
//    }

    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((mCustomView == null) && canGoBack()){
                goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebChromeClient extends WebChromeClient {
        private Bitmap 		mDefaultVideoPoster;
        private View 		mVideoProgressView;
        private HTML5WebView host;
        public MyWebChromeClient(HTML5WebView webView){
            this.host = webView;
        }
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback)
        {
            //Log.i(LOGTAG, "here in on ShowCustomView");
            HTML5WebView.this.setVisibility(View.GONE);

            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomViewContainer.addView(view);
            mCustomView = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);
        }

        @Override
        public void onHideCustomView() {

            if (mCustomView == null)
                return;

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            mCustomViewContainer.removeView(mCustomView);
            mCustomView = null;
            mCustomViewContainer.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();

            HTML5WebView.this.setVisibility(View.VISIBLE);

            //Log.i(LOGTAG, "set it to webVew");
        }

        @Override
        public Bitmap getDefaultVideoPoster() {
            //Log.i(LOGTAG, "here in on getDefaultVideoPoster");
            if (mDefaultVideoPoster == null) {
                mDefaultVideoPoster = BitmapFactory.decodeResource(
                        getResources(), getRID("default_video_poster","drawable"));
            }
            return mDefaultVideoPoster;
        }

        @Override
        public View getVideoLoadingProgressView() {
            //Log.i(LOGTAG, "here in on getVideoLoadingPregressView");

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                mVideoProgressView = inflater.inflate(getRID("video_loading_progress","layout"), null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
//            ((Activity) mContext).setTitle(title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
//            ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress*100);
                if(host != null && host.isCacheing && host.handler!= null){
                    if(newProgress == 100){
                        Log.d("ReactWebActivity","loaded url:"+host.getUrl());
                        Message message = new Message();
                        message.what = 1;
                        message.obj = host.getUrl();
                        host.handler.sendMessage(message);
                        //封闭TimeOut的执行
                        host.isCacheing = false;
                    }else{
                        Message message = new Message();
                        message.what = 1;
                        message.obj = newProgress;
                        host.handler.sendMessage(message);
                    }
                }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            ((ContainerActivity)mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            ((ContainerActivity)mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            ((ContainerActivity)mContext).startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        HTML5WebView host;
        public Navigation navigation;

        public MyWebViewClient(HTML5WebView webView) {
            host = webView;
            navigation = new Navigation(webView.mContext, webView);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            CookieManager cookieManager = CookieManager.getInstance();
            String CookieStr = cookieManager.getCookie(url);
            Log.e("yanjiaqi","url="+url);
            Log.e("yanjiaqi", "Cookies = " + CookieStr);
            Util.cookies.put(url, CookieStr);
            //处理url
            URLFilterAction(url);
            super.onPageFinished(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view,url);

            navigation.onUrlChanged(view.getUrl());

            Log.e("yanjiaqi","resource="+url);
        }

            @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(LOGTAG, "shouldOverrideUrlLoading: "+url);
            String httpHead = url.substring(0, 5).toString();
            if (!httpHead.equals("http:")
                    && !httpHead.equals("https")) {
                if(handler != null){
                    Message msg = new Message();
                    msg.what = MSG_OTHER_PROTOCAL;
                    msg.obj = url;
                    handler.sendMessage(msg);
                    return true;
                }
            }
            // don't override URL so that stuff within iframe can work properly
            // view.loadUrl(url);
            String loadUrl = getUrl();
            if(callBacks.containsKey(loadUrl)){
                IUrlFinishLoaded obj = callBacks.get(loadUrl);
                callBacks.remove(obj);
                callBacks.put(url,obj);
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if(host != null && host.isCacheing && host.handler!= null){
                Log.d("ReactWebActivity","onReceivedError url:"+host.getUrl());
                Message message = new Message();
                message.what = 3;
                message.obj = error.toString();
                host.handler.sendMessage(message);
                host.isCacheing = false;
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if(host != null && host.isCacheing && host.handler!= null){
                Log.d("ReactWebActivity","onReceivedHttpError url:"+host.getUrl());
                Message message = new Message();
                message.what = 3;
                message.obj = errorResponse.toString();
                host.handler.sendMessage(message);
                host.isCacheing = false;
            }
        }
    }

    static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    private int getRID(String name,String type){
        return mContext.getResources().getIdentifier(name,type,mContext.getPackageName());
    }

    public interface IUrlFinishLoaded{
        public void onFinishLoad(String url);
    }
}