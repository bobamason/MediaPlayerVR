package net.masonapps.mediaplayervr.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by Bob on 5/5/2016.
 */
public class AlbumArtUtils {

    public static Uri uriFromAlbumId(Context context, long albumId) {
        Uri uri = null;
        final String[] projection = new String[]{MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums._ID};
        final String selection = MediaStore.Audio.Albums._ID + " = " + albumId;
        final String[] selectionArgs = null;
        final Cursor cursor = context.getContentResolver().query(Constants.ALBUMS_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            if (path != null) {
                final File file = new File(path);
                if (file != null && file.exists()) uri = Uri.fromFile(file);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return uri;
    }

    public static Uri uriFromArtist(Context context, String artist) {
        Uri uri = null;
        final String[] projection = new String[]{MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.ARTIST};
        final String selection = MediaStore.Audio.Albums.ARTIST + " = '" + artist + "'";
        final String[] selectionArgs = null;
        final Cursor cursor = context.getContentResolver().query(Constants.ALBUMS_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            final String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            if (path != null) {
                final File file = new File(path);
                if (file != null && file.exists()) uri = Uri.fromFile(file);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return uri;
    }
}
