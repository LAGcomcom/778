package com.example.smsdemo;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;

public class PendingQueue {
    public static void enqueue(Context ctx, String json) {
        try {
            File f = new File(PathUtils.INSTANCE.concatFilePath(PathUtils.INSTANCE.getAppPath(), "pending.jsonl"));
            FileWriter fw = new FileWriter(f, true);
            fw.write(json);
            fw.write("\n");
            fw.flush();
            fw.close();
        } catch (Exception ignored) {
        }
    }
}
