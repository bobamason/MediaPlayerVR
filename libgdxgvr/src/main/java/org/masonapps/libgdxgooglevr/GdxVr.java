package org.masonapps.libgdxgooglevr;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.GL20;

import org.masonapps.libgdxgooglevr.vr.VrActivity;
import org.masonapps.libgdxgooglevr.vr.VrAndroidInput;
import org.masonapps.libgdxgooglevr.vr.VrAudio;
import org.masonapps.libgdxgooglevr.vr.VrGraphics;

/**
 * Created by Bob on 1/9/2017.
 */

public class GdxVr {
    public static VrActivity.VrApplication app;
    public static VrGraphics graphics;
    public static VrAndroidInput input;
    public static VrAudio audio;
    public static Files files;
    public static Net net;

    public static GL20 gl;
    public static GL20 gl20;
}
