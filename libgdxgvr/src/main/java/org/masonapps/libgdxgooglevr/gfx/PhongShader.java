package org.masonapps.libgdxgooglevr.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

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
        this(renderable, config, createPrefix(renderable, config));
    }

    public PhongShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix, config.vertexShader != null ? config.vertexShader : getPhongVertexShader(),
                config.fragmentShader != null ? config.fragmentShader : getPhongFragmentShader());
    }

    public PhongShader(Renderable renderable, Config config, String prefix, String vertexShader, String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    public PhongShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);
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
