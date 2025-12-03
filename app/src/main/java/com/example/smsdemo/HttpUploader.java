package com.example.smsdemo;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUploader {

    public static void enqueueUpload(Context ctx, int id, String address, String body, int type, long dateTs) {
        new Thread(() -> {
            String urlStr = ConfigManager.getUploadUrl(ctx);
            String phone = ConfigManager.getPhoneNumber(ctx);
            if (urlStr == null || urlStr.isEmpty() || phone == null || phone.isEmpty()) {
                LogBuffer.log("上传跳过: 未配置地址或号码");
                return;
            }
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                String json = toJson(phone, id, address, body, type, dateTs);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                bw.write(json);
                bw.flush();
                bw.close();
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    ConfigManager.setLastUploadedId(ctx, id);
                    LogBuffer.log("上传成功:" + id + ", code=" + code);
                } else {
                    PendingQueue.enqueue(ctx, json);
                    LogBuffer.log("上传失败入队:" + id + ", code=" + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                PendingQueue.enqueue(ctx, toJson(ConfigManager.getPhoneNumber(ctx), id, address, body, type, dateTs));
                LogBuffer.log("上传异常入队:" + id + ", " + e.getClass().getSimpleName() + ":" + (e.getMessage() == null ? "" : e.getMessage()));
            }
        }).start();
    }

    private static String toJson(String phone, int id, String address, String body, int type, long dateTs) {
        String safeBody = body == null ? "" : body.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        String safeAddr = address == null ? "" : address.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{" +
                "\"phone\":\"" + phone + "\"," +
                "\"sms_id\":" + id + "," +
                "\"address\":\"" + safeAddr + "\"," +
                "\"body\":\"" + safeBody + "\"," +
                "\"type\":" + type + "," +
                "\"date_ts\":" + dateTs +
                "}";
    }
}
