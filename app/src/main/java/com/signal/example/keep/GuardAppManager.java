package com.signal.example.keep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

public class GuardAppManager {
    private static final String TAG = "keep";
    private GuardAppManager(){}
    private static volatile GuardAppManager mInstance;
    private BootCompleteReceiver mReceiver;

    public static GuardAppManager getInstance(){
        if (mInstance == null){
            synchronized (GuardAppManager.class){
                if (mInstance == null){
                    return new GuardAppManager();
                }
            }
        }
        return mInstance;
    }
    public class BootCompleteReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG,"ACTION_SCREEN_OFF");
                DemoActivity.startDaemon();
            }
            else if(action.equals(Intent.ACTION_SCREEN_ON)){
                Log.i(TAG,"ACTION_SCREEN_ON");
                DemoActivity.stopDaemon();
            } else {
                Log.i(TAG, action);
            }
        }
    }
    /**
     * 开启守护
     * @param context
     */
    public void start(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, GuardService1.class));
            context.startForegroundService(new Intent(context, GuardService2.class));
        } else {
            context.startService(new Intent(context, GuardService1.class));
            context.startService(new Intent(context, GuardService2.class));
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //必须大于5.0
            context.startService(new Intent(context, JobWakeUpService2.class));
            context.startService(new Intent(context, JobWakeUpService1.class));
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new BootCompleteReceiver();
        context.getApplicationContext().registerReceiver(mReceiver, filter);
    }
    /**
     * 关闭守护
     * @param context
     */
    public void stop(Context context){
        if (mReceiver != null){
            context.unregisterReceiver(mReceiver);
        }
    }
}
