package net.masonapps.mediaplayervr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Bob on 2/15/2017.
 */

public class VideoOptionsDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String TABLE_NAME = "video_options";

    public VideoOptionsDatabaseHelper(Context context) {
        super(context, context.getApplicationContext().getPackageName().replace('.', '_') + ".db", null, DATABASE_VERSION);
    }

    @NonNull
    private static ContentValues createContentValues(@NonNull VideoOptions videoOptions) {
//        Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "createContentValues -> " + videoOptions.toString());
        final ContentValues values = new ContentValues();
        values.put(VideoOptions.Columns.TITLE, getEscapedTitleString(videoOptions.title));
        values.put(VideoOptions.Columns.USE_CUSTOM_CAMERA, videoOptions.useCustomCamera ? 1 : 0);
        values.put(VideoOptions.Columns.MODE_SELECTION, videoOptions.modeSelection);
        values.put(VideoOptions.Columns.ASPECT_RATIO_SELECTION, videoOptions.aspectRatioSelection);
        values.put(VideoOptions.Columns.TEXTURE_STRETCH_X, videoOptions.textureStretch.x);
        values.put(VideoOptions.Columns.TEXTURE_STRETCH_Y, videoOptions.textureStretch.y);
        values.put(VideoOptions.Columns.IPD, videoOptions.ipd);
        values.put(VideoOptions.Columns.EYE_ANGLE, videoOptions.eyeAngle);
        values.put(VideoOptions.Columns.ZOOM, videoOptions.zoom);
        values.put(VideoOptions.Columns.TINT, videoOptions.tint);
        values.put(VideoOptions.Columns.BRIGHTNESS, videoOptions.brightness);
        values.put(VideoOptions.Columns.CONTRAST, videoOptions.contrast);
        values.put(VideoOptions.Columns.COLOR_TEMP, videoOptions.colorTemp);
        return values;
    }

    private static String getEscapedTitleString(String title) {
        return title.replaceAll("[.\'\"]", "_");
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
                VideoOptions.Columns.EYE_ANGLE + " REAL," +
                VideoOptions.Columns.ZOOM + " REAL," +
                VideoOptions.Columns.TINT + " REAL," +
                VideoOptions.Columns.BRIGHTNESS + " REAL," +
                VideoOptions.Columns.CONTRAST + " REAL," +
                VideoOptions.Columns.COLOR_TEMP + " REAL" +
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
        videoOptions.id = getWritableDatabase().insert(TABLE_NAME, null, values);
//        Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "inserted new videoOptions:\n" + videoOptions.toString());
    }

    public void saveVideoOptions(VideoOptions videoOptions) {
//        Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "saving");
        if (videoOptions.id < 0) {
            insertVideoOptions(videoOptions);
            return;
        }
        final ContentValues values = createContentValues(videoOptions);
        final int count = getWritableDatabase().update(TABLE_NAME, values, VideoOptions.Columns._ID + " = " + videoOptions.id, null);
        if (count <= 0)
            insertVideoOptions(videoOptions);
//        else
//            Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "updated videoOptions:\n" + videoOptions.toString());
    }

    @Nullable
    public VideoOptions getVideoOptionsByTitle(String title) {
        VideoOptions videoOptions = null;
        final SQLiteDatabase db = getReadableDatabase();
        final String selection = VideoOptions.Columns.TITLE + " LIKE ?";
        final Cursor cursor = db.query(TABLE_NAME, VideoOptions.Columns.ALL_COLUMNS, selection, new String[]{getEscapedTitleString(title)}, null, null, null);
        if (cursor.moveToFirst()) {
//            Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "loading existing videoOptions for: " + getEscapedTitleString(title));
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
                videoOptions.eyeAngle = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.EYE_ANGLE));
                videoOptions.zoom = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.ZOOM));
                    videoOptions.tint = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.TINT));
                    videoOptions.brightness = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.BRIGHTNESS));
                    videoOptions.contrast = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.CONTRAST));
                    videoOptions.colorTemp = cursor.getFloat(cursor.getColumnIndex(VideoOptions.Columns.COLOR_TEMP));
                Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "loading videoOptions successful");
            } catch (Exception e) {
                Log.e(VideoOptionsDatabaseHelper.class.getSimpleName(), "loading existing videoOptions failed");
                e.printStackTrace();
            }
        } else {
            Log.d(VideoOptionsDatabaseHelper.class.getSimpleName(), "no existing videoOptions found for: " + getEscapedTitleString(title));
        }
        cursor.close();
        return videoOptions;
    }
}
