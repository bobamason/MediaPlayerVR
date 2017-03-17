package net.masonapps.mediaplayervr;

/**
 * Created by Bob on 3/16/2017.
 */

public class GlobalSettings {
    public static final int NONE = 0;
    public static final int ZOOM = 1;
    public static final int TINT = 2;
    public static final int BRIGHTNESS = 3;
    public static final int CONTRAST = 4;
    public static final int SATURATION = 5;

    public static final float MIN_TINT = 0f;
    public static final float MAX_TINT = 1f;
    public static final String KEY_TINT = "tint";

    public static final float MIN_BRIGHTNESS = -0.5f;
    public static final float MAX_BRIGHTNESS = 0.5f;
    public static final String KEY_BRIGHTNESS = "brightness";

    public static final float MIN_CONTRAST = 0.1f;
    public static final float MAX_CONTRAST = 2.1f;
    public static final String KEY_CONTRAST = "contrast";

    public static final float MIN_SATURATION = 0f;
    public static final float MAX_SATURATION = 1f;
    public static final String KEY_SATURATION = "saturation";
    public static final float DEFAULT_TINT = 0f;
    public static final float DEFAULT_CONTRAST = 1f;
    public static final float DEFAULT_BRIGHTNESS = 0f;
    public static final float DEFAULT_SATURATION = 1f;

    private static GlobalSettings instance = null;

    public float tint;
    public float brightness;
    public float contrast;
    public float saturation;

    private GlobalSettings() {
        tint = DEFAULT_TINT;
        contrast = DEFAULT_CONTRAST;
        brightness = DEFAULT_BRIGHTNESS;
        saturation = DEFAULT_SATURATION;
    }

    public static GlobalSettings getInstance() {
        if (instance == null)
            instance = new GlobalSettings();
        return instance;
    }

    public void restoreDefault() {
        tint = DEFAULT_TINT;
        contrast = DEFAULT_CONTRAST;
        brightness = DEFAULT_BRIGHTNESS;
        saturation = DEFAULT_SATURATION;
    }
}
