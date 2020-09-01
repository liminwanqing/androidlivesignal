package com.zenmen.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.animation.Animator;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zenmen.demo.FileLock.FileLockTest;
import com.zenmen.demo.component.DataActivity;
import com.zenmen.demo.keep.GuardAppManager;
import com.zenmen.demo.util.AesUtils;
import com.zenmen.testnavigiationbar.interpolator.DecelerateAccelerateInterpolator;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "master";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private HomeViewModel homeViewModel;
    TextView text, data;
    private boolean mRunning = false;
    private Handler mHandler;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        final TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        text = findViewById(R.id.test);
        data = findViewById(R.id.dataBtn);

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        homeViewModel.getHome().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tv.setText(homeViewModel.getHome().getValue());
                text.setText(homeViewModel.getHome().getValue());
            }
        });

        //test aes
        String testStr = "E3D5847D-17BD-4FEC-9399-8D6BF51E9E69";
        Log.i(TAG, "onCreate:  111 " + testStr);
        String testjiami = AesUtils.aesEncrypt(testStr);

        Log.i(TAG, "onCreate: 222 " + testjiami);

        String testjiemi = AesUtils.aesDecrypt(testjiami);
        Log.i(TAG, "onCreate: 333 " + testjiemi);

        //test filelock
        FileLockTest fileLockTest = new FileLockTest();
        fileLockTest.test(this);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = stringFromJNI();
                homeViewModel.getHome().setValue(tmp);
                Animator animator = ObjectAnimator.ofFloat(text, "translationX", 0f, 300f, 0f);
                animator.setDuration(1000);
                animator.setInterpolator(new DecelerateAccelerateInterpolator());
                animator.start();

                if (first) {
                    first = false;
                    HandlerThread thread = new HandlerThread("MyHandlerThread");
                    thread.start();
                    mHandler = new Handler(thread.getLooper());
                    mHandler.post(mBackgroundRunnable);
                }

            }
        });

        data.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intenA = new Intent(getApplicationContext(), DataActivity.class);
                startActivity(intenA);
            }
        });

        GuardAppManager.getInstance().start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GuardAppManager.getInstance().stop(this);
    }

    Runnable mBackgroundRunnable = new Runnable() {

        @Override
        public void run() {
            signalFromJNI();
            while (mRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

        /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void signalFromJNI();
}
