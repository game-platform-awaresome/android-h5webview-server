package xysz.egret.com.third.tencent;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebView;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Formatter;

import xysz.egret.com.xysz.ContainerActivity;
import xysz.egret.com.xysz.ExecutorLab;
import xysz.egret.com.xysz.Log;
import xysz.egret.com.xysz.NetClass;
import xysz.egret.com.xysz.R;
import xysz.egret.com.xysz.view.HTML5WebView;

/**
 * Created by yanjiaqi on 2017/2/16.
 */

public class TencentMMManager {
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String REFRESH_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s";
    public static final int CALLING = 0X111111;
    public static final int STATE_1 = 0x111331;
    public static final int STATE_2 = 0x111332;
    public static final int NO_CALL = 0X222222;

//    int state = NO_CALL;
    String state_callback = null;

    IWXAPI api;
    Context context;
    public WebView webView;
    static TencentMMManager singleton;
    private TencentMMManager(){
    }

    public TencentMMManager init(Context context){
        this.context = context;
        api = WXAPIFactory.createWXAPI(context,Constants.APP_ID,true);
        api.registerApp(Constants.APP_ID);
        return this;
    }

    public TencentMMManager setWebView(WebView webView){
        this.webView = webView;
        return this;
    }

    public WebView getWebView(){
        return this.webView;
    }

    public void setCallBack(String callBackName){
        state_callback = callBackName;
    }

    public String getCallBackName(){
        String callBackName = state_callback;
        state_callback = null;
        return callBackName;
    }

    public static TencentMMManager getInstance(){
        if(singleton == null){
            singleton = new TencentMMManager();
        }
        return singleton;
    }

//    public boolean isCalling(){
//        return state == CALLING;
//    }

    private TencentMMManager sendAuthReq(){
//        if(state == NO_CALL){
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "wechat_sdk_demo_test";
            api.sendReq(req);
//        }
        return this;
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private TencentMMManager sendAuthReqForShare(String url, String title, String desc,
                                                 String imageUrl, String type){
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = url;

        WXMediaMessage msg = new WXMediaMessage((webpageObject));
        msg.title = title;
        msg.description = desc;

        URL imgUrl = null;
        Bitmap icon = null;
        try {
            imgUrl = new URL(imageUrl);
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

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (imageUrl.indexOf(".png") != -1) {
            icon.compress(Bitmap.CompressFormat.PNG, 50, stream);
        }
        else {
            icon.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        }

        msg.thumbData = stream.toByteArray();

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;

        if (type.equals("1")) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
        }
        else {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        }

        api.sendReq(req);

        return this;
    }

    public void addParams(boolean isParamsAdded,String baseUrl,Object... params){
        StringBuilder result = new StringBuilder(baseUrl);

        if(!isParamsAdded){
            result.append("?").append(params[0]).append("=").append(params[1]);
        }else{
            result.append("&").append(params[0]).append("=").append(params[1]);
        }

        String jumpStr = result.toString();
//        Log.d("YANJIAQI","jumpUrl="+jumpStr);
        ContainerActivity ca = (ContainerActivity)context;
        Message msg = ca.getHandler().obtainMessage(HTML5WebView.MSG_JAVA_CALL_JS);
        msg.obj = jumpStr;
        ca.getHandler().sendMessage(msg);
    }

    public void invokeJS(String function, Object... params){
        StringBuilder result = new StringBuilder();
        result.append("javascript:").append(function).append("(");
        for(int i=0 ;i < params.length ; i++){
            result.append("\"").append(params[i].toString()).append("\"");
            if(i < params.length - 1){
                result.append(",");
            }
        }
        result.append(")");

        String invokeStr = result.toString();

        ContainerActivity ca = (ContainerActivity)context;
        Message msg = ca.getHandler().obtainMessage(HTML5WebView.MSG_JAVA_CALL_JS);
        msg.obj = invokeStr;
        ca.getHandler().sendMessage(msg);
    }

//    public void getAccessToken(String code, final ITencentBack callBack){
//        final String url = String.format(ACCESS_TOKEN_URL,
//                Constants.APP_ID,
//                Constants.APP_SECRET,
//                code);
//        Log.d("YANJIAQI","url is:\n" + url);
//        ExecutorLab.getInstance().addTask(new Runnable() {
//            @Override
//            public void run() {
//                NetClass net = new NetClass();
//                String res = net.getRequest(url);
//                if(callBack != null){
//                    callBack.onMessage(res);
//                }
//                Log.d("YANJIAQI","response to"+url+"is:\n"+res);
//            }
//        });
//    }

//    public void getRefreshToken(String refreshToken, final ITencentBack callBack){
//        final String url = String.format(REFRESH_TOKEN_URL,
//                Constants.APP_ID,
//                refreshToken);
//        ExecutorLab.getInstance().addTask(new Runnable() {
//            @Override
//            public void run() {
//                NetClass net = new NetClass();
//                String res = net.getRequest(url);
//                if(callBack != null){
//                    callBack.onMessage(res);
//                }
//                Log.d("WX","response to" + url + "is:\n" + res);
//            }
//        });
//    }


    public TencentMMManager wxLogin(String jumpUrl){
        setCallBack(jumpUrl);
        sendAuthReq();
        return this;
    }

    private static String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }

    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    private String generateSign(String prepayId, String nonceStr, String timeStamp)  {
        String str1 = "appid=" + Constants.APP_ID + "&noncestr=" + nonceStr + "&package=Sign=WXPay"
                + "&partnerid=" + Constants.PARTNER_ID + "&prepayid=" + prepayId +
                "&timestamp=" + timeStamp;

        String str2 = str1 + "&key=" + Constants.KEY;

        android.util.Log.d("onPayFinish ", str2);
        String ret = stringToMD5(str2);
        return ret;
    }

    public void wxPay(String prepayId, String nonceStr) {
        PayReq req = new PayReq();
        req.appId = Constants.APP_ID;
        req.partnerId = Constants.PARTNER_ID;
        req.prepayId = prepayId;
        req.packageValue = "Sign=WXPay";
        req.nonceStr = nonceStr;

        long time = System.currentTimeMillis() / 1000;
        req.timeStamp = String.valueOf(time);

        req.sign = generateSign(prepayId, nonceStr, req.timeStamp);

        android.util.Log.d("onPayFinish ", req.sign);

        api.sendReq(req);
    }

    public void wxShare(String url, String title, String desc, String imageUrl, String type) {
        sendAuthReqForShare(url, title, desc, imageUrl, type);
    }

//    public TencentMMManager wxRefresh(String refreshToken, final String callBackFunc){
//        setCallBack(callBackFunc);
//        getRefreshToken(refreshToken, new ITencentBack() {
//            @Override
//            public void onMessage(String msg) {
//                String callBackName = getCallBackName();
//                TencentMMManager.getInstance().invokeJS(
//                        callBackName,
//                        msg);
//            }
//        });
//        return this;
//    }

    public IWXAPI getApi(){
        return api;
    }

//    public int getState(){
//        return state;
//    }

    public interface ITencentBack{
        void onMessage(String msg);
    }
}
