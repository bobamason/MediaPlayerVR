package org.masonapps.libgdxgooglevr.vr;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.vr.sdk.base.Eye;

/**
 * Created by Bob on 10/14/2016.
 */

public class VrCamera extends Camera {

    private final Matrix4 tempM = new Matrix4();

    public VrCamera() {
        super();
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("call onDrawEye(GdxEye gdxEye) instead");
    }

    @Override
    public void update(boolean updateFrustum) {
        throw new UnsupportedOperationException("call onDrawEye(GdxEye gdxEye, boolean updateFrustum) instead");
    }

    @Override
    public void lookAt(Vector3 target) {
        throw new UnsupportedOperationException("call lookAt() on parent matrix onDrawEye(GdxEye gdxEye, Matrix4 parentMat) or onDrawEye(GdxEye gdxEye, ModelInstance parent) instead");
    }

    @Override
    public void lookAt(float x, float y, float z) {
        throw new UnsupportedOperationException("call lookAt() on parent matrix onDrawEye(GdxEye gdxEye, Matrix4 parentMat) or onDrawEye(GdxEye gdxEye, ModelInstance parent) instead");
    }

    @Override
    public void rotate(float angle, float axisX, float axisY, float axisZ) {
        throw new UnsupportedOperationException("call rotate() on parent matrix onDrawEye(GdxEye gdxEye, Matrix4 parentMat) or onDrawEye(GdxEye gdxEye, ModelInstance parent) instead");
    }

    @Override
    public void rotate(Vector3 axis, float angle) {
        throw new UnsupportedOperationException("call rotate() on parent matrix onDrawEye(GdxEye gdxEye, Matrix4 parentMat) or onDrawEye(GdxEye gdxEye, ModelInstance parent) instead");
    }

    @Override
    public void rotate(Quaternion quat) {
        throw new UnsupportedOperationException("call rotate() on parent matrix onDrawEye(GdxEye gdxEye, Matrix4 parentMat) or onDrawEye(GdxEye gdxEye, ModelInstance parent) instead");
    }

    @Override
    public void rotate(Matrix4 transform) {
        throw new UnsupportedOperationException("call rotate() on parent matrix onDrawEye(GdxEye gdxEye, Matrix4 parentMat) or onDrawEye(GdxEye gdxEye, ModelInstance parent) instead");
    }

    public void onDrawEye(Eye eye) {
        onDrawEye(eye, null, true);
    }

    public void onDrawEye(Eye eye, ModelInstance parent) {
        onDrawEye(eye, parent.transform, true);
    }

    public void onDrawEye(Eye eye, boolean updateFrustum) {
        onDrawEye(eye, null, updateFrustum);
    }

    public void onDrawEye(Eye eye, Matrix4 parentMat) {
        onDrawEye(eye, parentMat, true);
    }

    public void onDrawEye(Eye eye, Matrix4 parentMat, boolean updateFrustum) {
        viewportWidth = eye.getViewport().width;
        viewportHeight = eye.getViewport().height;
        android.opengl.Matrix.setLookAtM(view.getValues(), 0, position.x, position.y, position.z, position.x, position.y, position.z - 0.01f, 0f, 1f, 0f);
        view.mulLeft(tempM.set(eye.getEyeView()));
        if (parentMat != null) {
            view.mulLeft(parentMat);
        }
        projection.set(eye.getPerspective(near, far));
        combined.set(projection);
        Matrix4.mul(combined.val, view.val);

        if (updateFrustum) {
            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    }
}
