package net.masonapps.mediaplayervr.database;

import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bob on 2/15/2017.
 */

public class VideoOptions {
    public static final float MIN_ZOOM = 0.1f;
    public static final float MAX_ZOOM = 2f;
    public static final int DEFAULT_MODE_SELECTION = 0;
    public static final int DEFAULT_ASPECT_SELECTION = 0;
    public static final float DEFAULT_IPD = 0f;
    public static final float DEFAULT_ZOOM = 1f;
    private static final Vector2 DEFAULT_TEXTURE_STRETCH = new Vector2();
    @Nullable
    public String title;
    public long id;
    public boolean useCustomCamera;
    public int modeSelection;
    public int aspectRatioSelection;
    public Vector2 textureStretch;
    public float zoom;
    public float ipd;

    public VideoOptions() {
        title = null;
        id = -1;
        useCustomCamera = false;
        modeSelection = DEFAULT_MODE_SELECTION;
        aspectRatioSelection = DEFAULT_ASPECT_SELECTION;
        textureStretch = new Vector2(DEFAULT_TEXTURE_STRETCH);
        ipd = DEFAULT_IPD;
        zoom = DEFAULT_ZOOM;
    }

    public void restoreDefaults() {
        useCustomCamera = false;
        aspectRatioSelection = DEFAULT_ASPECT_SELECTION;
        textureStretch = new Vector2(DEFAULT_TEXTURE_STRETCH);
        ipd = DEFAULT_IPD;
        zoom = DEFAULT_ZOOM;
    }

    @Override
    public String toString() {
        return "title = " + (title == null ? "null" : title.replace('.', '_')) + "\n" +
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
