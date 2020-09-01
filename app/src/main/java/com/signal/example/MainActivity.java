package com.signal.example;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.animation.Animator;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.signal.example.FileLock.FileLockTest;
import com.signal.example.keep.GuardAppManager;
import com.signal.example.util.AesUtils;
import com.signal.testnavigiationbar.interpolator.DecelerateAccelerateInterpolator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "master";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private HomeViewModel homeViewModel;
    TextView text;
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

                testRemove7();

                if (first) {
                    first = false;
                    HandlerThread thread = new HandlerThread("MyHandlerThread");
                    thread.start();
                    mHandler = new Handler(thread.getLooper());
                    mHandler.post(mBackgroundRunnable);
                }

            }
        });

        GuardAppManager.getInstance().start(this);
    }

    /**
     * List.remove()有两个，一个 public E remove(int index)，
     * 一个是public boolean remove(Object o)
     * I/System.out: [1, 2, 3]
     * I/System.out: [1, 3]
     */

    public void testRemove(){
        ArrayList<Integer> integers = new ArrayList<Integer> ();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        System.out.println(integers);
        integers.remove(1);
//        integers.remove((Integer) 1);
        System.out.println(integers);
    }

    /**
     *System.arraycopy(elementData, index, elementData, index + 1,
     *                          size - index);
     *remove 的时候， arrayList大小会变化，导致整体大小变化
     * I/System.out: [1, 2, 2, 4, 5]
     * I/System.out: [1, 2, 5]
     */

    public void testRemove2(){
        List<Integer> integers = new ArrayList<>(5);
        integers.add(1);
        integers.add(2);
        integers.add(2);
        integers.add(4);
        integers.add(5);

        System.out.println(integers);

        for (int i = 0; i < integers.size(); i++) {
            if (integers.get(i) % 2==0){
                integers.remove(i);
            }
        }

        System.out.println(integers);
    }

    /**
     * 调用Arrays.asList()产生的List中add、remove方法时报异常，
     * 这是由于Arrays.asList()返回的是Arrays的内部类ArrayList，
     * 而不是java.util.ArrayList。
     * Arrays的内部类ArrayList和java.util.ArrayList都是继承AbstractList，
     * remove、add等方法在AbstractList中是默认throw UnsupportedOperationException而且不作任何操作。
     * java.util.ArrayList重写这些方法而Arrays的内部类ArrayList没有重写，所以会抛出异常
     */
    public void testRemove3(){
        List<String> list = Arrays.asList("a","b");
        list.add("c");
        System.out.println(list);
    }

    /**
     * strings 已经变化了，不能用
     * java.util.ConcurrentModificationException
     */
    public void testRemove4(){
        List<String> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        strings.add("c");
        strings.add("d");

        for (String string : strings) {
            strings.remove(string);
        }
    }

    /**
     * strings  大小变化了，但是size没变导致异常
     * java.util.ConcurrentModificationException
     */
    public void testRemove5(){
        List<String> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        strings.add("c");
        strings.add("d");

        int size = strings.size();
        for (int i = 0; i < size; i++) {
            strings.remove(i);
        }
    }

    /**
     * strings  大小变化了，但是size没变导致异常
     * java.util.ConcurrentModificationException
     */
    public void testRemove6(){
        List<String> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        strings.add("c");
        strings.add("d");

        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            strings.remove(next);
        }

        System.out.println(strings);
    }

    /**
     *  Succeed
     */
    public void testRemove7(){
        List<String> strings = new ArrayList<>();
        strings.add("a");
        strings.add("b");
        strings.add("c");
        strings.add("d");

        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            iterator.remove();
        }

        System.out.println(strings);
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

    public static String getStackTrace() {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        Throwable print = new Throwable("crashreport");
        print.printStackTrace(printWriter);
        return result.toString();
    }

        /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void signalFromJNI();
}
