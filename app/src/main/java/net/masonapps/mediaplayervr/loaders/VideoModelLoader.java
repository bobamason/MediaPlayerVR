package net.masonapps.mediaplayervr.loaders;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import net.masonapps.mediaplayervr.utils.ModelGenerator;

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
            final ModelBuilder modelBuilder = new ModelBuilder();
            final String shape = json.getString("shape");
            final float r = (float) json.getDouble("radius");
            final int divU = json.getInt("divisions_u");
            final int divV = json.getInt("divisions_v");
            if (shape.equals("sphere"))
                return createSphere(modelBuilder, r, divU, divV);
            else if (shape.equals("cylinder"))
                return createCylinder(modelBuilder, r, divU, divV);
            else
                return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Model loadModel(FileHandle fileHandle) {
        try {
            final JSONObject json = new JSONObject(fileHandle.readString());
            final ModelBuilder modelBuilder = new ModelBuilder();
            final String shape = json.getString("shape");
            final float r = (float) json.getDouble("radius");
            final int divU = json.getInt("divisions_u");
            final int divV = json.getInt("divisions_v");
            if (shape.equals("sphere"))
                return ModelGenerator.createSphere(modelBuilder, r, divU, divV);
            else if (shape.equals("cylinder"))
                return ModelGenerator.createCylinder(modelBuilder, r, divU, divV);
            else
                return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ModelData createSphere(ModelBuilder modelBuilder, float radius, int divisionsU, int divisionsV) {
        Model model = ModelGenerator.createSphere(modelBuilder, radius, divisionsU, divisionsV);
        return createModelData(model, "sphere");
    }

    private ModelData createCylinder(ModelBuilder modelBuilder, float radius, int divisionsU, int divisionsV) {
        Model model = ModelGenerator.createCylinder(modelBuilder, radius, divisionsU, divisionsV);
        return createModelData(model, "cylinder");
    }

    private ModelData createModelData(Model model, String id) {
        final ModelData modelData = new ModelData();
        modelData.id = id;

        final Mesh mesh = model.meshes.get(0);
        final ModelMesh modelMesh = new ModelMesh();
        modelMesh.id = id;

        final VertexAttribute[] vertexAttributes = new VertexAttribute[mesh.getVertexAttributes().size()];
        for (int i = 0; i < vertexAttributes.length; i++) {
            vertexAttributes[i] = mesh.getVertexAttributes().get(i);
        }
        modelMesh.attributes = vertexAttributes;

        final ModelMeshPart modelMeshPart = new ModelMeshPart();
        modelMeshPart.id = id;
        modelMeshPart.indices = new short[mesh.getNumIndices()];
        mesh.getIndices(modelMeshPart.indices);
        modelMeshPart.primitiveType = GL20.GL_TRIANGLES;
        modelMesh.parts = new ModelMeshPart[]{modelMeshPart};

        modelMesh.vertices = new float[mesh.getNumVertices() * mesh.getVertexSize()];
        mesh.getVertices(modelMesh.vertices);

        modelData.addMesh(modelMesh);
        return modelData;
    }
}
