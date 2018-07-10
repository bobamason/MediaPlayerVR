package org.masonapps.libgdxgooglevr.gfx;

import android.util.Log;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob on 1/4/2018.
 */

public class Transformable {
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Matrix4 transform = new Matrix4();
    protected final Matrix4 inverseTransform = new Matrix4();
    protected final Vector3 scale = new Vector3(1f, 1f, 1f);
    protected boolean updated = false;

    public Matrix4 getTransform(Matrix4 out) {
        validate();
        return out.set(transform);
    }

    public Matrix4 getTransform() {
        validate();
        return transform;
    }

    public Transformable setTransform(Matrix4 transform) {
        this.transform.set(transform);
        this.transform.getTranslation(position);
        this.transform.getRotation(rotation);
        this.transform.getScale(scale);
        try {
            inverseTransform.set(this.transform).inv();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updated = true;
        return this;
    }

    public void validate() {
        if (!updated)
            recalculateTransform();
    }

    public Transformable setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
        return this;
    }

    public Transformable scaleX(float x) {
        scale.x *= x;
        invalidate();
        return this;
    }

    public Transformable scaleY(float y) {
        scale.y *= y;
        invalidate();
        return this;
    }

    public Transformable scaleZ(float z) {
        scale.z *= z;
        invalidate();
        return this;
    }

    public Transformable scale(float s) {
        scale.scl(s, s, s);
        invalidate();
        return this;
    }

    public Transformable scale(float x, float y, float z) {
        scale.scl(x, y, z);
        invalidate();
        return this;
    }

    public float getScaleX() {
        return this.scale.x;
    }

    public Transformable setScaleX(float x) {
        scale.x = x;
        invalidate();
        return this;
    }

    public float getScaleY() {
        return this.scale.y;
    }

    public Transformable setScaleY(float y) {
        scale.y = y;
        invalidate();
        return this;
    }

    public float getScaleZ() {
        return this.scale.z;
    }

    public Transformable setScaleZ(float z) {
        scale.z = z;
        invalidate();
        return this;
    }

    public Transformable setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
        return this;
    }

    public Transformable setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
        return this;
    }

    public Transformable setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
        return this;
    }

    public Transformable rotateX(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public Transformable rotateY(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public Transformable rotateZ(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public Transformable setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
        return this;
    }

    public Transformable setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(false, tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
        return this;
    }

    public Transformable lookAt(Vector3 target, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(target).sub(this.position).nor();
        setRotation(dir, up);
        Pools.free(dir);
        return this;
    }

    public Quaternion getRotation() {
        validate();
        return rotation;
    }

    public Transformable setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
        return this;
    }

    public Transformable translateX(float units) {
        this.position.x += units;
        invalidate();
        return this;
    }

    public float getX() {
        return this.position.x;
    }

    public Transformable setX(float x) {
        this.position.x = x;
        invalidate();
        return this;
    }

    public Transformable translateY(float units) {
        this.position.y += units;
        invalidate();
        return this;
    }

    public float getY() {
        return this.position.y;
    }

    public Transformable setY(float y) {
        this.position.y = y;
        invalidate();
        return this;
    }

    public Transformable translateZ(float units) {
        this.position.z += units;
        invalidate();
        return this;
    }

    public float getZ() {
        return this.position.z;
    }

    public Transformable setZ(float z) {
        this.position.z = z;
        invalidate();
        return this;
    }

    public Transformable translate(float x, float y, float z) {
        this.position.add(x, y, z);
        invalidate();
        return this;
    }

    public Transformable translate(Vector3 translate) {
        this.position.add(translate);
        invalidate();
        return this;
    }

    public Transformable setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        invalidate();
        return this;
    }

    public Vector3 getPosition() {
        validate();
        return position;
    }

    public Transformable setPosition(Vector3 pos) {
        this.position.set(pos);
        invalidate();
        return this;
    }

    public void invalidate() {
        updated = false;
    }

    public void recalculateTransform() {
        transform.set(position, rotation, scale);
        try {
//            inverseTransform.set(-position.x, -position.y, -position.z, -rotation.x, -rotation.y, -rotation.z, rotation.w, 1f / scale.x, 1f / scale.y, 1f / scale.z);
            inverseTransform.set(transform).inv();
        } catch (Exception e) {
            inverseTransform.idt();
            Log.e(Entity.class.getName(), e.getLocalizedMessage());
        }
        updated = true;
    }

    public Matrix4 getInverseTransform(Matrix4 out) {
        return out.set(inverseTransform);
    }

    public Matrix4 getInverseTransform() {
        return inverseTransform;
    }

    public boolean isUpdated() {
        return updated;
    }

    public Vector3 getScale() {
        validate();
        return scale;
    }

    public Transformable setScale(float scale) {
        this.scale.set(scale, scale, scale);
        invalidate();
        return this;
    }
}
