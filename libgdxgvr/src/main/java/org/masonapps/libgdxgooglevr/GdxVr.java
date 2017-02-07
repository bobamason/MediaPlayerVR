package org.masonapps.libgdxgooglevr;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

import org.masonapps.libgdxgooglevr.vr.VrActivity;
import org.masonapps.libgdxgooglevr.vr.VrAndroidInput;
import org.masonapps.libgdxgooglevr.vr.VrGraphics;

/**
 * Created by Bob on 1/9/2017.
 */

public class GdxVr {
    public static VrActivity app = (VrActivity) Gdx.app;
    public static VrGraphics graphics = (VrGraphics) Gdx.graphics;
    public static VrAndroidInput input = (VrAndroidInput) Gdx.input;
    public static Audio audio = Gdx.audio;
    public static Files files = Gdx.files;
    public static Net net = Gdx.net;

    public static GL20 gl = Gdx.gl;
    public static GL20 gl20 = Gdx.gl20;
    public static GL30 gl30 = Gdx.gl30;
}
