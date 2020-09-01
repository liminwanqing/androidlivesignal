package com.signal.example;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = "keep";
    public static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        Log.e(TAG, "getAppContext 111 : " + base);
        context = this;
        super.attachBaseContext(base);
    }

    public static Context getAppContext() {
        Log.e(TAG, "getAppContext: " + context);
        return context;
    }
}
