package org.masonapps.libgdxgooglevr.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

/**
 * Created by Bob on 3/8/2017.
 */

public class PhongShader extends DefaultShader {

    private static String phongVertexShader = null;
    private static String phongFragmentShader = null;

    public PhongShader(Renderable renderable) {
        this(renderable, new Config());
    }

    public PhongShader(Renderable renderable, Config config) {
        super(renderable, config, createPrefix(renderable, config), getPhongVertexShader(), getPhongFragmentShader());
    }

    public static String getPhongVertexShader() {
        if (phongVertexShader == null)
            phongVertexShader = Gdx.files.internal("phong.vertex.glsl").readString();
        return phongVertexShader;
    }

    public static String getPhongFragmentShader() {
        if (phongFragmentShader == null)
            phongFragmentShader = Gdx.files.internal("phong.fragment.glsl").readString();
        return phongFragmentShader;
    }

}
