package org.masonapps.libgdxgooglevr.gfx;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by Bob on 8/10/2015.
 */
public class Entity implements Disposable {
    private static final BoundingBox bounds = new BoundingBox();
    private static final Vector3 position = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public final Vector3 center = new Vector3();
    public final float radius;
    public Matrix4 transform;
    public ModelInstance modelInstance;
    protected boolean renderingEnabled = true;
    protected boolean lightingEnabled = true;
    @Nullable
    protected BaseShader shader = null;

    public Entity(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        this.transform = modelInstance.transform;
        modelInstance.calculateBoundingBox(bounds);
        bounds.getDimensions(dimensions);
        bounds.getCenter(center);
        radius = dimensions.len();
//        radius = dimensions.len() / 2f;
    }

    public boolean isVisible(Camera camera) {
        if (!renderingEnabled) return false;
        transform.getTranslation(position);
        return camera.frustum.sphereInFrustum(position.add(center), radius);
    }

    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    public void setRenderingEnabled(boolean renderingEnabled) {
        this.renderingEnabled = renderingEnabled;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
    }

    public void setShader(@Nullable BaseShader shader) {
        this.shader = shader;
    }

    @Override
    public void dispose() {
        if (shader != null)
            shader.dispose();
        shader = null;
    }

    public static class EntityConstructor extends World.Constructor<Entity> {

        public EntityConstructor(Model model) {
            super(model);
        }

        @Override
        public Entity construct(float x, float y, float z) {
            return new Entity(new ModelInstance(model, x, y, z));
        }

        @Override
        public Entity construct(Matrix4 transform) {
            return new Entity(new ModelInstance(model, transform));
        }

        @Override
        public void dispose() {
            try {
                model.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
