package org.masonapps.libgdxgooglevr.gfx;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob on 8/10/2015.
 */
public class Entity extends Transformable implements Disposable {
    private final Vector3 dimensions = new Vector3();
    private final Vector3 center = new Vector3();
    private final float radius;
    @Nullable
    public ModelInstance modelInstance;
    @Nullable
    protected BaseShader shader = null;
    private BoundingBox bounds = new BoundingBox();
    private boolean visible = true;
    private boolean lightingEnabled = true;

    public Entity(@Nullable ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        if (modelInstance != null) {
            setTransform(modelInstance.transform);
            bounds.inf();
            for (Node node : modelInstance.nodes) {
                node.extendBoundingBox(bounds, false);
            }
        }
        bounds.getDimensions(dimensions);
        bounds.getCenter(center);
        radius = dimensions.len() / 2f;
    }

    public Entity(@Nullable ModelInstance modelInstance, BoundingBox bounds) {
        this.modelInstance = modelInstance;
        if (modelInstance != null)
            setTransform(modelInstance.transform);
        this.bounds.set(bounds);
        this.bounds.getDimensions(dimensions);
        this.bounds.getCenter(center);
        radius = dimensions.len() / 2f;
    }

    public boolean isInCameraFrustum(Camera camera) {
        if (!visible || modelInstance == null) return false;
        if (!updated) recalculateTransform();
        final Vector3 pos = Pools.obtain(Vector3.class);
        final Vector3 s = Pools.obtain(Vector3.class);
        pos.set(center).mul(modelInstance.transform);
        modelInstance.transform.getScale(s);
        final boolean inFrustum = camera.frustum.sphereInFrustum(pos, radius * Math.max(s.x, Math.max(s.y, s.z)));
        Pools.free(pos);
        Pools.free(s);
        return inFrustum;
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        if (modelInstance != null)
            modelInstance.transform.set(transform);
    }

    @Override
    public Entity setTransform(Matrix4 transform) {
        super.setTransform(transform);
        if (modelInstance != null)
            modelInstance.transform.set(this.transform);
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
    }

    @Nullable
    public BaseShader getShader() {
        return shader;
    }

    public void setShader(@Nullable BaseShader shader) {
        this.shader = shader;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public float getRadius() {
        return radius;
    }

    public Vector3 getCenter() {
        return center;
    }

    public Vector3 getDimensions() {
        return dimensions;
    }

    @Override
    public void dispose() {
        if (shader != null)
            shader.dispose();
        shader = null;
    }

    public boolean intersectsRayBoundsFast(Ray ray) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBoundsFast = Intersector.intersectRayBoundsFast(tmpRay, bounds);

        Pools.free(tmpRay);
        return intersectRayBoundsFast;
    }

    public boolean intersectsRayBounds(Ray ray, @Nullable Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBounds = Intersector.intersectRayBounds(tmpRay, bounds, hitPoint);
        if (intersectRayBounds && hitPoint != null) hitPoint.mul(transform);

        Pools.free(tmpRay);
        return intersectRayBounds;
    }

    public boolean intersectsRaySphere(Ray ray, @Nullable Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class).set(ray);
        final Vector3 tmp = Pools.obtain(Vector3.class);
        tmpRay.direction.nor();
        tmp.set(position);
        final boolean intersectRaySphere = Intersector.intersectRaySphere(tmpRay, tmp, radius * Math.min(scale.x, Math.min(scale.y, scale.z)), hitPoint);
//        if (intersectRaySphere && hitPoint != null) hitPoint.mul(modelInstance.transform);

        Pools.free(tmpRay);
        Pools.free(tmp);
        return intersectRaySphere;
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
