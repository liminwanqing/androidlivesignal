package com.signal.example.keep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.signal.example.MyApplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "Keep";
    private static DemoActivity context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        Window window = getWindow();
        window.setGravity(Gravity.LEFT & Gravity.TOP);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = 1;
        attributes.height = 1;
        window.setAttributes(attributes);
    }

    public static void startDaemon() {
        Log.i(TAG,"startDaemon context "  + MyApplication.getAppContext());
        Intent intent = new Intent(MyApplication.getAppContext(), DemoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getAppContext().startActivity(intent);
        Log.i(TAG,"startDaemon");
    }

    public static void stopDaemon() {
        if (null != context) {
            Log.i(TAG,"stopDaemon");
            context.finish();
        }
    }
}
