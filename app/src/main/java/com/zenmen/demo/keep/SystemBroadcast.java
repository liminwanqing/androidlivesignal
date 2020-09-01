package com.zenmen.demo.keep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class SystemBroadcast extends BroadcastReceiver {

    private static final String TAG = "keep";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: ");

        GuardAppManager.getInstance().start(context);
    }
}
