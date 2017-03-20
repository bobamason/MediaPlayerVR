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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.masonapps.mediaplayervr.GlobalSettings;

/**
 * Created by Bob on 11/11/2016.
 */

public class VideoShader extends BaseShader {

    private static String vertexShader = null;
    private static String fragmentShader = null;
    private final int u_projTrans = register(new Uniform("u_projTrans"));
    private final int u_worldTrans = register(new Uniform("u_worldTrans"));
    private final int u_srcRect = register(new Uniform("u_srcRect"));
    private final int u_dstRect = register(new Uniform("u_dstRect"));
    private final int u_clip = register(new Uniform("u_clip"));
    private final int u_tint = register(new Uniform("u_tint"));
    private final int u_brightness = register(new Uniform("u_brightness"));
    private final int u_contrast = register(new Uniform("u_contrast"));
    private final int u_colorTemp = register(new Uniform("u_colorTemp"));
    private final ShaderProgram program;
    private Rectangle srcRect = new Rectangle(0, 0, 1, 1);
    private Rectangle dstRect = new Rectangle(0, 0, 1, 1);
    private float tint = GlobalSettings.DEFAULT_TINT;
    private float brightness = GlobalSettings.DEFAULT_BRIGHTNESS;
    private float contrast = GlobalSettings.DEFAULT_CONTRAST;
    private float colorTemp = GlobalSettings.DEFAULT_COLOR_TEMP;
    private int textureId = -1;

    public VideoShader() {
        program = new ShaderProgram(getVertexShader(), getFragmentShader());

        if (!program.isCompiled())
            throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
        String log = program.getLog();
        if (log.length() > 0) Gdx.app.error("ShaderTest", "Shader compilation log: " + log);
        init();
    }

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
        return textureId >= 0;
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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        set(u_srcRect, srcRect.x, srcRect.y, srcRect.width, srcRect.height);
        set(u_dstRect, dstRect.x, dstRect.y, dstRect.width, dstRect.height);
        set(u_clip, srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y + srcRect.height);
        set(u_tint, tint);
        set(u_brightness, brightness);
        set(u_contrast, contrast);
        set(u_colorTemp, colorTemp);
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

    public void setSrcRect(float x, float y, float width, float height) {
        this.srcRect.set(x, y, width, height);
    }

    public void setDstRect(float x, float y, float width, float height) {
        this.dstRect.set(x, y, width, height);
    }

    public Rectangle getSrcRect() {
        return srcRect;
    }

    public Rectangle getDstRect() {
        return dstRect;
    }

    public float getTint() {
        return tint;
    }

    public void setTint(float tint) {
        this.tint = tint;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float getContrast() {
        return contrast;
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
    }

    public float getColorTemp() {
        return colorTemp;
    }

    public void setColorTemp(float colorTemp) {
        this.colorTemp = colorTemp;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}
