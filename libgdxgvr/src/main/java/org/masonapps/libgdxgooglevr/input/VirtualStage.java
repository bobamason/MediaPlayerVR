package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


/**
 * Created by Bob on 1/6/2017.
 */

public class VirtualStage extends Stage implements VrInputProcessor {
    private static final Vector3 dir = new Vector3();
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    private static final Vector2 tmpV2 = new Vector2();
    private static final Matrix4 tmpM = new Matrix4();
    private final Vector3 xaxis = new Vector3();
    private final Vector3 yaxis = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Quaternion rotator = new Quaternion();
    private final Matrix4 transform = new Matrix4();
    private Plane plane = new Plane();
    private boolean visible = true;
    private Rectangle bounds = new Rectangle();
    private float pixelSizeWorld = 0.0025f;
    private int mouseScreenX;
    private int mouseScreenY;
    private Actor mouseOverActor = null;
    private boolean isCursorOver = false;
    private Vector2 hitPoint2DPixels = new Vector2();
    private Vector3 hitPoint3D = new Vector3();
    private boolean touchable = true;
    private Vector3 position = new Vector3();
    private float radius;
    private boolean updated = false;

    public VirtualStage(Batch batch, int virtualPixelWidth, int virtualPixelHeight) {
        super(new ScreenViewport(), batch);
        getViewport().update(virtualPixelWidth, virtualPixelHeight, false);
        bounds.set(0, 0, virtualPixelWidth, virtualPixelHeight);
    }

    public VirtualStage(Batch batch, int virtualPixelWidth, int virtualPixelHeight, float pixelSizeWorld) {
        this(batch, virtualPixelWidth, virtualPixelHeight);
        this.pixelSizeWorld = pixelSizeWorld;
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
        tmpM.set(position, rotation);
        plane.set(position, tmp.set(Vector3.Z).mul(rotation).nor());
        bounds.set(0, 0, getViewport().getCamera().viewportWidth * pixelSizeWorld, getViewport().getCamera().viewportHeight * pixelSizeWorld);
        radius = (float) Math.sqrt(bounds.width * bounds.width + bounds.height * bounds.height);
        transform.idt().translate(-bounds.getWidth() * 0.5f, -bounds.getHeight() * 0.5f, 0).mul(tmpM).scale(pixelSizeWorld, pixelSizeWorld, 1f);
        updated = true;
    }

    public void draw(Camera camera) {
        if (!visible) return;
        if (!updated) recalculateTransform();
        Batch batch = this.getBatch();
        getRoot().setTransform(false);
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(transform);
        getRoot().draw(batch, 1);
        batch.end();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        mouseOverActor = fireEnterAndExit(mouseOverActor, mouseScreenX, mouseScreenY, -1);
    }

    @Override
    public void draw() {
        throw new UnsupportedOperationException("method not supported in " + VirtualStage.class.getSimpleName());
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!visible | !touchable) return false;
        if (!updated) recalculateTransform();
        if (!Intersector.intersectRaySphere(ray, position, radius, null))
            return false;
        if (Intersector.intersectRayPlane(ray, plane, hitPoint3D)) {
            tmp2.set(hitPoint3D).sub(transform.getTranslation(tmp));
            xaxis.set(Vector3.Y).crs(plane.normal).nor();
            yaxis.set(plane.normal).crs(xaxis).nor();
            tmpV2.set(xaxis.dot(tmp2), yaxis.dot(tmp2));
            if (bounds.contains(tmpV2)) {
                hitPoint2DPixels.set(tmpV2).scl(1f / pixelSizeWorld);
                isCursorOver = true;
                return true;
            }
        }
        isCursorOver = false;
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        tmpV2.set(screenX, screenY);

        InputEvent event = Pools.obtain(InputEvent.class);
        event.setType(InputEvent.Type.touchDown);
        event.setStage(this);
        event.setStageX(tmpV2.x);
        event.setStageY(tmpV2.y);
        event.setPointer(pointer);
        event.setButton(button);

        Actor target = hit(tmpV2.x, tmpV2.y, true);
        if (target == null) {
            if (getRoot().getTouchable() == Touchable.enabled) getRoot().fire(event);
        } else {
            target.fire(event);
        }

        boolean handled = event.isHandled();
        Pools.free(event);
        return handled;
    }

    @Override
    public Vector2 screenToStageCoordinates(Vector2 screenCoords) {
        return screenCoords;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseScreenX = screenX;
        mouseScreenY = screenY;
        tmpV2.set(screenX, screenY);

        InputEvent event = Pools.obtain(InputEvent.class);
        event.setStage(this);
        event.setType(InputEvent.Type.mouseMoved);
        event.setStageX(tmpV2.x);
        event.setStageY(tmpV2.y);

        Actor target = hit(tmpV2.x, tmpV2.y, true);
        if (target == null) target = getRoot();

        target.fire(event);
        boolean handled = event.isHandled();
        Pools.free(event);
        return handled;
    }

    private Actor fireEnterAndExit(Actor overLast, int screenX, int screenY, int pointer) {
        // Find the actor under the point.
        tmpV2.set(screenX, screenY);
        Actor over = hit(tmpV2.x, tmpV2.y, true);
        if (over == overLast) return overLast;

        // Exit overLast.
        if (overLast != null) {
            InputEvent event = Pools.obtain(InputEvent.class);
            event.setStage(this);
            event.setStageX(tmpV2.x);
            event.setStageY(tmpV2.y);
            event.setPointer(pointer);
            event.setType(InputEvent.Type.exit);
            event.setRelatedActor(over);
            overLast.fire(event);
            Pools.free(event);
        }
        // Enter over.
        if (over != null) {
            InputEvent event = Pools.obtain(InputEvent.class);
            event.setStage(this);
            event.setStageX(tmpV2.x);
            event.setStageY(tmpV2.y);
            event.setPointer(pointer);
            event.setType(InputEvent.Type.enter);
            event.setRelatedActor(overLast);
            over.fire(event);
            Pools.free(event);
        }
        return over;
    }

    @Override
    public void calculateScissors(Rectangle localRect, Rectangle scissorRect) {
        super.calculateScissors(localRect, scissorRect);
        scissorRect.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public boolean isTouchable() {
        return touchable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    @Override
    public Vector2 getHitPoint2D() {
        return hitPoint2DPixels;
    }

    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    public Plane getPlane() {
        return plane;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    public float getWidthWorld() {
        if (!updated) recalculateTransform();
        return getWidth() * pixelSizeWorld;
    }

    public float getHeightWorld() {
        if (!updated) recalculateTransform();
        return getHeight() * pixelSizeWorld;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPixelSizeWorld(float pixelSizeWorld) {
        this.pixelSizeWorld = pixelSizeWorld;
        updated = false;
    }
}
