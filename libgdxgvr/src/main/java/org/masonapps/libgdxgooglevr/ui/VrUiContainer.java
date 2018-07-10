package org.masonapps.libgdxgooglevr.ui;

import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;

import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob on 3/15/2017.
 */

public class VrUiContainer extends VrInputMultiplexer {
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Quaternion rotator = new Quaternion();
    protected final Matrix4 transform = new Matrix4();
    protected final Matrix4 globalTransform = new Matrix4();
    protected final Matrix4 invTransform = new Matrix4();
    protected final Ray transformedRay = new Ray();
    protected boolean updated = false;
    protected boolean transformable = false;
    protected boolean visible = true;

    public VrUiContainer() {
        super();
    }

    public VrUiContainer(VrInputProcessor... processors) {
        super(processors);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!visible) return false;
        if (!updated && transformable) recalculateTransform();
        if (transformable)
            transformedRay.set(ray).mul(invTransform);
        else
            transformedRay.set(ray);
        final boolean rayTest = super.performRayTest(transformedRay);
        if (rayTest && transformable)
            hitPoint3D.mul(transform);
        return rayTest;
    }

    public void act() {
        if (!visible) return;
        if (!updated) recalculateTransform();
        for (VrInputProcessor processor : processors) {
            if (processor instanceof VirtualStage)
                ((VirtualStage) processor).act();
            if (processor instanceof VrUiContainer)
                ((VrUiContainer) processor).act();
        }
    }

    public void draw(Camera camera) {
        draw(camera, null);
    }

    public void draw(Camera camera, @Nullable Matrix4 parentTransform) {
        if (!visible) return;
        if (!updated) recalculateTransform();
        globalTransform.set(transform);
        if (parentTransform != null)
            globalTransform.mulLeft(parentTransform);
        for (VrInputProcessor processor : processors) {
            if (processor instanceof VirtualStage)
                ((VirtualStage) processor).draw(camera, globalTransform);
            if (processor instanceof VrUiContainer)
                ((VrUiContainer) processor).draw(camera, globalTransform);
        }
    }

    public void setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
    }

    public void setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
    }

    public void setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
    }

    public void rotateX(float angle) {
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void rotateY(float angle) {
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void rotateZ(float angle) {
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
    }

    public void invalidate() {
        updated = false;
        transformable = true;
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
    }

    public void lookAt(Vector3 position, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
        Pools.free(dir);
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
    }

    public void translateX(float units) {
        this.position.x += units;
        invalidate();
    }

    public float getX() {
        return this.position.x;
    }

    public void setX(float x) {
        this.position.x = x;
        invalidate();
    }

    public void translateY(float units) {
        this.position.y += units;
        invalidate();
    }

    public float getY() {
        return this.position.y;
    }

    public void setY(float y) {
        this.position.y = y;
        invalidate();
    }

    public void translateZ(float units) {
        this.position.z += units;
        invalidate();
    }

    public float getZ() {
        return this.position.z;
    }

    public void setZ(float z) {
        this.position.z = z;
        invalidate();
    }

    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        invalidate();
    }

    public void translate(Vector3 trans) {
        this.position.add(trans);
        invalidate();
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        invalidate();
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 pos) {
        this.position.set(pos);
        invalidate();
    }

    public void recalculateTransform() {
        if (transformable) {
            transform.idt().set(position, rotation);
            try {
                invTransform.set(transform).inv();
            } catch (Exception e) {
                Log.e(VrUiContainer.class.getName(), e.getLocalizedMessage());
            }
        }
        updated = true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isTransformable() {
        return transformable;
    }

    public void setTransformable(boolean transformable) {
        this.transformable = transformable;
    }

    public void setAlpha(float alpha) {
        for (VrInputProcessor processor : processors) {
            if (processor instanceof VirtualStage)
                ((VirtualStage) processor).setAlpha(alpha);
            if (processor instanceof VrUiContainer)
                ((VrUiContainer) processor).setAlpha(alpha);
        }
    }
}
