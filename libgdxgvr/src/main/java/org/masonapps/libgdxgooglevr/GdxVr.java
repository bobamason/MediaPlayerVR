package org.masonapps.libgdxgooglevr;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.GL20;

import org.masonapps.libgdxgooglevr.vr.VrActivity;
import org.masonapps.libgdxgooglevr.vr.VrAndroidInput;
import org.masonapps.libgdxgooglevr.vr.VrGraphics;

/**
 * Created by Bob on 1/9/2017.
 */

public class GdxVr {
    public static VrActivity app;
    public static VrGraphics graphics;
    public static VrAndroidInput input;
    public static Audio audio;
    public static Files files;
    public static Net net;

    public static GL20 gl;
    public static GL20 gl20;
}
