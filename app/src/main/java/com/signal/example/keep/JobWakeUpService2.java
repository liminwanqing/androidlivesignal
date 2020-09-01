package com.signal.example.keep;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.List;

public class JobWakeUpService2 extends JobService {
    private static final String TAG = "keep";
    private int jobId = 12;

    @Override
    public boolean onStartJob(JobParameters params) {
        // 开启定时任务，定时轮寻 ， 看MessageService有没有被杀死
        // 如果杀死了启动  轮寻onStartJob
        // 判断服务有没有在运行
        boolean messageServiceAlive = serviceAlive(GuardService2.class.getName());
        if(!messageServiceAlive){
            Log.i(TAG, "GuardService1 is killed ，restart it");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(getApplicationContext(), GuardService2.class));
            } else {
                startService(new Intent(getApplicationContext(), GuardService2.class));
            }
        }
        boolean JobWakeUpService2Alive = serviceAlive(JobWakeUpService1.class.getName());
        if(!JobWakeUpService2Alive){
            Log.i(TAG, "JobWakeUpService2 is killed ，restart it");
            startService(new Intent(this,JobWakeUpService1.class));
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启一个轮寻
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder jobBuilder = new JobInfo.Builder(jobId,new ComponentName(this, JobWakeUpService2.class));
        //设置每两秒钟一次
        jobBuilder.setPeriodic(900000L);
        jobBuilder.setPersisted(true);
        jobScheduler.schedule(jobBuilder.build());
        Log.i(TAG, "start JobWakeUpService1");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // 开启一个轮寻
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder jobBuilder = new JobInfo.Builder(jobId,new ComponentName(this, JobWakeUpService2.class));
        //设置每两秒钟一次
        jobBuilder.setPeriodic(900000L);
        jobBuilder.setPersisted(true);
        jobScheduler.schedule(jobBuilder.build());
        Log.i(TAG, "start JobWakeUpService1");
        super.onDestroy();
    }

    /**
     * 判断某个服务是否正在运行的方法
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    private boolean serviceAlive(String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
