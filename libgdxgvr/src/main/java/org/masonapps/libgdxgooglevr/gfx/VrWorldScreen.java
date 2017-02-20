package org.masonapps.libgdxgooglevr.gfx;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.VrCursor;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;

/**
 * Created by Bob on 10/9/2016.
 */

public abstract class VrWorldScreen extends VrScreen {
    protected Environment environment;
    protected World world;
    protected VrCursor cursor;
    protected Ray ray = new Ray();
    protected boolean isUiVisible = true;
    private AssetManager assets;
    private boolean loading = true;
    private Array<Disposable> disposables = new Array<>();
    private Color backgroundColor = Color.BLACK.cpy();
    private FloatAction floatAction;
    private ModelBatch modelBatch;
    private ShapeRenderer shapeRenderer;
    private Vector3 controllerPosition = new Vector3();
    private Color cursorColor1 = new Color(1f, 1f, 1f, 1f);
    private Color cursorColor2 = new Color(1f, 1f, 1f, 0f);

    public VrWorldScreen(VrGame game) {
        super(game);
        assets = new AssetManager();
        environment = createEnvironment();
        environment.add(createLight());
        world = createWorld();
        cursor = new VrCursor();
        cursor.setDeactivatedDiameter(0.02f);
        modelBatch = createModelBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        floatAction = new FloatAction();
        floatAction = new FloatAction(0f, 1f);
        floatAction.setInterpolation(new Interpolation.Swing(1f));
        floatAction.setDuration(1f);
    }

    protected Environment createEnvironment() {
        final Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.DARK_GRAY));
        return environment;
    }

    protected BaseLight createLight() {
        final DirectionalLight light = new DirectionalLight();
        light.set(Color.DARK_GRAY, 0.0f, -1.0f, 0.0f);
        return light;
    }

    protected ModelBatch createModelBatch() {
        return new ModelBatch();
    }

    protected World createWorld() {
        return new World();
    }

    @Override
    @CallSuper
    public void update() {
        if (loading) {
            if (assets.update()) {
                doneLoading();
                loading = false;
            }
        }
        world.update();
    }

    protected void doneLoading() {
    }

    @Override
    @CallSuper
    public void render(Camera camera, int whichEye) {
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        getModelBatch().begin(camera);
        world.render(getModelBatch(), environment);
        getModelBatch().end();
    }

    @Override
    protected void renderCursor(Camera camera) {
        if (!isUiVisible) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (GdxVr.input.isControllerConnected()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin();
            Gdx.gl.glLineWidth(2f);
            shapeRenderer.line(ray.origin.x, ray.origin.y, ray.origin.z, cursor.position.x, cursor.position.y, cursor.position.z, cursorColor1, cursorColor2);
            shapeRenderer.end();
        }
        cursor.render(camera);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (GdxVr.input.isControllerConnected()) {
            ray.set(GdxVr.input.getInputRay());
            final VrInputProcessor vrInputProcessor = GdxVr.input.getVrInputProcessor();
            if (vrInputProcessor != null && vrInputProcessor.isCursorOver()) {
                cursor.position.set(vrInputProcessor.getHitPoint3D());
                cursor.lookAtTarget(ray.origin, Vector3.Y);
//                floatAction.restart();
                cursor.setVisible(true);
            } else {
                final float s = 2f;
                cursor.position.set(ray.direction.x * s + ray.origin.x, ray.direction.y * s + ray.origin.y, ray.direction.z * s + ray.origin.z);
                cursor.lookAtTarget(ray.origin, Vector3.Y);
//                floatAction.setReverse(true);
//                floatAction.restart();
                cursor.setVisible(false);
            }
        }
    }

    private Vector3 getControllerPosition() {
        return controllerPosition.set(GdxVr.input.getControllerPosition()).add(GdxVr.input.getControllerOffset());
    }

    private Quaternion getControllerOrientation() {
        return GdxVr.input.getControllerOrientation();
    }

    @Override
    @CallSuper
    public void dispose() {
        if (disposables != null) {
            for (Disposable d : disposables) {
                if (d != null)
                    d.dispose();
            }
            if (modelBatch != null) {
                modelBatch.dispose();
            }
            disposables.clear();
        }
        if (assets != null)
            assets.dispose();
        if (world != null)
            world.dispose();
        assets = null;
        world = null;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ModelBatch getModelBatch() {
        return modelBatch;
    }

    public Array<Disposable> getDisposables() {
        return disposables;
    }

    public World getWorld() {
        return world;
    }

    public void loadAsset(String filename, Class<?> type) {
        assets.load(filename, type);
        loading = true;
    }

    public void loadAsset(AssetDescriptor desc) {
        assets.load(desc);
        loading = true;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        this.backgroundColor.set(r, g, b, a);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int rgba) {
        this.backgroundColor.set(rgba);
    }

    public void manageDisposable(Disposable... disposables) {
        for (Disposable d : disposables) {
            this.disposables.add(d);
        }
    }

    public void manageDisposable(Disposable disposable) {
        this.disposables.add(disposable);
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public Ray getControllerRay() {
        return ray;
    }

    public boolean isUiVisible() {
        return isUiVisible;
    }

    public void setUiVisible(boolean uiVisible) {
        isUiVisible = uiVisible;
    }
}
