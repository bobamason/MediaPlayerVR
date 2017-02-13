package org.masonapps.libgdxgooglevr.input;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
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
    private static final Vector3 tmpV3 = new Vector3();
    private static final Vector3 tmpV3_2 = new Vector3();
    private static final Vector2 tmpV2 = new Vector2();
    private static final Matrix4 tmpM = new Matrix4();
    private final Matrix4 transform = new Matrix4();
    private final Vector3 xaxis = new Vector3();
    private final Vector3 yaxis = new Vector3();
    private Vector2 hitPoint2DPixels = new Vector2();
    private Vector3 hitPoint3D = new Vector3();
    private Plane plane = new Plane();
    private boolean visible = true;
    private Rectangle bounds = new Rectangle();
    private Vector2 worldToPixelScale = new Vector2(1, 1);
    private int mouseScreenX;
    private int mouseScreenY;
    private Actor mouseOverActor = null;
    private volatile boolean isCursorOver = false;

    public VirtualStage(Batch batch, float width, float height, int virtualPixelWidth, int virtualPixelHeight) {
        super(new ScreenViewport(), batch);
        getViewport().update(virtualPixelWidth, virtualPixelHeight, true);
        bounds.set(0, 0, width, height);
        worldToPixelScale.set(virtualPixelWidth / width, virtualPixelHeight / height);
    }

    public void set3DTransform(Vector3 position, Vector3 up, Vector3 lookAt) {
        plane.set(position, tmpV3.set(lookAt).sub(position).nor());
        xaxis.set(up).crs(plane.normal).nor();
        yaxis.set(plane.normal).crs(xaxis).nor();
        tmpM.set(xaxis, yaxis, plane.normal, Vector3.Zero).tra();
        transform.idt().translate(-bounds.getWidth() * 0.5f, -bounds.getHeight() * 0.5f, 0).mul(tmpM).translate(position).scale(1f / worldToPixelScale.x, 1f / worldToPixelScale.y, 1f);
    }

    public void set3DTransform(Vector3 position, Vector3 lookAt) {
        set3DTransform(position, Vector3.Y, lookAt);
    }

    public void draw(Camera camera) {
        if (!visible) return;
        Batch batch = this.getBatch();
        getRoot().setTransform(false);
        batch.setProjectionMatrix(camera.combined);
//        batch.setProjectionMatrix(tmpM.set(camera.combined).mul(getViewport().getCamera().view));
        batch.begin();
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
        if (!visible) return false;
        if (Intersector.intersectRayPlane(ray, plane, hitPoint3D)) {
            tmpV3_2.set(hitPoint3D).sub(transform.getTranslation(tmpV3));
            xaxis.set(Vector3.Y).crs(plane.normal).nor();
            yaxis.set(plane.normal).crs(xaxis).nor();
            tmpV2.set(xaxis.dot(tmpV3_2), yaxis.dot(tmpV3_2));
            bounds.set(0, 0, getViewport().getCamera().viewportWidth / worldToPixelScale.x, getViewport().getCamera().viewportHeight / worldToPixelScale.y);
            if (bounds.contains(tmpV2)) {
                hitPoint2DPixels.set(tmpV2).scl(worldToPixelScale);
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
    public Vector2 getHitPoint2D() {
        return hitPoint2DPixels;
    }

    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    @Override
    public Plane getPlane() {
        return plane;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
