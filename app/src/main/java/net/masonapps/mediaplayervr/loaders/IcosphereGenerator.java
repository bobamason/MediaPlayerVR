package net.masonapps.mediaplayervr.loaders;

import android.util.Log;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bob on 5/23/2017.
 */

public class IcosphereGenerator {
    private final Array<Facet> faces = new Array<>();
    private final Array<Vertex> vertexArray = new Array<>();
    private int subdivisions = 3;
    private float radius = 1;

    @SuppressWarnings("SuspiciousNameCombination")
    private static Vertex createVertex(float x, float y, float z, float radius) {
        final Vertex vertex = new Vertex();
        vertex.position.set(x, y, z).nor().scl(radius);
        vertex.uv.set(MathUtils.atan2(x, -z) / MathUtils.PI * 0.5f + 0.5f, (float) Math.acos(y) / MathUtils.PI);
        return vertex;
    }

    private void build() {
        float t = (float) (0.5 + Math.sqrt(5.0) / 2.0);

        vertexArray.add(createVertex(-1, t, 0, radius));
        vertexArray.add(createVertex(1, t, 0, radius));
        vertexArray.add(createVertex(-1, -t, 0, radius));
        vertexArray.add(createVertex(1, -t, 0, radius));

        vertexArray.add(createVertex(0, -1, t, radius));
        vertexArray.add(createVertex(0, 1, t, radius));
        vertexArray.add(createVertex(0, -1, -t, radius));
        vertexArray.add(createVertex(0, 1, -t, radius));

        vertexArray.add(createVertex(t, 0, -1, radius));
        vertexArray.add(createVertex(t, 0, 1, radius));
        vertexArray.add(createVertex(-t, 0, -1, radius));
        vertexArray.add(createVertex(-t, 0, 1, radius));

        for (int i = 0; i < vertexArray.size; i++) {
            vertexArray.get(i).index = i;
        }
        faces.add(new Facet(0, 11, 5));
        faces.add(new Facet(0, 5, 1));
        faces.add(new Facet(0, 1, 7));
        faces.add(new Facet(0, 7, 10));
        faces.add(new Facet(0, 10, 11));

        faces.add(new Facet(1, 5, 9));
        faces.add(new Facet(5, 11, 4));
        faces.add(new Facet(11, 10, 2));
        faces.add(new Facet(10, 7, 6));
        faces.add(new Facet(7, 1, 8));

        faces.add(new Facet(3, 9, 4));
        faces.add(new Facet(3, 4, 2));
        faces.add(new Facet(3, 2, 6));
        faces.add(new Facet(3, 6, 8));
        faces.add(new Facet(3, 8, 9));

        faces.add(new Facet(4, 9, 5));
        faces.add(new Facet(2, 4, 11));
        faces.add(new Facet(6, 2, 10));
        faces.add(new Facet(8, 6, 7));
        faces.add(new Facet(9, 8, 1));

        Log.i(IcosphereGenerator.class.getSimpleName(), "vertex count: " + vertexArray.size + " face count: " + faces.size);
        for (int i = 0; i < subdivisions; i++) {
            subdivide();
            Log.i(IcosphereGenerator.class.getSimpleName(), "subdivision: " + (i + 1) + " vertex count: " + vertexArray.size + " face count: " + faces.size);
        }
    }

    private void subdivide() {
//        final Array<Triangle> newTris = new Array<>();
        final Array<Facet> newFaces = new Array<>();
        final Array<Vertex> newVerts = new Array<>();
        int i = 0;

        for (Facet face : faces) {
            final Vertex a = vertexArray.get(face.a);
            float r = a.position.len();
            final Vertex b = vertexArray.get(face.b);
            final Vertex c = vertexArray.get(face.c);
            Vertex ab = new Vertex(a).lerp(b, 0.5f);
            ab.position.nor().scl(r);
            Vertex bc = new Vertex(b).lerp(c, 0.5f);
            bc.position.nor().scl(r);
            Vertex ca = new Vertex(c).lerp(a, 0.5f);
            ca.position.nor().scl(r);

//            newTris.add(new Triangle(a, ab, ca));
//            newTris.add(new Triangle(b, bc, ab));
//            newTris.add(new Triangle(c, ca, bc));
//            newTris.add(new Triangle(ab, bc, ca));

            int iab = newVerts.indexOf(ab, true);
            if (iab == -1) {
                iab = i++;
                newVerts.add(ab);
            }

            int ibc = newVerts.indexOf(bc, true);
            if (ibc == -1) {
                ibc = i++;
                newVerts.add(bc);
            }

            int ica = newVerts.indexOf(ca, true);
            if (ica == -1) {
                ica = i++;
                newVerts.add(ca);
            }

            int ia = newVerts.indexOf(a, true);
            if (ia == -1) {
                ia = i++;
                newVerts.add(a);
            }

            int ib = newVerts.indexOf(b, true);
            if (ib == -1) {
                ib = i++;
                newVerts.add(b);
            }

            int ic = newVerts.indexOf(c, true);
            if (ic == -1) {
                ic = i++;
                newVerts.add(c);
            }

            newFaces.add(new Facet(ia, iab, ica));
            newFaces.add(new Facet(ib, ibc, iab));
            newFaces.add(new Facet(ic, ica, ibc));
            newFaces.add(new Facet(iab, ibc, ica));
        }

        vertexArray.clear();
        vertexArray.addAll(newVerts);

        faces.clear();
        faces.addAll(newFaces);
    }

    public ModelData buildModelData() {
        build();
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        for (Vertex vertex : vertexArray) {
            vertices.add(vertex.position.x);
            vertices.add(vertex.position.y);
            vertices.add(vertex.position.z);
            vertices.add(vertex.uv.x);
            vertices.add(vertex.uv.y);
        }

        for (Facet face : faces) {
            indices.add(face.a);
            indices.add(face.b);
            indices.add(face.c);
        }

        ModelNode node = new ModelNode();
        node.id = "node";
        node.meshId = "mesh";
        node.scale = new Vector3(1, 1, 1);
        node.translation = new Vector3();
        node.rotation = new Quaternion();
        ModelNodePart pm = new ModelNodePart();
        pm.meshPartId = "part";
        pm.materialId = "mat0";
        node.parts = new ModelNodePart[]{pm};
        ModelMeshPart part = new ModelMeshPart();
        part.id = "part";
        part.indices = indices.toArray();
        part.primitiveType = GL20.GL_TRIANGLES;
        ModelMesh mesh = new ModelMesh();
        mesh.id = "mesh";
        mesh.attributes = new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0)};
        mesh.vertices = vertices.toArray();
        mesh.parts = new ModelMeshPart[]{part};
        final ModelData data = new ModelData();
        data.nodes.add(node);
        data.meshes.add(mesh);
        ModelMaterial mm = new ModelMaterial();
        mm.id = "mat0";
        mm.diffuse = new Color(Color.WHITE);
        data.materials.add(mm);
        indices.clear();
        vertices.clear();
        return data;
    }

    public void setSubdivisions(int subdivisions) {
        this.subdivisions = subdivisions;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
