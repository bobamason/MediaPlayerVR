package org.masonapps.libgdxgooglevr.input;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

/**
 * Created by Bob on 3/15/2017.
 */

public class VrUiContainer implements VrInputProcessor {

    private static final Vector3 dir = new Vector3();
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private final ArrayList<VirtualStage> stages;
    private final Vector3 position = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Quaternion rotator = new Quaternion();
    private final Matrix4 transform = new Matrix4();
    private final Matrix4 invTransform = new Matrix4();
    private final Ray transformedRay = new Ray();
    private boolean isCursorOver = false;
    private Vector2 hitPoint2DPixels = new Vector2();
    private Vector3 hitPoint3D = new Vector3();
    private boolean updated = false;
    @Nullable
    private VrInputProcessor focusedStage;

    public VrUiContainer() {
        stages = new ArrayList<>();
    }

    public VrUiContainer(VirtualStage... stages) {
        this();
        for (VirtualStage stage : stages) {
            this.stages.add(stage);
        }
    }

    public void setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        updated = false;
    }

    public void setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        updated = false;
    }

    public void setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        updated = false;
    }

    public void rotateX(float angle) {
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        updated = false;
    }

    public void rotateY(float angle) {
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        updated = false;
    }

    public void rotateZ(float angle) {
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        updated = false;
    }

    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        updated = false;
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        updated = false;
    }

    public void lookAt(Vector3 position, Vector3 up) {
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion q) {
        rotation.set(q);
        updated = false;
    }

    public void translateX(float units) {
        this.position.x += units;
        updated = false;
    }

    public float getX() {
        return this.position.x;
    }

    public void setX(float x) {
        this.position.x = x;
        updated = false;
    }

    public void translateY(float units) {
        this.position.y += units;
        updated = false;
    }

    public float getY() {
        return this.position.y;
    }

    public void setY(float y) {
        this.position.y = y;
        updated = false;
    }

    public void translateZ(float units) {
        this.position.z += units;
        updated = false;
    }

    public float getZ() {
        return this.position.z;
    }

    public void setZ(float z) {
        this.position.z = z;
        updated = false;
    }

    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        updated = false;
    }

    public void translate(Vector3 trans) {
        this.position.add(trans);
        updated = false;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        updated = false;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 pos) {
        this.position.set(pos);
        updated = false;
    }

    public void recalculateTransform() {
        transform.idt().set(position, rotation);
        invTransform.set(transform).inv();
        updated = true;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!updated) recalculateTransform();
        transformedRay.origin.set(ray.origin).mul(invTransform);
        transformedRay.direction.set(ray.direction).mul(invTransform);
        for (VrInputProcessor inputProcessor : stages) {
            if (inputProcessor.performRayTest(transformedRay)) {
                focusedStage = inputProcessor;
                hitPoint2DPixels.set(inputProcessor.getHitPoint2D());
                hitPoint3D.set(inputProcessor.getHitPoint3D()).mul(transform);
                isCursorOver = true;
                return true;
            }
        }
        focusedStage = null;
        isCursorOver = false;
        return false;
    }

    public void act() {
        if (!updated) recalculateTransform();
        for (VirtualStage stage : stages) {
            stage.act();
        }
    }

    public void draw(Camera camera) {
        if (!updated) recalculateTransform();
        for (VirtualStage stage : stages) {
            stage.draw(camera, transform);
        }
    }

    @Override
    public Vector2 getHitPoint2D() {
        return hitPoint2DPixels;
    }

    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    public void addProcessor(VirtualStage stage) {
        stages.add(stage);
    }

    public void removeProcessor(VirtualStage stage) {
        stages.remove(stage);
    }

    public void clearProcessors() {
        stages.clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        return focusedStage != null && focusedStage.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return focusedStage != null && focusedStage.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return focusedStage != null && focusedStage.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return focusedStage != null && focusedStage.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return focusedStage != null && focusedStage.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return focusedStage != null && focusedStage.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return focusedStage != null && focusedStage.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return focusedStage != null && focusedStage.scrolled(amount);
    }
}
