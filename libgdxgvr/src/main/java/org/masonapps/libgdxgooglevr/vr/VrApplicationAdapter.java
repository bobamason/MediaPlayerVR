package org.masonapps.libgdxgooglevr.vr;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;

/**
 * Created by Bob on 12/15/2016.
 */

public abstract class VrApplicationAdapter implements ApplicationListener {
    protected VrCamera vrCamera;

    public VrApplicationAdapter() {
        vrCamera = new VrCamera();
    }

    @Override
    @CallSuper
    public void create() {
    }

    @CallSuper
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        onNewFrame(headTransform);
        onDrawEye(leftEye);
        onDrawEye(rightEye);
    }

    @CallSuper
    public void onNewFrame(HeadTransform headTransform) {
        update();
    }

    @CallSuper
    public void onDrawEye(Eye eye) {
        final Viewport viewport = eye.getViewport();
        GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        vrCamera.onDrawEye(eye);
        render(vrCamera, eye.getType());
    }

    public void update() {
    }

    public void render(Camera camera, int whichEye) {
    }

    @CallSuper
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
    }

    public void onCardboardTrigger() {
    }

    @Override
    @CallSuper
    public void render() {
        throw new UnsupportedOperationException("render() is not supported, call onNewFrame() and onDrawEye() instead");
    }

    @Override
    @CallSuper
    public void resize(int width, int height) {
        throw new UnsupportedOperationException("resize() is not supported");
    }

    @Override
    @CallSuper
    public void dispose() {
    }

    public Vector3 getForwardVector() {
        return GdxVr.graphics.getForward();
    }

    public Vector3 getUpVector() {
        return GdxVr.graphics.getUp();
    }

    public Vector3 getRightVector() {
        return GdxVr.graphics.getRight();
    }

    public Vector3 getHeadTranslation() {
        return GdxVr.graphics.getHeadTranslation();
    }

    public Quaternion getHeadQuaternion() {
        return GdxVr.graphics.getHeadQuaternion();
    }

    public Matrix4 getHeadMatrix() {
        return GdxVr.graphics.getHeadMatrix();
    }

    public VrCamera getVrCamera() {
        return vrCamera;
    }
}
