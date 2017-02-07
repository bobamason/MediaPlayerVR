package net.masonapps.mediaplayervr.video;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by Bob on 11/11/2016.
 */

public class VideoShader extends BaseShader {

    private final int u_projTrans = register(new Uniform("u_projTrans"));
    private final int u_worldTrans = register(new Uniform("u_worldTrans"));
//    private final int u_texture = register(new Uniform("u_texture"));
    private final int u_texScale = register(new Uniform("u_texScale"));
    private final int u_texOffset = register(new Uniform("u_texOffset"));
    
    private Vector2 textureScale = new Vector2();
    private Vector2 textureOffset = new Vector2();

    private static String vertexShader = null;
    private static String fragmentShader = null;
    private int textureId = -1;

    private static String getVertexShader() {
        if (vertexShader == null)
            vertexShader = Gdx.files.internal("video.vertex.glsl").readString();
        return vertexShader;
    }

    private static String getFragmentShader() {
        if (fragmentShader == null)
            fragmentShader = Gdx.files.internal("video.fragment.glsl").readString();
        return fragmentShader;
    }

    private final ShaderProgram program;

    public VideoShader() {
        program = new ShaderProgram(getVertexShader(), getFragmentShader());

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
        if(textureId >= 0) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        }
        set(u_texOffset, textureOffset);
        set(u_texScale, textureScale);
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

    public void setTextureOffset(float x, float y) {
        this.textureOffset.set(x, y);
    }

    public void setTextureScale(float x, float y) {
        this.textureScale.set(x, y);
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}
