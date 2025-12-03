package com.example.smsdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;

import androidx.core.app.NotificationCompat;

public class SmsForegroundService extends Service {

    private static final String CHANNEL_ID = "sms_observer_channel";
    private SmsContentObserver observer;
    private PendingRetryManager retryManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("短信自动上传")
                .setContentText("正在监听新短信")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(1, notification);
        LogBuffer.log("前台服务启动");
        observer = new SmsContentObserver(getContentResolver(), this);
        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, observer);
        LogBuffer.log("已注册短信监听");
        retryManager = new PendingRetryManager(this);
        retryManager.start();
        LogBuffer.log("启动失败重试管理");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (observer != null) {
            getContentResolver().unregisterContentObserver(observer);
        }
        if (retryManager != null) {
            retryManager.stop();
        }
        LogBuffer.log("前台服务结束");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "短信监听", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("监听并上传新短信");
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }
}
