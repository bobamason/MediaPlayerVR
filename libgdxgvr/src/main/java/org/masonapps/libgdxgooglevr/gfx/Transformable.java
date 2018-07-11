package org.masonapps.libgdxgooglevr.gfx;

import android.util.Log;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

/**
 * 
 * Created by Bob on 1/4/2018.
 */

public class Transformable {
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Vector3 scale = new Vector3(1f, 1f, 1f);
    protected final Matrix4 transform = new Matrix4();
    protected final Matrix4 inverseTransform = new Matrix4();
    protected boolean updated = false;

    /**
     * if the transformation matrix and inverse are not updated, they will be recalculated to match the current position, rotation, and scale
     */
    public void validate() {
        if (!updated)
            recalculateTransform();
    }

    /**
     * scale x dimension by sx
     *
     * @param sx x scale factor
     * @return this Transformable for chaining
     */
    public Transformable scaleX(float sx) {
        scale.x *= sx;
        invalidate();
        return this;
    }

    /**
     * scale y dimension by sy
     *
     * @param sy y scale factor
     * @return this Transformable for chaining
     */
    public Transformable scaleY(float sy) {
        scale.y *= sy;
        invalidate();
        return this;
    }

    /**
     * scale z dimension by sz
     *
     * @param sz z scale factor
     * @return this Transformable for chaining
     */
    public Transformable scaleZ(float sz) {
        scale.z *= sz;
        invalidate();
        return this;
    }

    /**
     * scale by s on x, y, and z
     * @param s the scale factor
     * @return this Transformable for chaining
     */
    public Transformable scale(float s) {
        scale.scl(s, s, s);
        invalidate();
        return this;
    }

    /**
     * scale x, y, z by sx, sy, sz
     *
     * @param sx x scale factor
     * @param sy y scale factor
     * @param sz z scale factor
     * @return this Transformable for chaining
     */
    public Transformable scale(float sx, float sy, float sz) {
        scale.scl(sx, sy, sz);
        invalidate();
        return this;
    }

