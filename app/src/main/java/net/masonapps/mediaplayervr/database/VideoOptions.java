package net.masonapps.mediaplayervr.database;

import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bob on 2/15/2017.
 */

public class VideoOptions {
    @Nullable
    public String title;
    public long id;
    public boolean useCustomCamera;
    public int modeSelection;
    public int aspectRatioSelection;
    public Vector2 textureStretch;
    public float ipd;
    public float zoom;

    public VideoOptions() {
        title = null;
        id = -1;
        useCustomCamera = false;
        modeSelection = 0;
        aspectRatioSelection = 0;
        textureStretch = new Vector2();
        ipd = 0f;
        zoom = 1f;
    }

    @Override
    public String toString() {
        return "title = " + (title == null ? "null" : title.replace('.', ',')) + "\n" +
                "id = " + id + "\n" +
                "useCustomCamera = " + useCustomCamera + "\n" +
                "modeSelection = " + modeSelection + "\n" +
                "aspectRatioSelection = " + aspectRatioSelection + "\n" +
                "textureStretch = " + textureStretch.toString() + "\n" +
                "zoom = " + zoom + "\n" +
                "ipd = " + ipd + "\n";
    }

    public static class Columns implements BaseColumns {
        public static final String TITLE = "title";
        public static final String USE_CUSTOM_CAMERA = "useCustomCamera";
        public static final String MODE_SELECTION = "modeSelection";
        public static final String ASPECT_RATIO_SELECTION = "aspectRatioSelection";
        public static final String TEXTURE_STRETCH_X = "textureStretchX";
        public static final String TEXTURE_STRETCH_Y = "textureStretchY";
        public static final String IPD = "ipd";
        public static final String ZOOM = "zoom";

        public static final String[] ALL_COLUMNS = new String[]{
                _ID,
                TITLE,
                USE_CUSTOM_CAMERA,
                MODE_SELECTION,
                ASPECT_RATIO_SELECTION,
                TEXTURE_STRETCH_X,
                TEXTURE_STRETCH_Y,
                IPD,
                ZOOM};
    }
}
