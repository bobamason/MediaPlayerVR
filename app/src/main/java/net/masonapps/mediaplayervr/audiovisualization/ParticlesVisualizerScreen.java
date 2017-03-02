package net.masonapps.mediaplayervr.audiovisualization;

import android.content.Context;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
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
import com.badlogic.gdx.graphics.g3d.particles.values.EllipseSpawnShapeValue;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;


/**
 * Created by Bob on 12/15/2016.
 */

public class ParticlesVisualizerScreen extends MusicVisualizerScreen {

    public static final String PARTICLE_FILE_NAME = "visualizer/particle.png";
    private static final float ALPHA = 0.005f;
    private BillboardParticleBatch particleBatch;
    private Array<ParticleController> emitters = new Array<>();
    private ColorInfluencer.Single colorInfluencer;
    private float a = 0;
    private Vector3 position = new Vector3();
    private boolean isTouchpadClicked = false;
    private ScaleInfluencer scaleInfluencer;
    private float[] colors;
    private float[] scaling;

    public ParticlesVisualizerScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        particleBatch = new BillboardParticleBatch(ParticleShader.AlignMode.ViewPoint, true, 1000);
        particleBatch.setCamera(getVrCamera());
        loadAsset(PARTICLE_FILE_NAME, Texture.class);
    }

    @Override
    protected void doneLoading(AssetManager assets) {
        Texture particleTexture = assets.get(PARTICLE_FILE_NAME, Texture.class);
        manageDisposable(particleTexture);
        particleBatch.setTexture(particleTexture);
        ParticleController controller = createBillboardController(particleTexture);
        controller.init();
        controller.start();
        emitters.add(controller);
    }

    private ParticleController createBillboardController(Texture particleTexture) {
        //Emission
        RegularEmitter emitter = new RegularEmitter();
        emitter.getDuration().setLow(3000);
        emitter.getEmission().setHigh(1900);
        emitter.getLife().setHigh(1000);
        emitter.setMaxParticleCount(2000);

        //Spawn
        final EllipseSpawnShapeValue ellipseSpawnShapeValue = new EllipseSpawnShapeValue();
        ellipseSpawnShapeValue.xOffsetValue.setLow(0, 1.5f);
        ellipseSpawnShapeValue.xOffsetValue.setActive(true);
        ellipseSpawnShapeValue.yOffsetValue.setLow(0, 1.5f);
        ellipseSpawnShapeValue.yOffsetValue.setActive(true);
        ellipseSpawnShapeValue.zOffsetValue.setLow(0, 0.015f);
        ellipseSpawnShapeValue.zOffsetValue.setActive(true);
        ellipseSpawnShapeValue.setEdges(true);
        SpawnInfluencer spawnSource = new SpawnInfluencer(ellipseSpawnShapeValue);

        //Scale
        scaleInfluencer = new ScaleInfluencer();
        scaleInfluencer.value.setTimeline(new float[]{0, 1});
        scaling = new float[]{0.1f, 0};
        scaleInfluencer.value.setScaling(scaling);
        scaleInfluencer.value.setLow(0);
        scaleInfluencer.value.setHigh(1);

        //Color
        colorInfluencer = new ColorInfluencer.Single();
        colors = new float[]{0.25f, 1f, 1f, 0, 0, 0};
        colorInfluencer.colorValue.setColors(colors);
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

        return new ParticleController("controller", emitter, new BillboardRenderer(particleBatch),
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
//        a += GdxVr.graphics.getDeltaTime();
//        a %= MathUtils.PI2;
        getModelBatch().begin(camera);
        particleBatch.begin();
        for (ParticleController emitter : emitters) {
            colors[0] = 1f - intensityValues[0];
            colors[1] = intensityValues[1];
            colors[2] = intensityValues[2];
//            colorInfluencer.colorValue.setColors(colors);
            scaling[0] = intensityValues[0] * 0.25f + 0.05f;
//            scaleInfluencer.value.setScaling(scaling);
            emitter.setTranslation(position);
            emitter.update();
            emitter.draw();
        }
        particleBatch.end();
        getModelBatch().render(particleBatch);
        getModelBatch().end();
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (GdxVr.input.isControllerConnected()) {
            position.set(getControllerRay().direction).scl(3f).add(getControllerRay().origin);
            isTouchpadClicked = controller.clickButtonState;
        }
    }
}