    /**
     * set the scale x,y,z
     *
     * @param x new scale x
     * @param y new scale y
     * @param z new scale z
     * @return this Transformable for chaining
     */
    public Transformable setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
        return this;
    }

    /**
     * get scale x
     * @return scale x
     */
    public float getScaleX() {
        return this.scale.x;
    }

    /**
     * set scale x
     * @param x new scale x
     * @return this Transformable for chaining
     */
    public Transformable setScaleX(float x) {
        scale.x = x;
        invalidate();
        return this;
    }

    /**
     * get scale y
     * @return scale y
     */
    public float getScaleY() {
        return this.scale.y;
    }

    /**
     * set scale y
     * @param y new scale y
     * @return this Transformable for chaining
     */
    public Transformable setScaleY(float y) {
        scale.y = y;
        invalidate();
        return this;
    }

    /**
     * get scale z
     * @return scale z
     */
    public float getScaleZ() {
        return this.scale.z;
    }

    /**
     * set scale z
     * @param z new scale z
     * @return this Transformable for chaining
     */
    public Transformable setScaleZ(float z) {
        scale.z = z;
        invalidate();
        return this;
    }

    /**
     * get current scale
     *
     * @return scale {@link Vector3} object, it is mutable so if modified call invalidate()
     */
    public Vector3 getScale() {
        validate();
        return scale;
    }

    /**
     * sets x, y, z dimensions to same scale
     *
     * @param scale scale value
     * @return this Transformable for chaining
     */
    public Transformable setScale(float scale) {
        this.scale.set(scale, scale, scale);
        invalidate();
        return this;
    }

    /**
     * set the rotation to angle around the x axis
     * @param angle angle in degrees
     * @return this Transformable for chaining
     */
    public Transformable setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
        return this;
    }

    /**
     * set the rotation to angle around the y axis
     * @param angle angle in degrees
     * @return this Transformable for chaining
     */
    public Transformable setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
        return this;
    }

    /**
     * set rotation to angle around z axis
     * @param angle angle in degrees
     * @return this Transformable for chaining
     */
    public Transformable setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
        return this;
    }

    /**
     * rotate around the x axis by angle
     * @param angle angle in degrees
     * @return this Transformable for chaining
     */
    public Transformable rotateX(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    /**
     * rotate around the y axis by angle
     * @param angle angle in degrees
     * @return this Transformable for chaining
     */
    public Transformable rotateY(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    /**
     * rotate around the z axis by angle
     * @param angle angle in degrees
     * @return this Transformable for chaining
     */
    public Transformable rotateZ(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    /**
     * set the rotation from euler angles in degrees
     * @param yaw angle around y axis
     * @param pitch angle around x axis
     * @param roll angle around z axis
     * @return this Transformable for chaining
     */
    public Transformable setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
        return this;
    }

    /**
     * set rotation by forward direction and up
     * @param dir forward vector
     * @param up up vector
     * @return this Transformable for chaining
     */
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

    /**
     * set rotation to look at target vector
     * @param target vector to look at
     * @param up up vector
     * @return this Transformable for chaining
     */
    public Transformable lookAt(Vector3 target, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(target).sub(this.position).nor();
        setRotation(dir, up);
        Pools.free(dir);
        return this;
    }

    /**
     * get current rotation
     * @return rotation {@link Quaternion} object, it is mutable so if modified call invalidate()
     */
    public Quaternion getRotation() {
        validate();
        return rotation;
    }

    /**
     * set rotation to quaternion
     * @param q input {@link Quaternion}
     * @return this Transformable for chaining
     */
    public Transformable setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
        return this;
    }

    /**
     * translate along x axis
     *
     * @param tx amount to translate by
     * @return this Transformable for chaining
     */
    public Transformable translateX(float tx) {
        this.position.x += tx;
        invalidate();
        return this;
    }

    /**
     * get position x
     * @return x
     */
    public float getX() {
        return this.position.x;
    }

    /**
     * set position x
     * @param x value
     * @return this Transformable for chaining
     */
    public Transformable setX(float x) {
        this.position.x = x;
        invalidate();
        return this;
    }

    /**
     * translate along y axis
     *
     * @param ty amount to translate by
     * @return this Transformable for chaining
     */
    public Transformable translateY(float ty) {
        this.position.y += ty;
        invalidate();
        return this;
    }

    /**
     * get position y
     * @return y
     */
    public float getY() {
        return this.position.y;
    }

    /**
     * set position y
     * @param y value
     * @return this Transformable for chaining
     */
    public Transformable setY(float y) {
        this.position.y = y;
        invalidate();
        return this;
    }

    /**
     * translate along z axis
     *
     * @param tz amount to translate by
     * @return this Transformable for chaining
     */
    public Transformable translateZ(float tz) {
        this.position.z += tz;
        invalidate();
        return this;
    }

    /**
     * get position z
     * @return z
     */
    public float getZ() {
        return this.position.z;
    }

    /**
     * set position z
     * @param z value
     * @return this Transformable for chaining
     */
    public Transformable setZ(float z) {
        this.position.z = z;
        invalidate();
        return this;
    }

    /**
     * translate by tx, ty, tz
     *
     * @param tx x value
     * @param ty y value
     * @param tz z value
     * @return this Transformable for chaining
     */
    public Transformable translate(float tx, float ty, float tz) {
        this.position.add(tx, ty, tz);
        invalidate();
        return this;
    }

    /**
     * translate by vector
     * @param translate {@link Vector3} to translate by
     * @return this Transformable for chaining
     */
    public Transformable translate(Vector3 translate) {
        this.position.add(translate);
        invalidate();
        return this;
    }

    /**
     * set position x, y, z
     * @param x x value
     * @param y y value
     * @param z z value
     * @return this Transformable for chaining
     */
    public Transformable setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        invalidate();
        return this;
    }

    /**
     * get current position
     * @return position {@link Vector3} object, it is mutable so if modified call invalidate()
     */
    public Vector3 getPosition() {
        validate();
        return position;
    }

    /**
     * set the position
     * @param pos new position {@link Vector3}
     * @return this Transformable for chaining
     */
    public Transformable setPosition(Vector3 pos) {
        this.position.set(pos);
        invalidate();
        return this;
    }

    /**
     * Does NOT need to be called if using setter methods. It should only be called after changes to position, rotation, and scale objects.
     */
    public void invalidate() {
        updated = false;
    }

    /**
     * calculates the transformation matrix and it's inverse. In most cases use setter methods, or use invalidate() and validate().
     */
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

    /**
     * save the transformation matrix to the matrix passed as the parameter
     *
     * @param out {@link Matrix4} to be set to the updated transformation matrix
     * @return the out {@link Matrix4} passed as the parameter
     */
    public Matrix4 getTransform(Matrix4 out) {
        return out.set(getTransform());
    }

    /**
     * get the mutable transformation matrix object
     *
     * @return the transformation {@link Matrix4} object, avoid using any methods that change the values
     */
    public Matrix4 getTransform() {
        validate();
        return transform;
    }

    /**
     * set the transformation matrix. The position, rotation, and scale will also be changed
     *
     * @param transform {@link Matrix4} to set the transform to
     * @return this Transformable for chaining
     */
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

    /**
     * save the inverse transformation matrix to the matrix passed as the parameter
     * @param out {@link Matrix4} to be set to the updated transformation matrix
     * @return the out {@link Matrix4} passed as the parameter
     */
    public Matrix4 getInverseTransform(Matrix4 out) {
        return out.set(getInverseTransform());
    }

    /**
     * get the mutable inverse transformation matrix object
     * @return the inverse transformation {@link Matrix4} object, avoid using any methods that change the values
     */
    public Matrix4 getInverseTransform() {
        validate();
        return inverseTransform;
    }

    /**
     * can be useful in sub-classes, in most cases it is not necessary to use this method use validate() instead
     * @return true if the transform and inverse transform have been updated
     */
    public boolean isUpdated() {
        return updated;
    }
}
