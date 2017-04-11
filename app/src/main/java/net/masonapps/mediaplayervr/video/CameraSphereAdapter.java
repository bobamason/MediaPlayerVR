package net.masonapps.mediaplayervr.video;

import android.util.Log;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.FrustumShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.base.Eye;

import org.masonapps.particletest.vr.VrCamera;

/**
 * Created by Bob on 4/11/2017.
 */

class CameraSphereAdapter extends BaseAppAdapter {
    private static final float defaultIpd = 0.064f;
    private static final Vector3 NEG_Z = new Vector3(0, 0, -1);
    private static final Vector3 tempV = new Vector3();
    private static final Quaternion tempQ = new Quaternion();
    private static final Matrix4 tempM = new Matrix4();
    private final Quaternion rotation = new Quaternion();
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private Frustum frustum0 = new Frustum();
    private Frustum frustum1 = new Frustum();
    private ModelInstance eye0Instance;
    private ModelInstance eye1Instance;
    private ModelInstance sphereInstance;
    private Array<ModelInstance> instances = new Array<>();
    private float rotCenterY = 0f;
    private float ipd = 1f;
    private float sphereDiameter = 10f;
    private float zoom = 1f;
    private Vector3 translation = new Vector3();
    private VrCamera leftCamera;
    private VrCamera rightCamera;
//    private boolean shouldUpdate = true;

    @Override
    protected void onCreate() {
        leftCamera = new VrCamera();
        rightCamera = new VrCamera();

        final ModelBuilder modelBuilder = new ModelBuilder();

        final Eye leftEye = new Eye(Eye.Type.LEFT);
        leftEye.getFov().setAngles();
        setCameraProjection(leftEye, leftCamera);
        final Eye rightEye = new Eye(Eye.Type.RIGHT);
        rightEye.getFov().setAngles();
        setCameraProjection(rightEye, rightCamera);

        final MeshPartBuilder eye0part = modelBuilder.part("eye0", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material());
        FrustumShapeBuilder.build(eye0part, frustum0, Color.SKY, Color.BLUE);
        eye0Instance = new ModelInstance(modelBuilder.end());

        final MeshPartBuilder eye1part = modelBuilder.part("eye1", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material());
        FrustumShapeBuilder.build(eye1part, frustum1, Color.CORAL, Color.RED);
        eye1Instance = new ModelInstance(modelBuilder.end());

        final MeshPartBuilder spherePart = modelBuilder.part("sphere", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
        SphereShapeBuilder.build(spherePart, 0.5f, 0.5f, 0.5f, 16, 8);
        sphereInstance = new ModelInstance(modelBuilder.end());
    }

    @Override
    protected void update() {
//        if(shouldUpdate) {
//            frustum0.update();
//            frustum1.update();
//            shouldUpdate = false;
//        }
        sphereInstance.transform.idt().translate(0, -2, -10).scale(sphereDiameter, sphereDiameter, sphereDiameter);
        eye0Instance.transform.idt().translate(0, -2, -10);
        eye1Instance.transform.idt().translate(0, -2, -10);
    }

    private Vector3 getForwardVector() {
        return NEG_Z;
    }

    private Vector3 getRightVector() {
        return Vector3.X;
    }

    private Vector3 getUpVector() {
        return Vector3.Y;
    }

    @Override
    protected void render(ModelBatch modelBatch) {
        modelBatch.render(instances);
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
    }

    private void setCameraProjection(Eye eye, VrCamera camera) {
        final float l = (float) -Math.tan(Math.toRadians(eye.getFov().getLeft())) * camera.near;
        final float r = (float) Math.tan(Math.toRadians(eye.getFov().getRight())) * camera.near;
        final float top = (float) Math.tan(Math.toRadians(eye.getFov().getTop())) * camera.near;
        final float bottom = (float) -Math.tan(Math.toRadians(eye.getFov().getBottom())) * camera.near;

        rotCenterY = (top + bottom) / 2f;

        final float side = (-l + r) / 2f;
        float left;
        float right;
        final float ipdHalf = defaultIpd * ipd / 2f;
        final float defaultShift = Math.abs(r - side);
        final float screenZ = (defaultIpd * 0.5f * camera.near) / defaultShift;
        sphereDiameter = screenZ * 2f;
        Log.d("setCameraProjection", (eye.getType() == Eye.Type.LEFT ? "left" : "right") + "eye");
        Log.d("setCameraProjection", "screenZ = " + screenZ + "m");
        final float shift = ipdHalf * camera.near / screenZ;
        Log.d("setCameraProjection", "defaultShift = " + defaultShift + ", shift = " + shift);
        if (eye.getType() == Eye.Type.LEFT) {
            left = -side + shift;
            right = side + shift;
        } else {
            left = -side - shift;
            right = side - shift;
        }
        camera.projection.setToProjection(left / zoom, right / zoom, bottom / zoom, top / zoom, camera.near, camera.far);
        final float a = 0f;
        translation.set(0, -rotCenterY, 0).rotate(Vector3.Z, a).add(0, rotCenterY, 0).add(tempV.set(getRightVector()).scl(-ipdHalf));
        leftCamera.view.setToLookAt(translation, tempV.set(translation).add(getForwardVector()), getUpVector());
        updateCamera(leftCamera);

        translation.set(0, -rotCenterY, 0).rotate(Vector3.Z, a).add(0, rotCenterY, 0).add(tempV.set(getRightVector()).scl(ipdHalf));
        rightCamera.view.setToLookAt(translation, tempV.set(translation).add(getForwardVector()), getUpVector());
        updateCamera(rightCamera);
    }

    private void updateCamera(VrCamera camera) {
        camera.combined.set(camera.projection);
        Matrix4.mul(camera.combined.val, camera.view.val);
    }
}
