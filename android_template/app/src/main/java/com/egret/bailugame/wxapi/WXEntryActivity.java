package com.egret.bailugame.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import xysz.egret.com.third.tencent.TencentMMManager;
import xysz.egret.com.xysz.R;

/**
 * Created by yanjiaqi on 2017/2/16.
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TencentMMManager
                .getInstance()
                .getApi()
                .handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        setIntent(intent);
//        if(getIntent()!=null){
//            TencentMMManager
//                    .getInstance()
//                    .getApi()
//                    .handleIntent(getIntent(), this);
//        }
    }

    @Override
    public void onReq(BaseReq baseReq) {

        Log.d("-->", "code " + "onReq");
    }

    @Override
    public void onResp(final BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            final String code = ((SendAuth.Resp) baseResp).code;
            Log.d("-->", "code " + code);
            TencentMMManager tmm = TencentMMManager.getInstance();
            tmm.addParams(true, tmm.getCallBackName(),"code", code);
        }
        else if (baseResp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {

            SendMessageToWX.Resp resp = (SendMessageToWX.Resp)baseResp;

            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                Log.d("-->", "share success");

                TencentMMManager tmm = TencentMMManager.getInstance();
                tmm.webView.loadUrl("javascript:window.DakaShareSuccess()");
            }
            else {
                Log.d("-->", "share failed");
            }
        }


//        Log.d("YANJIAQI","code:" + code);
//        TencentMMManager.getInstance().getAccessToken(code, new TencentMMManager.ITencentBack() {
//            @Override
//            public void onMessage(String msg) {
//                TencentMMManager tmm = TencentMMManager.getInstance();
//                tmm.invokeJS(
//                        tmm.getCallBackName(),
//                        msg);
//            }
//        });
        finish();

//        tmm.invokeJS(
//                tmm.getCallBackName(),
//                code);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
