package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.values.PointSpawnShapeValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;


/**
 * Created by Bob on 12/15/2016.
 */

public class ParticlesVisualizerScreen extends MusicVisualizerScreen {

    public static final String PARTICLE_FILE_NAME = "visualizer/particle.png";
    private final Entity highlightEntity;
    private final Entity controllerEntity;
    BillboardParticleBatch particleBatch;
    private Array<ParticleController> emitters = new Array<>();
    private ColorInfluencer.Single colorInfluencer;
    private float a = 0;
    private Vector3 position = new Vector3();
    private boolean isTouchpadClicked = false;
    private ScaleInfluencer scaleInfluencer;

    public ParticlesVisualizerScreen(VrGame game, Context context, SongDetails songDetails) {
        super(game, context, songDetails);
        particleBatch = new BillboardParticleBatch(ParticleShader.AlignMode.ViewPoint, false, 1000);
        particleBatch.setCamera(getVrCamera());
        final MediaPlayerGame mediaPlayerGame = (MediaPlayerGame) game;
        getWorld().add(mediaPlayerGame.getRoomEntity());
        getWorld().add(mediaPlayerGame.getFloorEntity());
        highlightEntity = getWorld().add(mediaPlayerGame.getHighlightEntity());
        controllerEntity = getWorld().add(mediaPlayerGame.getControllerEntity());
        loadAsset(PARTICLE_FILE_NAME, Texture.class);
    }

    @Override
    public void show() {
        GdxVr.app.getGvrView().setNeckModelEnabled(true);
        GdxVr.app.getGvrView().setNeckModelFactor(1f);
    }

    @Override
    public void hide() {

    }

    @Override
    protected void doneLoading(AssetManager assets) {
        Texture particleTexture = assets.get(PARTICLE_FILE_NAME, Texture.class);
        ParticleController controller = createBillboardController(Color.GREEN, particleTexture);
        controller.init();
        controller.start();
        controller.setTranslation(position.set(0, 0, -4));
        emitters.add(controller);
    }

    private ParticleController createBillboardController(Color color, Texture particleTexture) {
        //Emission
        RegularEmitter emitter = new RegularEmitter();
        emitter.getDuration().setLow(3000);
        emitter.getEmission().setHigh(2900);
        emitter.getLife().setHigh(1000);
        emitter.setMaxParticleCount(3000);

        //Spawn
        final PointSpawnShapeValue pointSpawnShapeValue = new PointSpawnShapeValue();
        pointSpawnShapeValue.xOffsetValue.setLow(0, 1f);
        pointSpawnShapeValue.xOffsetValue.setActive(true);
        pointSpawnShapeValue.yOffsetValue.setLow(0, 1f);
        pointSpawnShapeValue.yOffsetValue.setActive(true);
        pointSpawnShapeValue.zOffsetValue.setLow(0, 1f);
        pointSpawnShapeValue.zOffsetValue.setActive(true);
        SpawnInfluencer spawnSource = new SpawnInfluencer(pointSpawnShapeValue);

        //Scale
        scaleInfluencer = new ScaleInfluencer();
        scaleInfluencer.value.setTimeline(new float[]{0, 1});
        scaleInfluencer.value.setScaling(new float[]{1, 0});
        scaleInfluencer.value.setLow(0);
        scaleInfluencer.value.setHigh(1);

        //Color
        colorInfluencer = new ColorInfluencer.Single();
        colorInfluencer.colorValue.setColors(new float[]{color.r, color.g, color.b, 0, 0, 0});
        colorInfluencer.colorValue.setTimeline(new float[]{0, 1});
        colorInfluencer.alphaValue.setHigh(1);
        colorInfluencer.alphaValue.setTimeline(new float[]{0, 0.5f, 0.8f, 1});
        colorInfluencer.alphaValue.setScaling(new float[]{0, 0.15f, 0.5f, 0});

        //Dynamics
        DynamicsInfluencer dynamicsInfluencer = new DynamicsInfluencer();
        DynamicsModifier.BrownianAcceleration modifier = new DynamicsModifier.BrownianAcceleration();
        modifier.strengthValue.setTimeline(new float[]{0, 1});
        modifier.strengthValue.setScaling(new float[]{0, 1});
        modifier.strengthValue.setHigh(80);
        modifier.strengthValue.setLow(1, 5);
        dynamicsInfluencer.velocities.add(modifier);

        return new ParticleController("Billboard Controller", emitter, new BillboardRenderer(particleBatch),
                new RegionInfluencer.Single(particleTexture),
                spawnSource,
                scaleInfluencer,
                colorInfluencer,
                dynamicsInfluencer
        );
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        if (isLoading()) return;
        particleBatch.begin();
        a += GdxVr.graphics.getDeltaTime();
        for (ParticleController emitter : emitters) {
            colorInfluencer.colorValue.setColors(new float[]{MathUtils.sin(a) * 0.5f + 0.5f, 1f, MathUtils.cos(a) * 0.5f + 0.5f, 0, 0, 0});
//            scaleInfluencer.value.setScaling(new float[]{spectrumAnalyzer.getAmplitude(0) / 127f});
            emitter.setTranslation(position);
            emitter.update();
            emitter.draw();
        }
        particleBatch.end();
        getModelBatch().begin(camera);
        getModelBatch().render(particleBatch);
        getModelBatch().end();
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (GdxVr.input.isControllerConnected()) {
            position.set(GdxVr.input.getInputRay().direction).scl(5f).add(GdxVr.input.getInputRay().origin);
            isTouchpadClicked = controller.clickButtonState;
        }
    }
}
