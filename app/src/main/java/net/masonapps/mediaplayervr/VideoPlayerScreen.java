package net.masonapps.mediaplayervr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.video.VrVideoPlayerExo;
import net.masonapps.mediaplayervr.video.ui.VideoPlayerGUI;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;
import org.masonapps.libgdxgooglevr.vr.VrCamera;

/**
 * Created by Bob on 12/24/2016.
 */

public class VideoPlayerScreen extends VrWorldScreen implements DaydreamControllerInputListener, VrVideoPlayer.CompletionListener, VrVideoPlayer.ErrorListener {

    private static final Vector3 tempV = new Vector3();
    private static final Matrix4 tempM = new Matrix4();
    private final Vector3 controllerScale = new Vector3(10f, 10f, 10f);

    private final VideoDetails videoDetails;
    private final Entity controllerEntity;
    private final VideoPlayerGUI ui;
    private final VrCamera leftCamera;
    private final VrCamera rightCamera;
    private VideoOptions videoOptions;
    //    private final FieldOfView fov = new FieldOfView();
//    private final float[] proj = new float[16];
    private Context context;
    private VrVideoPlayer videoPlayer;
    private boolean isButtonClicked = false;
    private long startPosition;
    private float ipd;
    private float zoom = 1f;
    private float projZ = 0.94f;
    private float yRatio = 1f;
    private boolean doRatioCalc = true;
    private VrUiContainer container;
    private ModelInstance sphereOutlineInstance;
    private boolean useCustomCamera = false;
    private Vector3 translation = new Vector3();
    private boolean projectionChanged = true;

