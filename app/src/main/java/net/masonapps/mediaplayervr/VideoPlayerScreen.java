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
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.FrustumShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
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
    private static final Quaternion tempQ = new Quaternion();
    private static final Matrix4 tempM = new Matrix4();
    private static final Vector3 NEG_Z = new Vector3(0, 0, -1);
    private static final float NEAR = 1f;
    private static final float FAR = 101f;

    private final VideoDetails videoDetails;
    private final VideoPlayerGUI ui;
    private final VrCamera leftCamera;
    private final VrCamera rightCamera;
    private final ModelBuilder modelBuilder;
    private final Quaternion rotation = new Quaternion();
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private VideoOptions videoOptions;
    private Context context;
    private VrVideoPlayer videoPlayer;
    private boolean isButtonClicked = false;
    private long startPosition;
    private float ipd = 1f;
    private float zoom = 1f;
    private float yRatio = 1f;
    private boolean doRatioCalc = true;
    private VrUiContainer container;
    private ModelInstance sphereOutlineInstance;
    private boolean useCustomCamera = false;
    private Vector3 translation = new Vector3();
    private boolean projectionChanged;
    private float rotCenterY = 0f;
    private float sphereDiameter = 20f;
    private Frustum frustum0 = new Frustum();
    private Frustum frustum1 = new Frustum();
    private ModelInstance eye0Instance;
    private ModelInstance eye1Instance;
    private ModelInstance sphereInstance;
    private Quaternion headQuat = new Quaternion();
    private Vector3 right = new Vector3();
    private Vector3 up = new Vector3();
    private Vector3 forward = new Vector3();
    private boolean isUiVisible = true;

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
        setIpd(this.videoOptions.ipd);
        leftCamera = new VrCamera();
        leftCamera.near = NEAR;
        leftCamera.far = FAR;
        rightCamera = new VrCamera();
        rightCamera.near = NEAR;
        rightCamera.far = FAR;
        final MediaPlayerGame mediaPlayerGame = (MediaPlayerGame) game;
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height, mediaPlayerGame.getRectModel(), mediaPlayerGame.getSphereModel(), mediaPlayerGame.getCylinderModel());
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
        final Skin skin = mediaPlayerGame.getSkin();
        ui = new VideoPlayerGUI(this, spriteBatch, skin, this.videoOptions);
        ui.attach(container);
        getVrCamera().near = 0.125f;
        getVrCamera().far = 101f;
        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        final MeshPartBuilder part = modelBuilder.part("outline", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
        SphereShapeBuilder.build(part, 1, 1, 1, 12, 12);
        sphereOutlineInstance = new ModelInstance(modelBuilder.end());
        invalidateProjection();
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
        GdxVr.input.getDaydreamControllerHandler().addListener(this);
        GdxVr.input.setInputProcessor(container);
    }

    @Override
    public void hide() {
        pause();
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setInputProcessor(null);
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

        leftCamera.viewportWidth = leftEye.getViewport().width;
        leftCamera.viewportHeight = leftEye.getViewport().height;
        rightCamera.viewportWidth = rightEye.getViewport().width;
        rightCamera.viewportHeight = rightEye.getViewport().height;

        videoPlayer.setModelSize(videoPlayer.useFlatRectangle() ? 10f * zoom : sphereDiameter);

        if (leftEye.getProjectionChanged() | rightEye.getProjectionChanged())
            invalidateProjection();

        if (projectionChanged) {
            if (videoPlayer.useFlatRectangle()) {
                leftCamera.projection.set(leftEye.getPerspective(leftCamera.near, leftCamera.far));
                rightCamera.projection.set(rightEye.getPerspective(rightCamera.near, rightCamera.far));
            } else {
//                leftCamera.projection.set(leftEye.getPerspective(leftCamera.near, leftCamera.far));
//                rightCamera.projection.set(rightEye.getPerspective(rightCamera.near, rightCamera.far));
                setCameraProjection(leftEye, leftCamera);
                setCameraProjection(rightEye, rightCamera);

//                createVisualization();
            }
            projectionChanged = false;
        }

        final float ipdHalf = 0.064f * ipd / 2f;
//        if (videoPlayer.useFlatRectangle() || shouldRenderMono()) {
        setCameraViewFromEye(leftEye, leftCamera);
        updateCamera(leftCamera);

        setCameraViewFromEye(rightEye, rightCamera);
        updateCamera(rightCamera);
//
//            leftCamera.projection.set(leftEye.getPerspective(leftCamera.near, leftCamera.far));
//            rightCamera.projection.set(rightEye.getPerspective(rightCamera.near, rightCamera.far));
//        } else {

//        headQuat.slerp(getHeadQuaternion(), 0.75f);
//        right.set(1, 0, 0).mul(headQuat);
//        up.set(0, 1, 0).mul(headQuat);
//        forward.set(0, 0, -1).mul(headQuat);
////            final float a = getHeadQuaternion().getAngleAround(getForwardVector());
//
//        tempV.set(right);
//        tempV.y *= -1;
//        translation.set(tempV.scl(-ipdHalf));
////            translation.set(0, -rotCenterY, 0).rotate(Vector3.Z, a).add(0, rotCenterY, 0).add(tempV.set(getRightVector()).scl(-ipdHalf));
//        leftCamera.view.setToLookAt(translation, tempV.set(translation).add(forward), up);
//        updateCamera(leftCamera);
//        
//        tempV.set(right);
//        tempV.y *= -1;
//        translation.set(tempV.scl(ipdHalf));
////            translation.set(0, -rotCenterY, 0).rotate(Vector3.Z, a).add(0, rotCenterY, 0).add(tempV.set(getRightVector()).scl(ipdHalf));
//        rightCamera.view.setToLookAt(translation, tempV.set(translation).add(forward), up);
//        updateCamera(rightCamera);
////        }
    }

    private void createVisualization() {
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

    private boolean shouldRenderMono() {
        return (videoPlayer.isStereoscopic() && isUiVisible) &&
                ui.getCurrentSetting() != GlobalSettings.IPD &&
                ui.getCurrentSetting() != GlobalSettings.ZOOM;
    }

    private void setCameraViewFromEye(Eye eye, VrCamera camera) {
//        final Vector3 position = camera.position;
//        android.opengl.Matrix.setLookAtM(camera.view.getValues(), 0, position.x, position.y, position.z, position.x, position.y, position.z - 0.01f, 0f, 1f, 0f);
        camera.view.set(eye.getEyeView());
    }

    private void setCameraProjection(Eye eye, VrCamera camera) {
        final float l = (float) -Math.tan(Math.toRadians(eye.getFov().getLeft())) * camera.near;
        final float r = (float) Math.tan(Math.toRadians(eye.getFov().getRight())) * camera.near;
        final float top = (float) Math.tan(Math.toRadians(eye.getFov().getTop())) * camera.near;
        final float bottom = (float) -Math.tan(Math.toRadians(eye.getFov().getBottom())) * camera.near;

        rotCenterY = (top + bottom) / 2f;

//        final float side = (-l + r) / 2f;
        float left;
        float right;
//        final float defaultIpd = GdxVr.app.getSurfaceView().getInterpupillaryDistance();
//        final float ipdHalf = defaultIpd * ipd / 2f;
//        final float defaultShift = Math.abs(r - side);
//        final float screenZ = (defaultIpd * 0.5f * camera.near) / defaultShift;
//        sphereDiameter = 10f;
////        final float screenZ = sphereDiameter * 0.75f;
////        Log.d("setCameraProjection", (eye.getType() == Eye.Type.LEFT ? "left" : "right") + "eye");
////        Log.d("setCameraProjection", "screenZ = " + screenZ + "m");
//        final float shift = ipdHalf * camera.near / screenZ;
////        Log.d("setCameraProjection", "defaultShift = " + defaultShift + ", shift = " + shift);
//        if (eye.getType() == Eye.Type.LEFT) {
//            left = -side + shift;
//            right = side + shift;
//        } else {
//            left = -side - shift;
//            right = side - shift;
//        }
        final float shift = (ipd - 1f) * 0.1f;
        if (eye.getType() == Eye.Type.LEFT) {
            left = l + shift;
            right = r + shift;
        } else {
            left = l - shift;
            right = r - shift;
        }
        camera.projection.setToProjection(left / zoom, right / zoom, bottom / zoom, top / zoom, camera.near, camera.far);
    }

    private void updateCamera(VrCamera camera) {
        camera.combined.set(camera.projection);
        Matrix4.mul(camera.combined.val, camera.view.val);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        if (whichEye == Eye.Type.LEFT) {
            getModelBatch().begin(leftCamera);
            videoPlayer.render(getModelBatch(), shouldRenderMono() ? Eye.Type.MONOCULAR : Eye.Type.LEFT);
            getModelBatch().end();
        } else {
            getModelBatch().begin(rightCamera);
            videoPlayer.render(getModelBatch(), shouldRenderMono() ? Eye.Type.MONOCULAR : Eye.Type.RIGHT);
            getModelBatch().end();
        }
        super.render(camera, whichEye);
        container.draw(camera);
    }

    public void setUiVisible(boolean uiVisible) {
        isUiVisible = uiVisible;
        ui.setVisible(isUiVisible);
        game.setInputVisible(uiVisible);
        invalidateProjection();
    }

    public void invalidateProjection() {
        projectionChanged = true;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
    }

    @Override
    public void onConnectionStateChange(int connectionState) {

    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
        switch (event.button) {
            case DaydreamButtonEvent.BUTTON_TOUCHPAD:
                if (event.action == DaydreamButtonEvent.ACTION_DOWN) {
                    isButtonClicked = true;
                    if (videoPlayer.isPrepared()) {
                        if (isUiVisible) {
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
        invalidateProjection();
    }

    public float getIpd() {
        return ipd;
    }

    public void setIpd(float ipd) {
        this.ipd = MathUtils.clamp(ipd, -1f, 2f);
        invalidateProjection();
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

    public void setRotation(Vector3 dir, Vector3 up) {
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
    }
}
