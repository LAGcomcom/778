package com.example.smsdemo;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;

public class SmsPoller {

    private final android.content.Context ctx;
    private final android.content.ContentResolver resolver;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            pollOnce();
            handler.postDelayed(this, 15_000);
        }
    };

    public SmsPoller(android.content.Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.resolver = this.ctx.getContentResolver();
    }

    public void start() {
        handler.post(task);
        LogBuffer.log("开始轮询短信");
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private void pollOnce() {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"), new String[]{"_id","address","body","type","date"}, null, null, "date DESC");
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int _id = cursor.getInt(0);
                        String address = cursor.getString(1);
                        String body = cursor.getString(2);
                        int type = cursor.getInt(3);
                        long date = cursor.getLong(4);
                        if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) {
                            if (_id > ConfigManager.getLastUploadedId(ctx)) {
                                LogBuffer.log("轮询发现新短信:" + _id + ", " + address);
                                HttpUploader.enqueueUpload(ctx, _id, address, body, type, date);
                            }
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
