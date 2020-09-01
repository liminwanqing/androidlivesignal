package com.zenmen.demo.keep;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class GuardService1 extends FgService {
    private static final String TAG = "keep";
    private final int GuardServiceId2 = 12;

    @Override
    public void onCreate() {
        super.onCreate();
        XK();
        Log.i(TAG, "GuardService1 wait for signal");
    }

    @SuppressLint("WrongConstant")
    private void XK() {
        try {
            Intent intent = new Intent(this, GuardService1.class);
            intent.putExtra("extra_reason", "AlarmManagerFire");
            PendingIntent service = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
            ((AlarmManager) getApplicationContext().getSystemService(NotificationCompat.CATEGORY_ALARM)).setRepeating(9, System.currentTimeMillis(), 100L, service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //提高进程优先级
        createNotification(GuardServiceId2, "GuardService1");
        //绑定建立链接
        bindService(new Intent(getApplicationContext(), GuardService2.class),connection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new GuardAIDL.Stub(){};
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "connect to guardservice2");
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // 断开链接 ,重新启动，重新绑定
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(getApplicationContext(), GuardService2.class));
            } else {
                startService(new Intent(getApplicationContext(), GuardService2.class));
            }
            bindService(new Intent(getApplicationContext(),GuardService2.class), connection, Context.BIND_IMPORTANT);
            Log.i(TAG, "disconnect from guardservice2");
        }
    };
    @Override
    public void onDestroy() {
        Log.i(TAG, "guardservice2 is onDestroy");
        unbindService(connection);
        super.onDestroy();
    }
}
