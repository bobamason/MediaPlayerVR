package net.masonapps.mediaplayervr.audiovisualization;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.vr.sdk.base.Eye;

import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/18/2017.
 */

public class TrippyVisualizer extends MusicVisualizerScreen {
    private final Vector3 axis = new Vector3(1f, 1f, 1f).nor();
    private Array<ModelInstance> instances = new Array<>();
    private SpriteBatch spriteBatch;
    private FrameBuffer fbo;
    private OrthographicCamera orthoCamera;
    private float angle = 0f;
    private Texture texture;
    private Color color = new Color();
    private ShaderProgram shaderProgram;
    private float time = 0f;

    public TrippyVisualizer(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        final ModelBuilder modelBuilder = new ModelBuilder();

        texture = new Texture("white.png");

        spriteBatch = new SpriteBatch();
        fbo = new FrameBuffer(Pixmap.Format.RGB565, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        orthoCamera = new OrthographicCamera(GdxVr.graphics.getWidth(), GdxVr.graphics.getHeight());

        final Color[] colors = new Color[]{Color.LIME, Color.BLUE, Color.OLIVE, Color.ORANGE, Color.TAN, Color.RED, Color.PURPLE};
        for (int i = 0; i < colors.length; i++) {
            instances.add(new ModelInstance(createBox(modelBuilder, colors[i]), 0, 0, -5));
        }

        createShaderProgram();
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        PointLight light = new PointLight();
        light.setPosition(0, 1, 0);
        light.setColor(Color.WHITE);
        light.setIntensity(40f);
        lights.add(light);
    }

    private void createShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shaders/post.vertex.glsl"), Gdx.files.internal("shaders/post.fragment.glsl"));
        if (!shaderProgram.isCompiled())
            throw new GdxRuntimeException("could not compile splat batch: " + shaderProgram.getLog());
        if (shaderProgram.getLog().length() != 0)
            Gdx.app.log("PuzzleBatch", shaderProgram.getLog());
    }

    private Model createBox(ModelBuilder modelBuilder, Color c) {
        final Material material = new Material(ColorAttribute.createDiffuse(c), ColorAttribute.createAmbient(c), ColorAttribute.createSpecular(Color.WHITE));
        return modelBuilder.createBox(1f, 1f, 1f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    @Override
    public void update() {
        super.update();
        angle += Gdx.graphics.getDeltaTime() * 30f;
        for (int i = 0; i < instances.size; i++) {
            instances.get(i).transform.idt().translate(MathUtils.cosDeg(angle - i * 45f) * 2f, MathUtils.sinDeg(angle + i * 30f) * 3f, -5f - i).rotate(axis, angle + i * 60f);
        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        super.onDrawEye(eye);
        if (Gdx.graphics.getFrameId() % 60 == 0)
            Log.d("FPS", Gdx.graphics.getFramesPerSecond() + "fps");

        fbo.begin();

//        Gdx.gl.glViewport(eye.getViewport().x, eye.getViewport().y, eye.getViewport().width, eye.getViewport().height);
//        Gdx.gl.glClearColor(getBackgroundColor().r, getBackgroundColor().g, getBackgroundColor().b, getBackgroundColor().a);
////        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//        spriteBatch.setShader(null);
        spriteBatch.begin();
        spriteBatch.setShader(shaderProgram);
        shaderProgram.setUniformf("u_time", time);
//        spriteBatch.setColor(color.set(0, 0, 0, 0.05f));
        spriteBatch.setProjectionMatrix(orthoCamera.combined);

        spriteBatch.setColor(color.set(Color.WHITE));

        spriteBatch.draw(fbo.getColorBufferTexture(), eye.getViewport().x - eye.getViewport().width / 2 + 4, eye.getViewport().y - eye.getViewport().height / 2 + 4, eye.getViewport().width - 8, eye.getViewport().height - 8, eye.getViewport().x, eye.getViewport().y, eye.getViewport().width, eye.getViewport().height, false, true);

//        spriteBatch.draw(texture, -fbo.getWidth() / 2, -fbo.getHeight() / 2, fbo.getWidth(), fbo.getHeight());

//        spriteBatch.setShader(null);
//        spriteBatch.setColor(color.set(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f));
//        spriteBatch.draw(texture, Gdx.input.getX() - fbo.getWidth() / 2f - texture.getWidth() / 2f, Gdx.input.getY() - fbo.getHeight() / 2 - texture.getHeight() / 2f, texture.getWidth(), texture.getHeight(), 0, 0, texture.getWidth(), texture.getHeight(), false, true);

        spriteBatch.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        getModelBatch().begin(getVrCamera());
        getModelBatch().render(instances, getEnvironment());
        getModelBatch().end();

        fbo.end();

//        Gdx.gl.glViewport(eye.getViewport().x, eye.getViewport().y, eye.getViewport().width, eye.getViewport().height);
//        Gdx.gl.glClearColor(getBackgroundColor().r, getBackgroundColor().g, getBackgroundColor().b, getBackgroundColor().a);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        time += Gdx.graphics.getDeltaTime();
        time %= MathUtils.PI2;
        spriteBatch.setShader(null);
        spriteBatch.begin();
//        shaderProgram.setUniformf("u_time", time);
        spriteBatch.setColor(Color.WHITE);
        spriteBatch.setProjectionMatrix(orthoCamera.combined);
        spriteBatch.draw(fbo.getColorBufferTexture(), eye.getViewport().x - eye.getViewport().width / 2, eye.getViewport().y - eye.getViewport().height / 2, eye.getViewport().width, eye.getViewport().height);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        fbo.dispose();
        spriteBatch.dispose();
        texture.dispose();
    }
}
