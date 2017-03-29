package net.masonapps.mediaplayervr.audiovisualization.tests;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/27/2017.
 */

public class DecalsPerformanceTest extends MusicVisualizerScreen {

    public static final float SPACING = 2f;
    public static final int MAX_LAYERS = 100;
    public static final float SPEED = 5f;
    public static final String TAG = DecalsPerformanceTest.class.getSimpleName();
    private final Array<Array<Decal>> decals = new Array<>();
    private DecalBatch decalBatch;
    private TextureRegion textureRegion;
    private float lastLayerZ = 0f;
    private int resetIndex = -1;

    public DecalsPerformanceTest(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        setBackgroundColor(Color.BLACK);
        decalBatch = new DecalBatch(new CameraGroupStrategy(getVrCamera()));
        manageDisposable(decalBatch);
        final Texture texture = new Texture("visualizer/particle.png");
        manageDisposable(texture);
        textureRegion = new TextureRegion(texture);
        final Array<Decal> array = new Array<>();
        addLayer(textureRegion, array, MathUtils.random(8, 12), MathUtils.random(4f, 6f), 0);
        decals.add(array);
    }

    private static void addLayer(TextureRegion textureRegion, Array<Decal> array, int n, float r, float z) {
        final float aStep = MathUtils.PI2 / n;
        final Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
        for (int i = 0; i < n; i++) {
            final Decal decal = Decal.newDecal(2f, 2f, textureRegion, GL20.GL_ONE, GL20.GL_ONE);
            final float a = aStep * i;
            decal.setPosition(MathUtils.cos(a) * r, MathUtils.sin(a) * r, z);
            decal.setColor(color);
            array.add(decal);
        }
    }

    private static void resetLayer(Array<Decal> array, float r, float z) {
        final float aStep = MathUtils.PI2 / array.size;
        final Color color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);
        for (int i = 0; i < array.size; i++) {
            final float a = aStep * i;
            final Decal decal = array.get(i);
            decal.setPosition(MathUtils.cos(a) * r, MathUtils.sin(a) * r, z);
            decal.rotateZ(MathUtils.random(360));
            decal.setColor(color);
        }
    }

    @Override
    public void update() {
        super.update();
        getVrCamera().position.z -= Gdx.graphics.getDeltaTime() * SPEED;

        if (getVrCamera().position.z - 100f < lastLayerZ - SPACING) {
            final float z = lastLayerZ - SPACING;
            if (decals.size >= MAX_LAYERS) {
                resetIndex++;
                resetIndex %= decals.size;
                resetLayer(decals.get(resetIndex), MathUtils.random(4f, 6f), z);

            } else {
                final Array<Decal> array = new Array<>();
                addLayer(textureRegion, array, MathUtils.random(8, 12), MathUtils.random(4f, 6f), z);
                decals.add(array);
            }
            lastLayerZ = z;
        }
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);

        int count = 0;
        for (int i = 0; i < decals.size; i++) {
            final Array<Decal> subList = decals.get(i);
            for (int j = 0; j < subList.size; j++) {
                final Decal decal = subList.get(j);
                decal.lookAt(getVrCamera().position, Vector3.Y);
                decalBatch.add(decal);
                count++;
            }
        }
        decalBatch.flush();
        
        if (Gdx.graphics.getFrameId() % 120 == 0) {
            Log.i(TAG, Gdx.graphics.getFramesPerSecond() + "fps");
            Log.i(TAG, "count: " + count);
        }
    }
}
