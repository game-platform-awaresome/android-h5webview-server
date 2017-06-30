package xysz.egret.com.xysz.view;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import xysz.egret.com.third.tencent.TencentMMManager;
import xysz.egret.com.xysz.R;
import xysz.egret.com.xysz.Util;

/**
 * Created by egret0 on 6/7/17.
 */

public class Navigation {

    private HTML5WebView webView;
    private Context mContext;
    private String currentUrl;
    private String lastUrl;
    private RelativeLayout relativeLayout;

    private Button btn_back;
    private Button btn_reload;
    private Button btn_share;
    private TextView tittle;

    private final String homeUrl = "http://wan.yichi666.com/go.php";
    private final String homeUrl1 = "http://wan.yichi666.com/games";
    private final String homeUrl2 = "http://wan.yichi666.com/activity";
    private final String homeUrl3 = "http://wan.yichi666.com/mine";

    private static Navigation _instance = null;

    private String _startUrl = "";

    public static Navigation getInstance() {
        return _instance;
    }

    public Navigation(final Context context, HTML5WebView html5WebView) {
        webView = html5WebView;
        mContext = context;
        currentUrl = "";

        relativeLayout = (RelativeLayout)((Activity)context).findViewById(R.id.nav_layout);

        btn_back = (Button)relativeLayout.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("++> ", "back");

                webView.loadUrl(_startUrl);
//                if (lastUrl.indexOf("appId") != -1) {
//                    webView.loadUrl(lastUrl.substring(0, lastUrl.indexOf("&appId")));
//                }
//                else {
//                    webView.goBack();
//                }
            }
        });

        btn_reload = (Button)relativeLayout.findViewById(R.id.btn_reload);
        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("++> ", "reload");

                webView.reload();
            }
        });

        btn_share = (Button)relativeLayout.findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("++> ", "share");

                webView.loadUrl("javascript:window.DaKaShareWX()");
            }
        });

        tittle = (TextView)relativeLayout.findViewById(R.id.text_tittle);

        _instance = this;
        _startUrl = "http://wan.yichi666.com/go.php?" + Util.getUrlParams(context);

        relativeLayout.post(new Runnable() {
            @Override
            public void run() {
                WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                int screenWidth = wm.getDefaultDisplay().getWidth();
                tittle.setMaxWidth(screenWidth - 500);
                tittle.setMaxLines(1);
//                Log.d("______", "" + tittle.getMaxWidth());
            }
        });
    }

    public void onUrlChanged(String url) {
        if (url == null) {
            Log.w("-->", "url is null");
            return;
        }

        if (url.equals(currentUrl)) {
            return;
        }

        lastUrl = currentUrl;
        currentUrl = url;
        Log.d("++> ", url);

        if (url.indexOf(homeUrl) != -1 || url.indexOf(homeUrl1) != -1 ||
                url.indexOf(homeUrl2) != -1 || url.indexOf(homeUrl3) != -1) {
            btn_back.setVisibility(View.INVISIBLE);
        }
        else {
            btn_back.setVisibility(View.VISIBLE);
        }
    }

    public void setTittle(final String titleStr) {
        relativeLayout.post(new Runnable() {
            @Override
            public void run() {
                tittle.setText(titleStr);
//                tittle.setText("ABCDEFGHIJKLMNOPQRSTUVWXTZ");
            }
        });
    }

}
