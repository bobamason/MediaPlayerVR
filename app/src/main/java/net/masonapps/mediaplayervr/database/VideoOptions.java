package net.masonapps.mediaplayervr.database;

import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bob on 2/15/2017.
 */

public class VideoOptions {
    public static final int KEY_NONE = 0;
    public static final int KEY_ZOOM = 1;
    public static final int KEY_TINT = 2;
    public static final int KEY_BRIGHTNESS = 3;
    public static final int KEY_CONTRAST = 4;
    public static final int KEY_COLOR_TEMPERATURE = 5;
    public static final int KEY_IPD = 6;

    public static final float MIN_TINT = -0.2f;
    public static final float MAX_TINT = 0.2f;

    public static final float MIN_BRIGHTNESS = -0.5f;
    public static final float MAX_BRIGHTNESS = 0.5f;

    public static final float MIN_CONTRAST = 0f;
    public static final float MAX_CONTRAST = 2f;

    public static final float MIN_COLOR_TEMP = -0.2f;
    public static final float MAX_COLOR_TEMP = 0.2f;

    public static final float DEFAULT_TINT = 0f;
    public static final float DEFAULT_CONTRAST = 1f;
    public static final float DEFAULT_BRIGHTNESS = 0f;
    public static final float DEFAULT_COLOR_TEMP = 0f;

    public static final float MIN_ZOOM = 0.1f;
    public static final float MAX_ZOOM = 2f;
    public static final float MIN_IPD = -20f;
    public static final float MAX_IPD = 20f;
    public static final int DEFAULT_MODE_SELECTION = 0;
    public static final int DEFAULT_ASPECT_SELECTION = 0;
    public static final float DEFAULT_IPD = 1f;
    public static final float DEFAULT_ZOOM = 1f;
    private static final Vector2 DEFAULT_TEXTURE_STRETCH = new Vector2();
    @Nullable
    public String title;
    public long id;
    public boolean useCustomCamera;
    public int modeSelection;
    public int aspectRatioSelection;
    public Vector2 textureStretch = new Vector2(DEFAULT_TEXTURE_STRETCH);
    public float zoom;
    public float ipd;
    public float tint;
    public float brightness;
    public float contrast;
    public float colorTemp;

    public VideoOptions() {
        title = null;
        id = -1;
        modeSelection = DEFAULT_MODE_SELECTION;
        restoreDefaults();
    }

    public void restoreDefaults() {
        useCustomCamera = false;
        aspectRatioSelection = DEFAULT_ASPECT_SELECTION;
        textureStretch.set(DEFAULT_TEXTURE_STRETCH);
        ipd = DEFAULT_IPD;
        zoom = DEFAULT_ZOOM;
        tint = DEFAULT_TINT;
        brightness = DEFAULT_BRIGHTNESS;
        contrast = DEFAULT_CONTRAST;
        colorTemp = DEFAULT_COLOR_TEMP;
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
                "ipd = " + ipd + "\n" +
                "tint = " + tint + "\n" +
                "brightness = " + brightness + "\n" +
                "contrast = " + contrast + "\n" +
                "colorTemp = " + colorTemp + "\n";
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
        public static final String TINT = "tint";
        public static final String BRIGHTNESS = "brightness";
        public static final String CONTRAST = "contrast";
        public static final String COLOR_TEMP = "colorTemp";

        public static final String[] ALL_COLUMNS = new String[]{
                _ID,
                TITLE,
                USE_CUSTOM_CAMERA,
                MODE_SELECTION,
                ASPECT_RATIO_SELECTION,
                TEXTURE_STRETCH_X,
                TEXTURE_STRETCH_Y,
                IPD,
                ZOOM,
                TINT,
                BRIGHTNESS,
                CONTRAST,
                COLOR_TEMP};
    }
}
