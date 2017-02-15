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
        modeSelection = -1;
        aspectRatioSelection = -1;
        textureStretch = new Vector2();
        ipd = 0.0639f;
        zoom = 1f;
    }

    public static class Columns implements BaseColumns {
        public static final String TITLE = "title";
        public static final String USE_CUSTOM_CAMERA = "useCustomCamera";
        public static final String MODE_SELECTION = "modeSelection";
        public static final String ASPECT_RATIO_SELECTION = "aspectRatioSelection";
        public static final String TEXTURE_STRETCH = "textureStretch";
        public static final String IPD = "ipd";
        public static final String ZOOM = "zoom";

        public static final String[] ALL_COLUMNS = new String[]{
                _ID,
                TITLE,
                USE_CUSTOM_CAMERA,
                MODE_SELECTION,
                ASPECT_RATIO_SELECTION,
                TEXTURE_STRETCH,
                IPD,
                ZOOM};
    }
}
