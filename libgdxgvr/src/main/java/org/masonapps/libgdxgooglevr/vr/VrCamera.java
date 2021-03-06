package org.masonapps.libgdxgooglevr.vr;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.base.Eye;

/**
 * Created by Bob on 10/14/2016.
 * based on PerspectiveCamera originally written by mzechner
 */

public class VrCamera extends Camera {

    private final Matrix4 tempM = new Matrix4();
    private final Vector3 tempV = new Vector3();

    public VrCamera() {
        super();
        near = 0.1f;
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
    public Ray getPickRay(float screenX, float screenY) {
        throw new UnsupportedOperationException("use controller ray or head forward");
    }

    @Override
    public Ray getPickRay(float screenX, float screenY, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
        throw new UnsupportedOperationException("use controller ray or head forward");
    }

    public void onDrawEye(Eye eye) {
        onDrawEye(eye, true);
    }

    public void onDrawEye(Eye eye, boolean updateFrustum) {
        viewportWidth = eye.getViewport().width;
        viewportHeight = eye.getViewport().height;
        view.setToLookAt(position, tempV.set(position).add(direction), up);
        view.mulLeft(tempM.set(eye.getEyeView()));
        projection.set(eye.getPerspective(near, far));
        combined.set(projection);
        Matrix4.mul(combined.val, view.val);

        if (updateFrustum) {
            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    }

    public Quaternion getQuaternion(Quaternion out) {
        final Matrix4 tmp = Pools.obtain(Matrix4.class);

        direction.nor();
//        tmp.set(up).crs(direction).nor();
//        up.set(direction).crs(tmp).nor();
        tmp.setToLookAt(direction, up).inv().tra();
        tmp.getRotation(out);
//        out.setFromAxes(false, tmp.x, up.x, direction.x, tmp.y, up.y, direction.y, tmp.z, up.z, direction.z);
//        out.setFromAxes(false, tmp.x, tmp.y, tmp.z, up.x, up.y, up.z, direction.x, direction.y, direction.z).conjugate();
        Pools.free(tmp);
        return out;
    }
}
