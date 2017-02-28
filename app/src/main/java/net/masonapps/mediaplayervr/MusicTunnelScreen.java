package net.masonapps.mediaplayervr;

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
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 2/27/2017.
 */

public class MusicTunnelScreen extends MusicVisualizerScreen {
    private static final int MAX_INDICES = 3000;
    private static final int FLOATS_PER_VERTEX = 3 + 4;
    private static final int MAX_VERTICES = MAX_INDICES * FLOATS_PER_VERTEX;
    private static final float DELAY = 0.125f;
    private static final Vector3 tmp = new Vector3();
    private static final Color tmpColor = new Color();
    private final Entity entity;
    private float z = 0;
    private short[] indices = new short[MAX_INDICES];
    private Mesh mesh;
    private int offset = 0;
    private float t = 1;
    private float[] vertices = new float[MAX_VERTICES];
    private ArrayList<MeshPartBuilder.VertexInfo> shape = new ArrayList<>();
    private ArrayList<MeshPartBuilder.VertexInfo> lastShape = new ArrayList<>();
    private float lastZ = 0f;

    public MusicTunnelScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        fillVertexList(shape, 12);
        fillVertexList(lastShape, 12);
        final float aStep = MathUtils.PI2 / lastShape.size();
        for (int i = 0; i < lastShape.size(); i++) {
            MeshPartBuilder.VertexInfo v = lastShape.get(i);
            float a = aStep * i;
            v.position.set((i % 2 == 0 ? 2f : 4f) * MathUtils.cos(a), (i % 2 == 0 ? 2f : 4f) * MathUtils.sin(a), 0);
            v.color.set(tmpColor);
        }

        mesh = new Mesh(false, MAX_VERTICES, MAX_INDICES, VertexAttribute.Position(), VertexAttribute.ColorUnpacked());

        for (short i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        mesh.setIndices(indices);
        mesh.setVertices(vertices);

        final ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("mesh", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        entity = getWorld().add(new Entity(new ModelInstance(modelBuilder.end())));
        entity.setLightingEnabled(false);
    }

    private static void makeRectFace(float[] vertices, int start, MeshPartBuilder.VertexInfo v1a, MeshPartBuilder.VertexInfo v2a, MeshPartBuilder.VertexInfo v1b, MeshPartBuilder.VertexInfo v2b) {
        setVertex(vertices, start, v1a);
        setVertex(vertices, start + FLOATS_PER_VERTEX, v2a);
        setVertex(vertices, start + FLOATS_PER_VERTEX * 2, v2b);

        setVertex(vertices, start + FLOATS_PER_VERTEX * 3, v1a);
        setVertex(vertices, start + FLOATS_PER_VERTEX * 4, v2b);
        setVertex(vertices, start + FLOATS_PER_VERTEX * 5, v1b);
    }

    private static void setVertex(float[] vertices, int start, MeshPartBuilder.VertexInfo v) {
        vertices[start] = v.position.x;
        vertices[start + 1] = v.position.y;
        vertices[start + 2] = v.position.z;
        vertices[start + 3] = v.color.r;
        vertices[start + 4] = v.color.g;
        vertices[start + 5] = v.color.b;
        vertices[start + 6] = v.color.a;
    }

    private static void extrude(List<MeshPartBuilder.VertexInfo> src, Vector3 v, List<MeshPartBuilder.VertexInfo> dst) {
        for (int i = 0; i < src.size(); i++) {
            dst.get(i).position.set(src.get(i).position).add(v);
        }
    }

    private static void rotate(List<MeshPartBuilder.VertexInfo> vertexInfos, Vector3 axis, float angle) {
        for (int i = 0; i < vertexInfos.size(); i++) {
            vertexInfos.get(i).position.rotate(axis, angle);
        }
    }

    private static void fillVertexList(List<MeshPartBuilder.VertexInfo> vecs, int n) {
        vecs.clear();
        for (int i = 0; i < n; i++) {
            vecs.add(i, new MeshPartBuilder.VertexInfo());
        }
    }

    private static void setVertexList(List<MeshPartBuilder.VertexInfo> src, List<MeshPartBuilder.VertexInfo> dst) {
        for (int i = 0; i < src.size(); i++) {
            dst.get(i).set(src.get(i));
        }
    }

    private static void setColors(List<MeshPartBuilder.VertexInfo> vertexInfos, Color color) {
        for (MeshPartBuilder.VertexInfo vertexInfo : vertexInfos) {
            vertexInfo.color.set(color);
        }
    }

    @Override
    public void update() {
        super.update();
        t += Math.min(Gdx.graphics.getDeltaTime(), 0.03333f);
        getVrCamera().position.z -= Math.min(Gdx.graphics.getDeltaTime(), 0.03333f) * 10;
        if (t > DELAY) {
            z = getVrCamera().position.z - 30;
            tmpColor.set(1f - intensityValues[0], intensityValues[1], intensityValues[2] * 0.5f + 0.5f, 1f);
            extrude(lastShape, tmp.set(0, 0, z - lastZ), shape);
            rotate(shape, Vector3.Z, 30 * DELAY);
            setColors(shape, tmpColor);
            setColors(lastShape, tmpColor);
            final int n = shape.size() * 6 * FLOATS_PER_VERTEX;
            offset += n;
            if (offset >= MAX_VERTICES - n) offset = 0;
            for (int i = 0; i < shape.size(); i++) {
                final int i2 = (i + 1) % shape.size();
                final MeshPartBuilder.VertexInfo v1a = lastShape.get(i);
                final MeshPartBuilder.VertexInfo v2a = lastShape.get(i2);
                final MeshPartBuilder.VertexInfo v1b = shape.get(i);
                final MeshPartBuilder.VertexInfo v2b = shape.get(i2);
                makeRectFace(vertices, offset + i * 6 * FLOATS_PER_VERTEX, v1a, v2a, v1b, v2b);
            }
            mesh.updateVertices(offset, vertices, offset, n);
            t = 0f;
            lastZ = z;
            setVertexList(shape, lastShape);
        }
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        if (GdxVr.input.isControllerConnected()) {
        }
    }
}
