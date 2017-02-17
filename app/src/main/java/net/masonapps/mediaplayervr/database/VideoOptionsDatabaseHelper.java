package net.masonapps.mediaplayervr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Bob on 2/15/2017.
 */

public class VideoOptionsDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String TABLE_NAME = "video_options";

    public VideoOptionsDatabaseHelper(Context context) {
        super(context, context.getApplicationContext().getPackageName().replace('.', '_') + ".db", null, DATABASE_VERSION);
    }

    @NonNull
    private static ContentValues createContentValues(VideoOptions videoOptions) {
        final ContentValues values = new ContentValues();
        values.put(VideoOptions.Columns.TITLE, DatabaseUtils.sqlEscapeString(videoOptions.title));
        values.put(VideoOptions.Columns.USE_CUSTOM_CAMERA, videoOptions.useCustomCamera ? 1 : 0);
        values.put(VideoOptions.Columns.MODE_SELECTION, videoOptions.modeSelection);
        values.put(VideoOptions.Columns.ASPECT_RATIO_SELECTION, videoOptions.aspectRatioSelection);
        values.put(VideoOptions.Columns.TEXTURE_STRETCH_X, videoOptions.textureStretch.x);
        values.put(VideoOptions.Columns.TEXTURE_STRETCH_Y, videoOptions.textureStretch.y);
        values.put(VideoOptions.Columns.IPD, videoOptions.ipd);
        values.put(VideoOptions.Columns.ZOOM, videoOptions.zoom);
        return values;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String rawSql = "CREATE TABLE " + TABLE_NAME +
                " (" +
                VideoOptions.Columns._ID + " INTEGER PRIMARY KEY," +
                VideoOptions.Columns.TITLE + " TEXT," +
                VideoOptions.Columns.USE_CUSTOM_CAMERA + " INTEGER," +
                VideoOptions.Columns.MODE_SELECTION + " INTEGER," +
                VideoOptions.Columns.ASPECT_RATIO_SELECTION + " INTEGER," +
                VideoOptions.Columns.TEXTURE_STRETCH_X + " REAL," +
                VideoOptions.Columns.TEXTURE_STRETCH_Y + " REAL," +
                VideoOptions.Columns.IPD + " REAL," +
                VideoOptions.Columns.ZOOM + " REAL" +
                ")";
        sqLiteDatabase.execSQL(rawSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        final String rawSql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(rawSql);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void insertVideoOptions(VideoOptions videoOptions) {
        final ContentValues values = createContentValues(videoOptions);
        values.put(VideoOptions.Columns.ZOOM, Float.toString(videoOptions.zoom));
        videoOptions.id = getWritableDatabase().insert(TABLE_NAME, null, values);
    }

    public void saveVideoOptions(VideoOptions videoOptions) {
        if (videoOptions.id < 0) {
            insertVideoOptions(videoOptions);
            return;
        }
        final ContentValues values = createContentValues(videoOptions);
        values.put(VideoOptions.Columns.ZOOM, Float.toString(videoOptions.zoom));
        final int count = getWritableDatabase().update(TABLE_NAME, values, VideoOptions.Columns._ID + " = " + videoOptions.id, null);
        if (count <= 0)
            insertVideoOptions(videoOptions);
    }

    @Nullable
    public VideoOptions getVideoOptionsByTitle(String title) {
        VideoOptions videoOptions = null;
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(TABLE_NAME, VideoOptions.Columns.ALL_COLUMNS, VideoOptions.Columns.TITLE + " = " + DatabaseUtils.sqlEscapeString(title), null, null, null, null);
        if (cursor.moveToFirst()) {
            try {
                videoOptions = new VideoOptions();
                videoOptions.id = cursor.getLong(cursor.getColumnIndex(VideoOptions.Columns._ID));
                videoOptions.title = cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.TITLE));
                videoOptions.useCustomCamera = cursor.getInt(cursor.getColumnIndex(VideoOptions.Columns.USE_CUSTOM_CAMERA)) == 1;
                videoOptions.modeSelection = cursor.getInt(cursor.getColumnIndex(VideoOptions.Columns.MODE_SELECTION));
                videoOptions.aspectRatioSelection = cursor.getInt(cursor.getColumnIndex(VideoOptions.Columns.ASPECT_RATIO_SELECTION));
                videoOptions.textureStretch.x = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.TEXTURE_STRETCH_X));
                videoOptions.textureStretch.y = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.TEXTURE_STRETCH_Y));
                videoOptions.ipd = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.IPD));
                videoOptions.zoom = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.ZOOM));
            } catch (Exception e) {
                e.printStackTrace();
                videoOptions = null;
            }
        }
        cursor.close();
        return videoOptions;
    }
}
