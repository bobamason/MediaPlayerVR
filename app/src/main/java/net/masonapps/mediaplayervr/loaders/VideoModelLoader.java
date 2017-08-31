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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

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
            if (shape.equals("sphere")) {
                return createSurface(divU, divV);
            }
            else if (shape.equals("cylinder"))
                return createCylinder(r, divU, divV);
            else
                return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ModelData createSurface(int divisionsU, int divisionsV) {

        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        for (int j = 0; j <= divisionsV; ++j) {
            for (int i = 0; i <= divisionsU; ++i) {
                final float u = (float) i / divisionsU;
                final float v = (float) j / divisionsV;

                float azimuth = -u * MathUtils.PI * 2.0f - MathUtils.PI * 0.5f;
                float elevation = v * MathUtils.PI;
                vertices.add((float) (Math.cos(azimuth) * Math.sin(elevation)));
                vertices.add((float) Math.cos(elevation));
                vertices.add((float) (-Math.sin(azimuth) * Math.sin(elevation)));
                vertices.add(u);
                vertices.add(v);

                if (i < divisionsU && j < divisionsV) {
                    int a = (divisionsU + 1) * j + i;
                    int b = (divisionsU + 1) * j + i + 1;
                    int c = (divisionsU + 1) * (j + 1) + i;
                    int d = (divisionsU + 1) * (j + 1) + i + 1;
                    indices.add(a);
                    indices.add(c);
                    indices.add(d);
                    indices.add(a);
                    indices.add(d);
                    indices.add(b);
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
        vertices.clear();
        indices.clear();
        return data;
    }
    
    private ModelData createSphere(float radius, int divisionsU, int divisionsV) {

        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final ShortArray tmpIndices = new ShortArray();
        final int s = divisionsU + 3;
        tmpIndices.clear();
        tmpIndices.ensureCapacity(divisionsU * 2);
        tmpIndices.size = s;
        int tempOffset = 0;

        for (int j = 0; j <= divisionsV; ++j) {
            float angleV = (float) (Math.PI * j / divisionsV);
            float y = radius * (float) Math.cos(angleV);
            float ringRadius = radius * (float) Math.sin(angleV);

            for (int i = 0; i <= divisionsU; ++i) {
                float angleU = (float) (2.0f * Math.PI * i / divisionsU);
                float x = ringRadius * (float) Math.cos(angleU);
                float z = -ringRadius * (float) Math.sin(angleU);

                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
                vertices.add((float) ((i + 3 * divisionsU / 4) % divisionsU) / divisionsU);
                vertices.add((float) j / divisionsV);

                final int o = tempOffset + s;
                if (i > 0 && j > 0) {
                    tempOffset = (tempOffset + 1) % tmpIndices.size;
                    int a = (divisionsU + 1) * j + i;
                    int b = (divisionsU + 1) * j + i - 1;
                    int c = (divisionsU + 1) * (j - 1) + i - 1;
                    int d = (divisionsU + 1) * (j - 1) + i;
//                    int a = tmpIndices.get(tempOffset);
//                    int b = tmpIndices.get((o - 1) % s);
//                    int c = tmpIndices.get((o - (divisionsU + 2)) % s);
//                    int d = tmpIndices.get((o - (divisionsU + 1)) % s);

//                    if (j == divisionsV) {
//                        indices.add(a);
//                        indices.add(c);
//                        indices.add(d);
//                    } else if (j == 1) {
//                        indices.add(a);
//                        indices.add(b);
//                        indices.add(c);
//                    } else {
                    indices.add(a);
                    indices.add(b);
                    indices.add(c);

//                    indices.add(c);
//                    indices.add(d);
//                    indices.add(a);

                    indices.add(a);
                    indices.add(c);
                    indices.add(d);
//                    }
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
        vertices.clear();
        indices.clear();
        return data;
    }

    private ModelData createCylinder(float radius, int divisionsU, int divisionsV) {
        return null;
    }
}
