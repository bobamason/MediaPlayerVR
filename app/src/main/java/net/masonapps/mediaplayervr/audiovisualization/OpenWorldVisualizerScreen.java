package net.masonapps.mediaplayervr.audiovisualization;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/10/2017.
 */

public class OpenWorldVisualizerScreen extends MusicVisualizerScreen {

    private final Vector3 velocity = new Vector3();
    private final Vector3 acceleration = new Vector3();
    private final Vector3 steeringForce = new Vector3();
    private ModelInstance starsInstance;

    public OpenWorldVisualizerScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);

        final ModelBuilder modelBuilder = new ModelBuilder();

        int numStars = 6000;
        final Mesh stars = new Mesh(true, numStars * 3, numStars, VertexAttribute.Position());
        final float[] vertices = new float[numStars * 3];
        final short[] indices = new short[numStars];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
//            final float a = MathUtils.random(0, MathUtils.PI2);
//            final float r = MathUtils.random(10, 40);
            final float z = MathUtils.random(-500, 500);
            final float x = MathUtils.random(-500, 500);
            final float y = MathUtils.random(-500, 500);
//            vertices[i * 3] = r * MathUtils.cos(a);
//            vertices[i * 3 + 1] = r * MathUtils.sin(a);
            vertices[i * 3] = x;
            vertices[i * 3 + 1] = y;
            vertices[i * 3 + 2] = z;
        }
        stars.setIndices(indices);
        stars.setVertices(vertices);

        modelBuilder.begin();
        modelBuilder.part("stars", stars, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        starsInstance = new ModelInstance(modelBuilder.end());
    }

    @Override
    public void update() {
        super.update();
        final float dT = Math.min(Gdx.graphics.getDeltaTime(), 0.03333f);
//        acceleration.scl(0);
//        acceleration.add(steeringForce);
//        velocity.add(acceleration);
        velocity.add(getForwardVector()).scl(dT * 10f);
        getVrCamera().position.add(velocity);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        getModelBatch().render(starsInstance);
        getModelBatch().end();
    }
}
