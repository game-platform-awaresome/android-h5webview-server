package xysz.egret.com.third.tencent;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONObject;

import xysz.egret.com.xysz.view.Navigation;

/**
 * Created by yanjiaqi on 2017/2/16.
 */

public class Interface {
    Context context;
    WebView webView;
    public Interface(Context context,WebView webView){
        this.context = context;
        this.webView = webView;
        TencentMMManager.getInstance().setWebView(webView);
    }

    @JavascriptInterface
    public void loginWX(String jumpUrl){
        TencentMMManager tmm = TencentMMManager.getInstance();
        if(tmm.getApi() == null){
            tmm.init(context);
        }
        tmm.wxLogin(jumpUrl);
    }

    @JavascriptInterface
    public void payWX(String data){
        Log.d("++>>>", data);

        TencentMMManager tmm = TencentMMManager.getInstance();
        if(tmm.getApi() == null){
            tmm.init(context);
        }

        try {
            JSONObject jsonObject = new JSONObject(data);
            tmm.wxPay(jsonObject.getString("prepayId"), jsonObject.getString("nonceStr"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void shareWX(String data) {
        Log.d("++>>>shareWX ", data);

        TencentMMManager tmm = TencentMMManager.getInstance();
        if(tmm.getApi() == null) {
            tmm.init(context);
        }

        try {
            JSONObject jsonObject = new JSONObject(data);
            tmm.wxShare(jsonObject.getString("link"), jsonObject.getString("title"),
                    jsonObject.getString("desc"), jsonObject.getString("imgUrl"),
                    jsonObject.getString("type"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void setTitle(String title) {
        Log.d(">>>>>>>>>>>>>>>>>>>>>", title);

        Navigation navigation = Navigation.getInstance();
        if (navigation != null) {
            navigation.setTittle(title);
        }
    }

    @JavascriptInterface
    public boolean isWXInstalled(){
        TencentMMManager tmm = TencentMMManager.getInstance();
        if(tmm.getApi() == null){
            tmm.init(context);
        }
        return tmm.getApi().isWXAppInstalled();
    }

    @JavascriptInterface
    public void refreshToken(String refreshToken,String callBackFunc){
        TencentMMManager tmm = TencentMMManager.getInstance();
        if(tmm.getApi() == null){
            tmm.init(context);
        }
//        tmm.wxRefresh(refreshToken,callBackFunc);
    }
}
