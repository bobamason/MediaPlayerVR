package org.masonapps.libgdxgooglevr.gfx;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.vr.VrCamera;

/**
 * Created by Bob on 12/22/2016.
 */
public abstract class VrScreen implements Disposable {

    protected VrGame game;

    public VrScreen(VrGame game) {
        this.game = game;
    }

    public abstract void resume();

    public void onDrawFrame(HeadTransform headTransform, Eye eye, Eye eye1) {
        onNewFrame(headTransform);
        onDrawEye(eye);
        onDrawEye(eye1);
    }

    public abstract void pause();

    public abstract void show();

    public abstract void hide();

    public void update() {
    }

    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
    }

    public void onCardboardTrigger() {
    }

    public void onNewFrame(HeadTransform headTransform) {
        update();
    }

    public void onDrawEye(Eye eye) {
        final Viewport viewport = eye.getViewport();
        GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        getVrCamera().onDrawEye(eye);
        render(getVrCamera(), eye.getType());
        renderCursor(getVrCamera());
    }

    public void render(Camera camera, int whichEye) {

    }

    protected void renderCursor(Camera camera) {
    }

    public Vector3 getForwardVector() {
        return game.getForwardVector();
    }

    public Vector3 getUpVector() {
        return game.getUpVector();
    }

    public Vector3 getRightVector() {
        return game.getRightVector();
    }

    public Vector3 getHeadTranslation() {
        return game.getHeadTranslation();
    }

    public Quaternion getHeadQuaternion() {
        return game.getHeadQuaternion();
    }

    public Matrix4 getHeadMatrix() {
        return game.getHeadMatrix();
    }

    public VrCamera getVrCamera() {
        return game.getVrCamera();
    }

    protected void doneLoading(AssetManager assets) {}
}
