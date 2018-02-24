package org.masonapps.libgdxgooglevr.utils;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by Bob Mason on 2/16/2018.
 */

public class ElapsedTimer {

    private static ElapsedTimer instance = null;
    private final DecimalFormat df;
    private final HashMap<String, Long> startTimes;
    private boolean isReflectionEnabled = true;
    private boolean isLogEnabled = true;

    private ElapsedTimer() {
        df = new DecimalFormat("#,###.##");
        startTimes = new HashMap<>();
    }

    public static ElapsedTimer getInstance() {
        if (instance == null)
            instance = new ElapsedTimer();
        return instance;
    }

    public void start(String tag) {
        startTimes.put(tag, System.nanoTime());
    }

    private long getStartTime(String tag) {
        if (startTimes.containsKey(tag))
            return startTimes.get(tag);
        else
            return System.nanoTime();
    }

    public void log(String tag) {
        if (!isLogEnabled) return;
        long start = getStartTime(tag);
        final long current = System.nanoTime();
        double millis = (current - start) / 1000000.;
        if (isReflectionEnabled) {
            final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.d(String.format("(%s:%d)", stackTraceElement.getFileName(), stackTraceElement.getLineNumber()), tag + " eT = " + df.format(millis) + "ms");
        } else {
            Log.d(tag, tag + " eT = " + df.format(millis) + "ms");
        }
    }

    public void logNanos(String tag) {
        if (!isLogEnabled) return;
        long start = getStartTime(tag);
        final long current = System.nanoTime();
        if (isReflectionEnabled) {
            final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            Log.d(String.format("(%s:%d)", stackTraceElement.getFileName(), stackTraceElement.getLineNumber()), tag + " eT = " + (current - start) + "ns");
        } else {
            Log.d(tag, tag + " eT = " + (current - start) + "ns");
        }
    }

    public void setReflectionEnabled(boolean reflectionEnabled) {
        isReflectionEnabled = reflectionEnabled;
    }

    public void setLogEnabled(boolean isPrintEnabled) {
        this.isLogEnabled = isPrintEnabled;
    }
}
