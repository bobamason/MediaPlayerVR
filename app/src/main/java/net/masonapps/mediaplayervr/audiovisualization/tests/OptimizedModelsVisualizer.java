package net.masonapps.mediaplayervr.audiovisualization.tests;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/4/2017.
 */

public class OptimizedModelsVisualizer extends MusicVisualizerScreen {

    private static final Vector3 tmp = new Vector3();
    private Array<ModelInstance> instances = new Array<>();
    private Environment enviroment;
    private ModelInstance starsInstance;


    public OptimizedModelsVisualizer(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        setUiVisible(false);
        enviroment = new Environment();
        enviroment.set(ColorAttribute.createAmbient(Color.DARK_GRAY));
        DirectionalLight light = new DirectionalLight();
        light.setDirection(tmp.set(0.05f, -0.1f, -1f).nor());
        light.setColor(Color.WHITE);
        enviroment.add(light);

        final ModelBuilder modelBuilder = new ModelBuilder();

        int numStars = 2000;
        final Mesh stars = new Mesh(true, numStars * 3, numStars, VertexAttribute.Position());
        final float[] vertices = new float[numStars * 3];
        final short[] indices = new short[numStars];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
            final float a = MathUtils.random(0, MathUtils.PI2);
            final float r = MathUtils.random(10, 40);
            final float z = MathUtils.random(-1000, 40);
            vertices[i * 3] = r * MathUtils.cos(a);
            vertices[i * 3 + 1] = r * MathUtils.sin(a);
            vertices[i * 3 + 2] = z;
        }
        stars.setIndices(indices);
        stars.setVertices(vertices);

        modelBuilder.begin();
        modelBuilder.part("stars", stars, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        starsInstance = new ModelInstance(modelBuilder.end());
        manageDisposable(starsInstance.model);
    }

    @Override
    public void update() {
        super.update();
        final float dT = Math.min(Gdx.graphics.getDeltaTime(), 0.03333f);
        starsInstance.transform.setToTranslation(tmp.set(getVrCamera().position).scl(0.5f));
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        getModelBatch().render(starsInstance);
//        getModelBatch().render(instances, enviroment);
        getModelBatch().end();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
