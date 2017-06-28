package com.egret.bailugame.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import xysz.egret.com.third.tencent.Constants;

/**
 * Created by egret0 on 6/7/17.
 */

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "MicroMsg.SDKSa";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Log.d("-->", "code " + "onReq1");
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d("-->", "code " + "resp1");
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
        Log.d(TAG, "onPayFinish, errStr = " + resp.errStr);

        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            finish();
        }
    }
}
