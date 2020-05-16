package com.challenge.dfr.broken;

import android.app.ActivityManager;
import android.content.Context;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.os.Build;

import java.lang.reflect.Field;

public class BadIdeas {
    public static void fix(Context ctx) {
        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            long maxMemory = getMemorySizeInBytes(ctx);
            if (maxMemory < Integer.MAX_VALUE) {
                field.set(null, (int) getMemorySizeInBytes(ctx)); //Make the SQL cursor the full RAM size.
            } else {
                field.set(null, (int) Integer.MAX_VALUE); //Make the SQL cursor the full int size.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sourced: https://ourcodeworld.com/articles/read/900/how-to-retrieve-the-available-ram-on-your-android-device-with-java
     * Returns the available ammount of RAM of your Android device in Bytes e.g 1567342592 (1.5GB)
     * @return {Long}
     */
    private static long getMemorySizeInBytes(Context ctx)
    {
        Context context = ctx;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalMemory = memoryInfo.totalMem;
        return totalMemory;
    }
}
