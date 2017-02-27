package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 2/27/2017.
 */

public class MusicTunnelScreen extends MusicVisualizerScreen {
    public static final int FLOATS_PER_FACE = 3 * 3;
    private static final int MAX_VERTICES = 40;
    private static final int MAX_INDICES = 40;
    private static final float DELAY = 0.5f;
    private final Entity entity;
    private float[] vertices = new float[MAX_VERTICES * 4];
    private short[] indices = new short[MAX_INDICES];
    private int index = 0;
    private float time = 0f;
    private Vector3 tmp = new Vector3();
    private Vector3 tmp2 = new Vector3();
    private Vector3 vec = new Vector3();
    private Vector3 lastVec = new Vector3();
    private Vector3 vec2 = new Vector3();
    private Vector3 vec3 = new Vector3();

    public MusicTunnelScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        final Mesh mesh = new Mesh(false, MAX_VERTICES, MAX_INDICES, VertexAttribute.Position());
        mesh.setVertices(vertices);
        for (int i = 0; i < MAX_INDICES; i++) {
            indices[i] = (short) i;
        }
        mesh.setIndices(indices);
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.RED), ColorAttribute.createAmbient(Color.RED)));
        entity = getWorld().add(new Entity(new ModelInstance(modelBuilder.end())));
        entity.setLightingEnabled(false);
    }

    private static void setTriangle(float[] vertices, int start, Vector3 v, Vector3 v2, Vector3 v3) {
        vertices[start] = v.x;
        vertices[start + 1] = v.y;
        vertices[start + 2] = v.z;

        vertices[start + 3] = v2.x;
        vertices[start + 4] = v2.y;
        vertices[start + 5] = v2.z;

        vertices[start + 6] = v3.x;
        vertices[start + 7] = v3.y;
        vertices[start + 8] = v3.z;
    }

    @Override
    public void update() {
        super.update();
        final Mesh mesh = entity.modelInstance.model.meshes.get(0);
        time += GdxVr.graphics.getDeltaTime();
        if (time > DELAY) {
            index += FLOATS_PER_FACE;
            index %= MAX_VERTICES - FLOATS_PER_FACE;
            tmp.set(getControllerRay().origin).sub(vec).nor();
            tmp2.set(lastVec).sub(vec).nor();
            vec2.set(lastVec);
            vec3.set(tmp.crs(tmp2)).scl(0.1f).add(vec);
            setTriangle(vertices, index, vec, vec2, vec3);
            mesh.updateVertices(index, vertices, index, FLOATS_PER_FACE);
            lastVec.set(vec);
            time = 0f;
        }
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        if (GdxVr.input.isControllerConnected()) {
            vec.set(getControllerRay().direction).scl(3f).add(getControllerRay().origin);
        }
    }
}
