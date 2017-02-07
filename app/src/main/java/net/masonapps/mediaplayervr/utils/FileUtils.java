package net.masonapps.mediaplayervr.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Bob on 11/9/2016.
 */

public class FileUtils {

    public static String readRawText(Context context, int resId) throws IOException {
        return readRawText(context.getResources().openRawResource(resId));
    }

    private static String readRawText(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(inputStream);
        final char[] buffer = new char[1024];
        int bytesRead;
        try {
            while ((bytesRead = reader.read(buffer)) > 0) {
                stringBuilder.append(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public static String readRawText(File file) throws IOException {
        return readRawText(new FileInputStream(file));
    }
}
