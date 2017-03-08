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
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.gfx.DynamicSpiralMesh;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 2/27/2017.
 */

public class MusicTunnelScreen extends MusicVisualizerScreen {

    private static final Vector3 tmp = new Vector3();
    private Array<ModelInstance> instances = new Array<>();
    private ArrayList<DynamicSpiralMesh> stripMeshs = new ArrayList<>();
    private Environment enviroment;
    private ModelInstance starsInstance;

    public MusicTunnelScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
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

        int n = 6;
        float invN = 1f / n;
        float[] polygonVerts = new float[12];
        for (int i = 0; i < 6; i++) {
            polygonVerts[i * 2] = (i % 2 == 0 ? 0.125f : 0.4f) * MathUtils.cos(i * MathUtils.PI / 3f);
            polygonVerts[i * 2 + 1] = (i % 2 == 0 ? 0.125f : 0.4f) * MathUtils.sin(i * MathUtils.PI / 3f);
        }
        Polygon polygon = new Polygon(polygonVerts);
        for (int i = 0; i < n; i++) {
            DynamicSpiralMesh strip = new DynamicSpiralMesh(5000, polygon, MathUtils.PI2 / 6f * i, i % 2 == 0 ? 3f : 6f);
            strip.setColor(new Color(1f - invN * i, invN * i, invN * i * 0.5f + 0.5f, 1f));
            stripMeshs.add(strip);

            modelBuilder.begin();
            modelBuilder.part("mesh" + i, strip, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.WHITE), ColorAttribute.createAmbient(Color.WHITE), ColorAttribute.createSpecular(Color.WHITE)));
            instances.add(new ModelInstance(modelBuilder.end()));
        }
    }

    @Override
    public void update() {
        super.update();
        final float dT = Math.min(Gdx.graphics.getDeltaTime(), 0.03333f);
        int i = 0;
        for (DynamicSpiralMesh strip : stripMeshs) {
            if (i % 3 == 0)
                strip.setColor(intensityValues[0], 1f - intensityValues[0], 0.5f * intensityValues[0] + 0.5f);
            else if (i % 2 == 0)
                strip.setColor(0.5f - intensityValues[1] * 0.5f, intensityValues[1], 1f - intensityValues[1]);
            else
                strip.setColor(1f - intensityValues[2], 0.5f * intensityValues[2] + 0.5f, intensityValues[2]);
            strip.update(dT);
            getVrCamera().position.z = strip.getZ() + 50;
            i++;
        }
        starsInstance.transform.setToTranslation(tmp.set(getVrCamera().position).scl(0.5f));
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        getModelBatch().render(starsInstance);
        getModelBatch().render(instances, enviroment);
        getModelBatch().end();
    }

    @Override
    public void dispose() {
        super.dispose();
        for (DynamicSpiralMesh stripMesh : stripMeshs) {
            try {
                stripMesh.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
