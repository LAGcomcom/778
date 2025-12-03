package com.example.smsdemo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogBuffer {
    private static final List<String> lines = new ArrayList<>();
    private static final int MAX = 1000;
    private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");

    public static synchronized void log(String s) {
        String ts = fmt.format(new Date());
        lines.add(ts + " " + s);
        if (lines.size() > MAX) {
            lines.remove(0);
        }
    }

    public static synchronized String dump() {
        StringBuilder sb = new StringBuilder();
        for (String l : lines) sb.append(l).append('\n');
        return sb.toString();
    }

    public static synchronized void clear() {
        lines.clear();
    }
}
