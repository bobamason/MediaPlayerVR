package net.masonapps.mediaplayervr.loaders;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;

import net.masonapps.mediaplayervr.utils.ModelGenerator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bob on 4/12/2017.
 */

public class VideoSphereLoader extends ModelLoader<ModelLoader.ModelParameters> {

    public VideoSphereLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public ModelData loadModelData(FileHandle fileHandle, ModelParameters parameters) {
        return null;
    }

    @Override
    public Model loadModel(FileHandle fileHandle) {
        try {
            final JSONObject json = new JSONObject(fileHandle.readString());
            final ModelBuilder modelBuilder = new ModelBuilder();
            float r = (float) json.getDouble("radius");
            final int divU = json.getInt("divisions_u");
            final int divV = json.getInt("divisions_v");
            return ModelGenerator.createSphere(modelBuilder, r, divU, divV);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Model loadModel(FileHandle fileHandle, ModelParameters parameters) {
        return loadModel(fileHandle);
    }

    @Override
    public Model loadModel(FileHandle fileHandle, TextureProvider textureProvider) {
        return loadModel(fileHandle);
    }

    @Override
    public Model loadModel(FileHandle fileHandle, TextureProvider textureProvider, ModelParameters parameters) {
        return loadModel(fileHandle);
    }
}
