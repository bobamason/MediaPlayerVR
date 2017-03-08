package net.masonapps.mediaplayervr.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 3/4/2017.
 */

public class DynamicSpiralMesh extends DynamicMesh {
    private static final float DELAY = 0.03333f * 2f;
    private static final int FLOATS_PER_VERTEX = 3 + 3 + 4;
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    private static final Vector3 tmp3 = new Vector3();
    private static final Vector3 normal = new Vector3();
    private static final Color tmpColor = new Color();
    private final int numPoints;
    private final float[] tmpVertices;
    private ArrayList<MeshPartBuilder.VertexInfo> shape = new ArrayList<>();
    private ArrayList<MeshPartBuilder.VertexInfo> lastShape = new ArrayList<>();
    private Vector3 pos = new Vector3();
    private Vector3 lastPos = new Vector3();
    private float t = 1;
    private int offset = 0;
    private float radius;
    private float startAngle;
    private Color color = Color.RED.cpy();
    private Polygon polygon;
    private float z = 0f;

    public DynamicSpiralMesh(Polygon polygon) {
        this(1000, polygon);
    }

    public DynamicSpiralMesh(int numVertices, Polygon polygon) {
        this(numVertices, polygon, 0f, 1f);
    }

    public DynamicSpiralMesh(Polygon polygon, float startAngle, float radius) {
        this(1000, polygon, startAngle, radius);
    }

    public DynamicSpiralMesh(int numVertices, Polygon polygon, float startAngle, float radius) {
        super(numVertices * FLOATS_PER_VERTEX, numVertices, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
        final short[] indices = new short[numVertices];
        for (short i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        setIndices(indices);
        setVertices(new float[getMaxVertices()]);
        this.polygon = polygon;
        numPoints = polygon.getVertices().length / 2;
        tmpVertices = new float[numPoints * 6 * FLOATS_PER_VERTEX];
        this.startAngle = startAngle;
        this.radius = radius;
        fillVertexList(shape, numPoints);
        fillVertexList(lastShape, numPoints);
        final float[] polygonVertices = polygon.getTransformedVertices();
        for (int i = 0; i < polygonVertices.length; i += 2) {
            MeshPartBuilder.VertexInfo v = lastShape.get(i / 2);
            v.position.set(polygonVertices[i], polygonVertices[i + 1], 0);
        }
        pos.set(radius * MathUtils.cos(startAngle), radius * MathUtils.sin(startAngle), 0);
        lastPos.set(pos);
        translate(lastShape, lastPos);
    }

    private static void makeRectFace(float[] vertices, int start, MeshPartBuilder.VertexInfo v1a, MeshPartBuilder.VertexInfo v2a, MeshPartBuilder.VertexInfo v1b, MeshPartBuilder.VertexInfo v2b, boolean flip) {
        tmp2.set(v2a.position).sub(v1a.position).nor();
        tmp3.set(v1b.position).sub(v1a.position).nor();
        normal.set(tmp3).crs(tmp2);
        if (flip) {
            normal.scl(-1f);
            v1a.setNor(normal);
            v1b.setNor(normal);
            v2a.setNor(normal);
            v2b.setNor(normal);
            setVertex(vertices, start, v1a);
            setVertex(vertices, start + FLOATS_PER_VERTEX, v2a);
            setVertex(vertices, start + FLOATS_PER_VERTEX * 2, v2b);

            setVertex(vertices, start + FLOATS_PER_VERTEX * 3, v1a);
            setVertex(vertices, start + FLOATS_PER_VERTEX * 4, v2b);
            setVertex(vertices, start + FLOATS_PER_VERTEX * 5, v1b);
        } else {
            v1a.setNor(normal);
            v1b.setNor(normal);
            v2a.setNor(normal);
            v2b.setNor(normal);
            setVertex(vertices, start, v2a);
            setVertex(vertices, start + FLOATS_PER_VERTEX, v1a);
            setVertex(vertices, start + FLOATS_PER_VERTEX * 2, v1b);

            setVertex(vertices, start + FLOATS_PER_VERTEX * 3, v2a);
            setVertex(vertices, start + FLOATS_PER_VERTEX * 4, v1b);
            setVertex(vertices, start + FLOATS_PER_VERTEX * 5, v2b);
        }
    }

    private static void setVertex(float[] vertices, int start, MeshPartBuilder.VertexInfo v) {
        vertices[start] = v.position.x;
        vertices[start + 1] = v.position.y;
        vertices[start + 2] = v.position.z;
        vertices[start + 3] = v.normal.x;
        vertices[start + 4] = v.normal.y;
        vertices[start + 5] = v.normal.z;
        vertices[start + 6] = v.color.r;
        vertices[start + 7] = v.color.g;
        vertices[start + 8] = v.color.b;
        vertices[start + 9] = v.color.a;
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

    private static void translate(List<MeshPartBuilder.VertexInfo> vertexInfos, Vector3 trans) {
        for (int i = 0; i < vertexInfos.size(); i++) {
            vertexInfos.get(i).position.add(trans);
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
    public void update(float deltaTime) {
        t += deltaTime;
        z -= deltaTime * 10;
        if (t > DELAY) {
            float a = z * 0.125f + startAngle;
            pos.set(radius * MathUtils.cos(a), radius * MathUtils.sin(a), z);
            tmpColor.set(color);
//            tmpColor.set(MathUtils.cos(a + color.r * MathUtils.PI2) * 0.5f + 0.5f, MathUtils.cos(a + color.g * MathUtils.PI2) * 0.5f + 0.5f, MathUtils.cos(a + color.b * MathUtils.PI2) * 0.5f + 0.5f, 1f);
            extrude(lastShape, tmp.set(pos).sub(lastPos), shape);
//            rotate(shape, Vector3.Z, 30 * DELAY);
            setColors(shape, tmpColor);
//                setColors(lastShape, tmpColor);
            final int n = shape.size() * 6 * FLOATS_PER_VERTEX;
            offset += n;
            if (offset >= getMaxVertices() - n) offset = 0;
            for (int i = 0; i < shape.size(); i++) {
                final int i2 = (i + 1) % shape.size();
                final MeshPartBuilder.VertexInfo v1a = lastShape.get(i);
                final MeshPartBuilder.VertexInfo v2a = lastShape.get(i2);
                final MeshPartBuilder.VertexInfo v1b = shape.get(i);
                final MeshPartBuilder.VertexInfo v2b = shape.get(i2);
                makeRectFace(tmpVertices, i * 6 * FLOATS_PER_VERTEX, v1a, v2a, v1b, v2b, false);
            }
            updateVertices(offset, tmpVertices);
            t = 0f;
            lastPos.set(pos);
            setVertexList(shape, lastShape);
        }
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public void setColor(float r, float g, float b) {
        this.color.set(r, g, b, 1f);
    }

    public float getZ() {
        return z;
    }
}
