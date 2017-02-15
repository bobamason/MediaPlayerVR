package net.masonapps.mediaplayervr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Bob on 2/15/2017.
 */

public class VideoOptionsDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "video_options";

    public VideoOptionsDatabaseHelper(Context context) {
        super(context, context.getApplicationContext().getPackageName().replace('.', '_') + ".db", null, DATABASE_VERSION);
    }

    @NonNull
    private static ContentValues createContentValues(VideoOptions videoOptions) {
        final ContentValues values = new ContentValues();
        values.put(VideoOptions.Columns.TITLE, videoOptions.title);
        values.put(VideoOptions.Columns.USE_CUSTOM_CAMERA, Boolean.toString(videoOptions.useCustomCamera));
        values.put(VideoOptions.Columns.MODE_SELECTION, Integer.toString(videoOptions.modeSelection));
        values.put(VideoOptions.Columns.ASPECT_RATIO_SELECTION, Integer.toString(videoOptions.aspectRatioSelection));
        values.put(VideoOptions.Columns.TEXTURE_STRETCH, videoOptions.textureStretch.toString());
        values.put(VideoOptions.Columns.IPD, Float.toString(videoOptions.ipd));
        return values;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String rawSql = "CREATE TABLE " + TABLE_NAME +
                " (" +
                VideoOptions.Columns._ID + " INTEGER PRIMARY KEY," +
                VideoOptions.Columns.TITLE + " TEXT," +
                VideoOptions.Columns.USE_CUSTOM_CAMERA + " TEXT," +
                VideoOptions.Columns.MODE_SELECTION + " TEXT," +
                VideoOptions.Columns.ASPECT_RATIO_SELECTION + " TEXT," +
                VideoOptions.Columns.TEXTURE_STRETCH + " TEXT," +
                VideoOptions.Columns.IPD + " TEXT," +
                VideoOptions.Columns.ZOOM + " TEXT," +
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
        final Cursor cursor = db.query(TABLE_NAME, VideoOptions.Columns.ALL_COLUMNS, VideoOptions.Columns.TITLE + " = " + title, null, null, null, null);
        if (cursor.moveToFirst()) {
            try {
                videoOptions = new VideoOptions();
                videoOptions.id = cursor.getLong(cursor.getColumnIndex(VideoOptions.Columns._ID));
                videoOptions.title = cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.TITLE));
                videoOptions.useCustomCamera = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.USE_CUSTOM_CAMERA)));
                videoOptions.modeSelection = Integer.parseInt(cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.MODE_SELECTION)));
                videoOptions.aspectRatioSelection = Integer.parseInt(cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.ASPECT_RATIO_SELECTION)));
                videoOptions.textureStretch.fromString((cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.TEXTURE_STRETCH))));
                videoOptions.ipd = Float.parseFloat(cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.IPD)));
                videoOptions.zoom = Float.parseFloat(cursor.getString(cursor.getColumnIndex(VideoOptions.Columns.ZOOM)));
            } catch (Exception e) {
                e.printStackTrace();
                videoOptions = null;
            }
        }
        cursor.close();
        return videoOptions;
    }
}
