package net.masonapps.mediaplayervr.loaders;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bob on 4/12/2017.
 */

public class VideoModelLoader extends ModelLoader<ModelLoader.ModelParameters> {

    public VideoModelLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public ModelData loadModelData(FileHandle fileHandle, ModelParameters parameters) {
        try {
            final JSONObject json = new JSONObject(fileHandle.readString());
            final String shape = json.getString("shape");
            final float r = (float) json.getDouble("radius");
            final int divU = json.getInt("divisions_u");
            final int divV = json.getInt("divisions_v");
            if (shape.equals("sphere"))
                return createSphere(r, divU, divV);
            else if (shape.equals("cylinder"))
                return createCylinder(r, divU, divV);
            else
                return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ModelData createSphere(float radius, int divisionsU, int divisionsV) {

        final int numVertices = (divisionsU + 1) * (divisionsV + 1);
        final int floatsPerVertex = 5;
        final int numIndices = 2 * divisionsU * (divisionsV - 1) * floatsPerVertex;

        final float[] vertices = new float[numVertices * floatsPerVertex];
        final short[] indices = new short[numIndices];

        int vertIndex = 0, index = 0;

        for (int j = 0; j <= divisionsV; ++j) {
            float horAngle = (float) (Math.PI * j / divisionsV);
            float z = radius * (float) Math.cos(horAngle);
            float ringRadius = radius * (float) Math.sin(horAngle);

            for (int i = 0; i <= divisionsU; ++i) {
                float verAngle = (float) (2.0f * Math.PI * i / divisionsU);
                float x = ringRadius * (float) Math.cos(verAngle);
                float y = ringRadius * (float) Math.sin(verAngle);

                vertices[vertIndex++] = x;
                vertices[vertIndex++] = z;
                vertices[vertIndex++] = y;
                vertices[vertIndex++] = (float) ((i + 3 * divisionsU / 4) % divisionsU) / divisionsU;
                vertices[vertIndex++] = (float) j / divisionsV;

                if (i > 0 && j > 0) {
                    int a = (divisionsU + 1) * j + i;
                    int b = (divisionsU + 1) * j + i - 1;
                    int c = (divisionsU + 1) * (j - 1) + i - 1;
                    int d = (divisionsU + 1) * (j - 1) + i;

                    if (j == divisionsV) {
                        indices[index++] = (short) a;
                        indices[index++] = (short) c;
                        indices[index++] = (short) d;
                    } else if (j == 1) {
                        indices[index++] = (short) a;
                        indices[index++] = (short) b;
                        indices[index++] = (short) c;
                    } else {
                        indices[index++] = (short) a;
                        indices[index++] = (short) b;
                        indices[index++] = (short) c;
                        indices[index++] = (short) a;
                        indices[index++] = (short) c;
                        indices[index++] = (short) d;
                    }
                }
            }
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
        part.indices = indices;
        part.primitiveType = GL20.GL_TRIANGLES;
        ModelMesh mesh = new ModelMesh();
        mesh.id = "mesh";
        mesh.attributes = new VertexAttribute[]{VertexAttribute.Position(), VertexAttribute.TexCoords(0)};
        mesh.vertices = vertices;
        mesh.parts = new ModelMeshPart[]{part};
        final ModelData data = new ModelData();
        data.nodes.add(node);
        data.meshes.add(mesh);
        ModelMaterial mm = new ModelMaterial();
        mm.id = "mat0";
        mm.diffuse = new Color(Color.WHITE);
        data.materials.add(mm);
        return data;
    }

    private ModelData createCylinder(float radius, int divisionsU, int divisionsV) {
        return null;
    }
}