    public VideoPlayerScreen(VrGame game, Context context, VideoDetails videoDetails, @Nullable VideoOptions videoOptions) {
        super(game);
        this.context = context;
        this.videoDetails = videoDetails;
        Log.i(VideoPlayerScreen.class.getSimpleName(), videoDetails.title + ": " + videoDetails.tags);
        this.videoOptions = videoOptions;
        if (this.videoOptions == null) {
            this.videoOptions = new VideoOptions();
            this.videoOptions.title = videoDetails.title;
        }
        leftCamera = new VrCamera();
        rightCamera = new VrCamera();
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        final GlobalSettings globalSettings = GlobalSettings.getInstance();
        videoPlayer.getShader().setBrightness(globalSettings.brightness);
        videoPlayer.getShader().setContrast(globalSettings.contrast);
        videoPlayer.getShader().setTint(globalSettings.tint);
        videoPlayer.getShader().setColorTemp(globalSettings.colorTemp);
        setBackgroundColor(Color.BLACK);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        container = new VrUiContainer();
        final Skin skin = ((MediaPlayerGame) game).getSkin();
        ui = new VideoPlayerGUI(this, spriteBatch, skin, this.videoOptions);
        ui.attach(container);
        getVrCamera().near = 0.25f;
        getVrCamera().far = 100f;
        controllerEntity = getWorld().add(((MediaPlayerGame) game).getControllerEntity());
        final ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        final MeshPartBuilder part = modelBuilder.part("outline", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
        SphereShapeBuilder.build(part, 1, 1, 1, 12, 12);
        sphereOutlineInstance = new ModelInstance(modelBuilder.end());
    }

    @Override
    public void resume() {
        if (!videoPlayer.isPrepared()) {
            videoPlayer.play(videoDetails.uri);
        } else {
            videoPlayer.resume();
        }
    }

    @Override
    public void pause() {
        videoPlayer.pause();
    }

    @Override
    public void show() {
        GdxVr.app.getGvrView().setNeckModelEnabled(false);
        GdxVr.app.getGvrView().setNeckModelFactor(0f);
        GdxVr.input.getDaydreamControllerHandler().addListener(this);
        GdxVr.input.setProcessor(container);
    }

    @Override
    public void hide() {
        pause();
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setProcessor(null);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            videoPlayer.stop();
            videoPlayer.dispose();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void update() {
        super.update();
        container.act();
        ui.update();
        videoPlayer.update();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        onNewFrame(headTransform);

        leftCamera.viewportWidth = leftEye.getViewport().width;
        leftCamera.viewportHeight = leftEye.getViewport().height;
        rightCamera.viewportWidth = rightEye.getViewport().width;
        rightCamera.viewportHeight = rightEye.getViewport().height;

        videoPlayer.setModelSize(videoPlayer.useFlatRectangle() ? 10f * zoom : 10f);

        if (leftEye.getProjectionChanged() | rightEye.getProjectionChanged())
            projectionChanged = true;

        if (projectionChanged) {
            if (videoPlayer.useFlatRectangle()) {
                leftCamera.projection.set(leftEye.getPerspective(leftCamera.near, leftCamera.far));
                rightCamera.projection.set(rightEye.getPerspective(rightCamera.near, rightCamera.far));
            } else {
                setCameraProjectionZoom(leftEye, leftCamera, zoom);
                setCameraProjectionZoom(rightEye, rightCamera, zoom);
            }
            projectionChanged = false;
        }

        final float ipdHalf = GdxVr.app.getGvrView().getInterpupillaryDistance() / 2f * ipd;
        if (!videoPlayer.useFlatRectangle() && videoPlayer.isStereoscopic()) {
            translation.set(-ipdHalf, 0, 0);
            leftCamera.view.setToLookAt(translation, tempV.set(translation).add(getForwardVector()), getUpVector());
            updateCamera(leftCamera);

            translation.set(ipdHalf, 0, 0);
            rightCamera.view.setToLookAt(translation, tempV.set(translation).add(getForwardVector()), getUpVector());
            updateCamera(rightCamera);
        } else {
            setCameraViewFromEye(leftEye, leftCamera);
            updateCamera(leftCamera);

            setCameraViewFromEye(rightEye, rightCamera);
            updateCamera(rightCamera);
        }

        getModelBatch().begin(leftCamera);
        videoPlayer.render(getModelBatch(), (videoPlayer.isStereoscopic() && isUiVisible) ? Eye.Type.MONOCULAR : leftEye.getType());
        getModelBatch().end();

        getModelBatch().begin(rightCamera);
        videoPlayer.render(getModelBatch(), (videoPlayer.isStereoscopic() && isUiVisible) ? Eye.Type.MONOCULAR : rightEye.getType());
        getModelBatch().end();

        onDrawEye(leftEye);
        onDrawEye(rightEye);
    }

    private void setCameraViewFromEye(Eye eye, VrCamera camera) {
        final Vector3 position = camera.position;
        android.opengl.Matrix.setLookAtM(camera.view.getValues(), 0, position.x, position.y, position.z, position.x, position.y, position.z - 0.01f, 0f, 1f, 0f);
        camera.view.mulLeft(tempM.set(eye.getEyeView()));
    }

    private void setCameraProjectionZoom(Eye eye, VrCamera camera, float zoom) {
        final float l = (float) -Math.tan(Math.toRadians(eye.getFov().getLeft())) * getVrCamera().near;
        final float r = (float) Math.tan(Math.toRadians(eye.getFov().getRight())) * getVrCamera().near;
        final float t = (float) Math.tan(Math.toRadians(eye.getFov().getTop())) * getVrCamera().near;
        final float b = (float) -Math.tan(Math.toRadians(eye.getFov().getBottom())) * getVrCamera().near;
        camera.projection.setToProjection(l / zoom, r / zoom, b / zoom, t / zoom, getVrCamera().near, getVrCamera().far);
    }

    private void updateCamera(VrCamera camera) {
        camera.combined.set(camera.projection);
        Matrix4.mul(camera.combined.val, camera.view.val);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void render(Camera camera, int whichEye) {
        container.draw(camera);
        if (isUiVisible()) {
            renderCursor(camera);
        }
    }

    @Override
    public void setUiVisible(boolean uiVisible) {
        super.setUiVisible(uiVisible);
        ui.setVisible(uiVisible);
        controllerEntity.setRenderingEnabled(uiVisible);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (controllerEntity.isRenderingEnabled())
            controllerEntity.modelInstance.transform.set(tempV.set(GdxVr.input.getControllerPosition()).add(GdxVr.input.getHandPosition()), GdxVr.input.getControllerOrientation(), controllerScale);
    }

    @Override
    public void onConnectionStateChange(int connectionState) {
        if (connectionState == Controller.ConnectionStates.CONNECTED) {

        } else {

        }
    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
        switch (event.button) {
            case DaydreamButtonEvent.BUTTON_TOUCHPAD:
                if (event.action == DaydreamButtonEvent.ACTION_DOWN) {
                    isButtonClicked = true;
                    if (videoPlayer.isPrepared()) {
                        if (isUiVisible()) {
                            if (!container.isCursorOver()) {
                                setUiVisible(false);
                            }
                        } else {
                            setUiVisible(true);
                        }
                    }
                } else if (event.action == DaydreamButtonEvent.ACTION_UP) {
                    isButtonClicked = false;
                }
                break;
            case DaydreamButtonEvent.BUTTON_APP:
                if (event.action == DaydreamButtonEvent.ACTION_UP) {
                    if (ui.thumbSeekbarLayout.isVisible()) {
                        setUiVisible(false);
                        ui.hideThumbSeekbarLayout();
                    } else
                        ui.backButtonClicked();
                }
                break;
        }
    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        if (!isButtonClicked) {
            if (ui.thumbSeekbarLayout.isVisible())
                ui.thumbSeekbarLayout.onTouchPadEvent(event);
            switch (event.action) {
                case DaydreamTouchEvent.ACTION_DOWN:
                    break;
                case DaydreamTouchEvent.ACTION_MOVE:
                    break;
                case DaydreamTouchEvent.ACTION_UP:
                    break;
            }
        }
    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onError(String error) {

    }

    public String getStringResource(int resId) {
        return context.getString(resId);
    }

    public VrVideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        projectionChanged = true;
    }

    public float getIpd() {
        return ipd;
    }

    public void setIpd(float ipd) {
        this.ipd = MathUtils.clamp(ipd, -1f, 2f);
//        videoPlayer.set3dShift(ipd);
    }

    public void exit() {
        ((MediaPlayerGame) game).goToSelectionScreen();
    }

    public VideoDetails getVideoDetails() {
        return videoDetails;
    }

    public VideoOptions getVideoOptions() {
        return videoOptions;
    }

    public void restoreDefaultVideoOptions() {
        videoOptions.restoreDefaults();
    }

    public void restoreDefaultGlobalSettings() {
        GlobalSettings.getInstance().restoreDefault();
    }

    public void setUseCustomCamera(boolean useCustomCamera) {
        this.useCustomCamera = useCustomCamera;
    }
}
