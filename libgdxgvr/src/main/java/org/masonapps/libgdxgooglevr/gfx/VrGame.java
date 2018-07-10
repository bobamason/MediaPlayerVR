package org.masonapps.libgdxgooglevr.gfx;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.UBJsonReader;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrCursor;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.vr.VrApplicationAdapter;

/**
 * Created by Bob on 12/22/2016.
 */

public class VrGame extends VrApplicationAdapter {
    public static final String CONTROLLER_FILENAME = "ddcontroller.g3db";
    private final Vector3 controllerScale = new Vector3(12f, 12f, 12f);
    private final Vector3 tmp = new Vector3();
    protected VrScreen screen;
    protected Ray ray = new Ray();
    protected boolean isCursorVisible = true;
    protected VrCursor cursor;
    private ShapeRenderer shapeRenderer;
    private Color cursorColor1 = new Color(1f, 1f, 1f, 1f);
    private Color cursorColor2 = new Color(1f, 1f, 1f, 0f);
    @Nullable
    private ModelInstance controllerModelInstance = null;
    private ModelBatch modelBatch;
    private AssetManager assets;
    private boolean isControllerVisible = true;
    private boolean loadingAssets = false;

    private static boolean shouldShowCursor(VrInputProcessor vrInputProcessor) {
        return vrInputProcessor != null && vrInputProcessor.isCursorOver() && vrInputProcessor.getHitPoint3D() != null;
    }

    @Override
    public void create() {
        super.create();
        assets = new AssetManager();
        modelBatch = createModelBatch();
        cursor = new VrCursor();
        cursor.setDeactivatedDiameter(0.02f);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        new Thread(() -> {
            final ModelData modelData = new G3dModelLoader(new UBJsonReader(), new InternalFileHandleResolver()).loadModelData(GdxVr.files.internal(CONTROLLER_FILENAME));
            GdxVr.app.postRunnable(() -> controllerModelInstance = new ModelInstance(new Model(modelData)));
        }).start();
    }

    protected ModelBatch createModelBatch() {
        return new ModelBatch(new PhongShaderProvider());
    }

    @Override
    public void pause() {
        if (screen != null) screen.pause();
    }

    @Override
    public void resume() {
        if (screen != null) screen.resume();
    }

    protected void doneLoading(AssetManager assets) {

    }

    @Override
    @CallSuper
    public void update() {
        if (loadingAssets) {
            if (assets.update()) {
                doneLoading(assets);
                loadingAssets = false;
            }
        }
        final VrInputProcessor vrInputProcessor = GdxVr.input.getVrInputProcessor();
        if (shouldShowCursor(vrInputProcessor)) {
            cursor.position.set(vrInputProcessor.getHitPoint3D());
            cursor.lookAtTarget(ray.origin, Vector3.Y);
        } else {
            cursor.position.set(ray.origin.x + ray.direction.x * 2.5f, ray.origin.y + ray.direction.y * 2.5f, ray.origin.z + ray.direction.z * 2.5f);
            cursor.lookAtTarget(ray.origin, Vector3.Y);
//            cursor.setVisible(!GdxVr.input.isControllerConnected());
        }
        if (screen != null) screen.update();
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        super.onDrawFrame(headTransform, leftEye, rightEye);
        if (screen != null) screen.onDrawFrame(headTransform, leftEye, rightEye);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        if (screen != null)
            screen.render(camera, whichEye);

//        renderController(camera);

        if (isCursorVisible)
            renderCursor(camera);
    }

    @SuppressWarnings("ConstantConditions")
    protected void renderController(Camera camera) {
        if (shouldRenderControllerModel()) {
            modelBatch.begin(camera);
            modelBatch.render(controllerModelInstance);
            modelBatch.end();
        }
    }

    public boolean shouldRenderControllerModel() {
        return controllerModelInstance != null && GdxVr.input.isControllerConnected() && isControllerVisible;
    }

    public void renderCursor(Camera camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (GdxVr.input.isControllerConnected()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin();
            Gdx.gl.glLineWidth(2f);
            tmp.set(ray.origin).lerp(cursor.position, 0.75f);
            shapeRenderer.line(ray.origin.x, ray.origin.y, ray.origin.z, tmp.x, tmp.y, tmp.z, cursorColor1, cursorColor2);
            shapeRenderer.end();
        }
        cursor.render(camera);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if (screen != null) screen.renderAfterCursor(camera);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        if (screen != null) screen.onNewFrame(headTransform);
    }

    @Override
    public void onDrawEye(Eye eye) {
        super.onDrawEye(eye);
        if (screen != null) screen.onDrawEye(eye);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        super.onFinishFrame(viewport);
        if (screen != null) screen.onFinishFrame(viewport);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        if (GdxVr.input.isControllerConnected()) {
            ray.set(GdxVr.input.getInputRay());
            if (controllerModelInstance != null) {
                controllerModelInstance.transform.set(GdxVr.input.getControllerPosition(), GdxVr.input.getControllerOrientation(), controllerScale);
            }
        }
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {

    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
        if (screen != null) screen.onCardboardTrigger();
    }

    public void loadAsset(String fileName, Class type) {
        assets.load(fileName, type);
        loadingAssets = true;
    }

    public void loadAsset(String fileName, Class type, AssetLoaderParameters params) {
        assets.load(fileName, type, params);
        loadingAssets = true;
    }

    public void loadAsset(AssetDescriptor desc) {
        assets.load(desc);
        loadingAssets = true;
    }

    @Nullable
    public ModelInstance getControllerModelInstance() {
        return controllerModelInstance;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        shapeRenderer = null;
        if (modelBatch != null) {
            modelBatch.dispose();
        }
        modelBatch = null;
        if (screen != null) {
            try {
                screen.hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                screen.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ModelBatch getModelBatch() {
        return modelBatch;
    }

    public VrScreen getScreen() {
        return screen;
    }

    public void setScreen(VrScreen screen) {
        try {
            if (this.screen != null)
                this.screen.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.screen = screen;
        try {
            if (this.screen != null)
                this.screen.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Ray getControllerRay() {
        return ray;
    }

    public boolean isCursorVisible() {
        return isCursorVisible;
    }

    public void setCursorVisible(boolean visible) {
        isCursorVisible = visible;
    }

    public void setControllerVisible(boolean controllerVisible) {
        this.isControllerVisible = controllerVisible;
    }

    public VrCursor getCursor() {
        return cursor;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}