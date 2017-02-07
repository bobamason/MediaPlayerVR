package net.masonapps.mediaplayervr.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by Bob on 12/1/2016.
 */

public class BarrierShader extends BaseShader {

    protected final int u_projTrans = register(new Uniform("u_projTrans"));
    protected final int u_worldTrans = register(new Uniform("u_worldTrans"));
    protected final int u_color = register(new Uniform("u_color"));
    protected final int u_color2 = register(new Uniform("u_color2"));
    protected final int u_time = register(new Uniform("u_time"));
    protected final int u_intensity = register(new Uniform("u_intensity"));
    protected final int u_diffuseTexture = register(new Uniform("diffuseTexture"));

    protected final ShaderProgram program;
    public float time = 0f;

    public BarrierShader() {
        program = new ShaderProgram(Gdx.files.internal("barrier_vertex.glsl"), Gdx.files.internal("barrier_fragment.glsl"));

        if (!program.isCompiled())
            throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
        String log = program.getLog();
        if (log.length() > 0) Gdx.app.error("ShaderTest", "Shader compilation log: " + log);
        init();
    }

    @Override
    public void init() {
        super.init(program, null);
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
//        return instance.material.has(ColorAttribute.Diffuse);
//        & instance.material.has(TimeAttribute.ID);
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
        context.setDepthMask(true);
        set(u_projTrans, camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        set(u_worldTrans, renderable.worldTransform);
        if (renderable.material.has(ColorAttribute.Diffuse)) {
            ColorAttribute colorAttr = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
            set(u_color, colorAttr.color);
        }
        if (renderable.material.has(ColorAttribute.Ambient)) {
            ColorAttribute colorAttr2 = (ColorAttribute) renderable.material.get(ColorAttribute.Ambient);
            set(u_color2, colorAttr2.color);
        }
        if (renderable.material.has(TextureAttribute.Diffuse)) {
            TextureAttribute texAttr = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
            texAttr.textureDescription.texture.bind();
            set(u_diffuseTexture, 0);
        }
        if (renderable.material.has(IntensityAttribute.ID)) {
            IntensityAttribute intensityAttr = (IntensityAttribute) renderable.material.get(IntensityAttribute.ID);
            set(u_intensity, intensityAttr.intensity);
        }

        set(u_time, time);

        renderable.meshPart.render(program);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        program.dispose();
    }
}
