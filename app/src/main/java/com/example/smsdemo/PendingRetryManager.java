package com.example.smsdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class PendingRetryManager {

    private final Context ctx;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            processOnce();
            handler.postDelayed(this, 60_000);
        }
    };

    public PendingRetryManager(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    public void start() {
        handler.post(task);
        LogBuffer.log("开始定时重试");
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private void processOnce() {
        File f = new File(PathUtils.INSTANCE.concatFilePath(PathUtils.INSTANCE.getAppPath(), "pending.jsonl"));
        if (!f.exists()) return;
        List<String> failures = new ArrayList<>();
        int total = 0;
        int success = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                total++;
                try {
                    int code = SimplePoster.post(ConfigManager.getUploadUrl(ctx), line);
                    if (code < 200 || code >= 300) {
                        failures.add(line);
                    }
                    else success++;
                } catch (Exception e) {
                    failures.add(line);
                }
            }
            br.close();
        } catch (Exception ignored) {
            return;
        }
        try {
            FileWriter fw = new FileWriter(f, false);
            for (String s : failures) {
                fw.write(s);
                fw.write("\n");
            }
            fw.flush();
            fw.close();
        } catch (Exception ignored) {
        }
        LogBuffer.log("重试完成: 总计=" + total + ", 成功=" + success + ", 剩余=" + failures.size());
    }

    public static class SimplePoster {
        public static int post(String urlStr, String json) throws Exception {
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);
            conn.setRequestProperty("User-Agent", "SmsSync/1.0");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            bw.write(json);
            bw.flush();
            bw.close();
            int code = conn.getResponseCode();
            conn.disconnect();
            return code;
        }
    }
}
