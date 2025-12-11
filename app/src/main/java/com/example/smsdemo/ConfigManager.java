package com.example.smsdemo;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {

    private static final String PREFS_NAME = "smsdemo_prefs";
    private static final String KEY_UPLOAD_URL = "upload_url";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_LAST_UPLOADED_ID = "last_uploaded_id";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void setUploadUrl(Context ctx, String url) {
        prefs(ctx).edit().putString(KEY_UPLOAD_URL, url == null ? "" : url.trim()).apply();
    }

    public static String getUploadUrl(Context ctx) {
        return "http://101.237.34.195:7789/api/sms/upload.php";
    }

    public static void setPhoneNumber(Context ctx, String phone) {
        prefs(ctx).edit().putString(KEY_PHONE_NUMBER, phone == null ? "" : phone.trim()).apply();
    }

    public static String getPhoneNumber(Context ctx) {
        return prefs(ctx).getString(KEY_PHONE_NUMBER, "");
    }

    public static void setLastUploadedId(Context ctx, int id) {
        prefs(ctx).edit().putInt(KEY_LAST_UPLOADED_ID, id).apply();
    }

    public static int getLastUploadedId(Context ctx) {
        return prefs(ctx).getInt(KEY_LAST_UPLOADED_ID, -1);
    }
}
