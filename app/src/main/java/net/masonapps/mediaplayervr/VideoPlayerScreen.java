package net.masonapps.mediaplayervr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
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
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;
import org.masonapps.libgdxgooglevr.vr.VrActivityGVR;
import org.masonapps.libgdxgooglevr.vr.VrCamera;

import java.nio.IntBuffer;

/**
 * Created by Bob on 12/24/2016.
 */

public class VideoPlayerScreen extends VrWorldScreen implements VrVideoPlayer.CompletionListener, VrVideoPlayer.ErrorListener {

    private static final Vector3 tempV = new Vector3();
    private static final Quaternion tempQ = new Quaternion();
    private static final Matrix4 tempM = new Matrix4();
    private static final Vector3 NEG_Z = new Vector3(0, 0, -1);

    private final VideoDetails videoDetails;
    private final VideoPlayerGUI ui;
    private final VrCamera leftCamera;
    private final VrCamera rightCamera;
    private final ModelBuilder modelBuilder;
    private final Quaternion rotation = new Quaternion();
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final FrameBuffer fbo;
    private final IntBuffer intBuffer;
    private final Entity rectEntity;
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
    private boolean isUiVisible = true;
    private PerspectiveCamera perspectiveCamera = new PerspectiveCamera(100f, 1024, 1024);
    private Matrix3 mat3;
    private float rectScale = 1f;
    private float planeZ = 4f;
    private Vector3 rectPosition = new Vector3();
    private float perspectiveFOV = 100f;
    private Matrix4 transform = new Matrix4();
    private float eyeAngle = 0f;
    private float tilt = 0f;

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
//        getWorld().add(Style.newGradientBackground(getVrCamera().far - 1f));
        setIpd(this.videoOptions.ipd);
        leftCamera = new VrCamera();
        leftCamera.near = getVrCamera().near;
        leftCamera.far = getVrCamera().far;
        rightCamera = new VrCamera();
        rightCamera.near = getVrCamera().near;
        rightCamera.far = getVrCamera().far;
        final MediaPlayerGame mediaPlayerGame = (MediaPlayerGame) game;
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height, mediaPlayerGame.getRectModel(), mediaPlayerGame.getSphereModel(), mediaPlayerGame.getCylinderModel());
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        videoPlayer.setVideoOptions(this.videoOptions);
        setBackgroundColor(Color.BLACK);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        container = new VrUiContainer();
        final Skin skin = mediaPlayerGame.getSkin();
        ui = new VideoPlayerGUI(this, spriteBatch, skin, this.videoOptions);
        ui.attach(container);
        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        final MeshPartBuilder part = modelBuilder.part("outline", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
        SphereShapeBuilder.build(part, 1, 1, 1, 12, 12);
        sphereOutlineInstance = new ModelInstance(modelBuilder.end());
        invalidateProjection();

        fbo = new FrameBuffer(Pixmap.Format.RGB888, 20, 10, false);
        manageDisposable(fbo);
        intBuffer = IntBuffer.allocate(1);
        rectEntity = new Entity(new ModelInstance(createRect(fbo.getColorBufferTexture())));
//        getWorld().add(rectEntity);
    }

    private Model createRect(Texture texture) {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Material material = new Material(TextureAttribute.createDiffuse(texture));
        float r = 0.5f;
        final Model rect = modelBuilder.createRect(
                -r, -r, 0,
                r, -r, 0,
                r, r, 0,
                -r, r, 0,
                0, 0, r,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);
        mat3 = new Matrix3();
        rect.meshes.get(0).transformUV(mat3.idt().scale(0.5f, -1));
        rect.meshes.get(0).transformUV(mat3.idt().translate(0, 1));
        return rect;
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
        super.show();
        GdxVr.input.setInputProcessor(container);
        GdxVr.input.setUpdateRayEnabled(true);
    }

    @Override
    public void hide() {
        super.hide();
        pause();
        GdxVr.input.setInputProcessor(null);
        GdxVr.input.setUpdateRayEnabled(true);
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
//            final float left = (float) -Math.tan(Math.toRadians(leftEye.getFov().getLeft())) * planeZ;
//            final float right = (float) Math.tan(Math.toRadians(rightEye.getFov().getRight())) * planeZ;
//            final float t = leftEye.getFov().getTop();
//            final float b = leftEye.getFov().getBottom();
//            final float top = (float) Math.tan(Math.toRadians(t)) * planeZ;
//            final float bottom = (float) -Math.tan(Math.toRadians(b)) * planeZ;
//            
//            rectScale = Math.max(right - left, top - bottom);
//            rectPosition.set((right + left) / 2f, (top + bottom) / 2f, -planeZ);
//            perspectiveFOV = t + b;


            if (videoPlayer.useFlatRectangle()) {
                leftCamera.projection.set(leftEye.getPerspective(leftCamera.near, leftCamera.far));
                rightCamera.projection.set(rightEye.getPerspective(rightCamera.near, rightCamera.far));
            } else {
                leftCamera.projection.set(leftEye.getPerspective(leftCamera.near, leftCamera.far));
                rightCamera.projection.set(rightEye.getPerspective(rightCamera.near, rightCamera.far));
//                setCameraProjection(leftEye, leftCamera);
//                setCameraProjection(rightEye, rightCamera);

//                createVisualization();
            }
            projectionChanged = false;
        }
//        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, intBuffer);
//        final int defaultBufferAddress = intBuffer.get(0);
//
//        fbo.begin();
//        GdxVr.gl.glClearColor(0, 0, 0, 0);
//        GdxVr.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        perspectiveCamera.direction.set(getForwardVector());
//        perspectiveCamera.fieldOfView = perspectiveFOV * zoom;
//        perspectiveCamera.update(false);
//
//        GdxVr.gl.glViewport(0, 0, fbo.getWidth() / 2, fbo.getHeight());
//        getModelBatch().begin(perspectiveCamera);
//        videoPlayer.render(getModelBatch(), leftEye.getType());
//        getModelBatch().end();
//
//        GdxVr.gl.glViewport(fbo.getWidth() / 2, 0, fbo.getWidth() / 2, fbo.getHeight());
//        getModelBatch().begin(perspectiveCamera);
//        videoPlayer.render(getModelBatch(), rightEye.getType());
//        getModelBatch().end();
//        fbo.end();
//
//        GdxVr.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultBufferAddress);

//        if (videoPlayer.useFlatRectangle() || shouldRenderMono()) {
        setCameraViewFromEye(leftEye, leftCamera);
        updateCamera(leftCamera);

        setCameraViewFromEye(rightEye, rightCamera);
        updateCamera(rightCamera);

//        if (videoPlayer instanceof VrVideoPlayerExo) {
//            final GvrAudioProcessor gvrAudioProcessor = ((VrVideoPlayerExo) videoPlayer).getGvrAudioProcessor();
//            if (gvrAudioProcessor != null) {
//                gvrAudioProcessor.updateOrientation(headQuaternion.w, headQuaternion.x, headQuaternion.y, headQuaternion.z);
//            }
//        }

        clearColorAndDepthBuffers();
        videoPlayer.update();
        videoPlayer.bindTexture();

        Viewport viewport = leftEye.getViewport();
        GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        renderLeftVideo();
        getVrCamera().onDrawEye(leftEye);
        renderUI(getVrCamera(), leftEye.getType());

        viewport = rightEye.getViewport();
        GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        renderRightVideo();
        getVrCamera().onDrawEye(rightEye);
        renderUI(getVrCamera(), rightEye.getType());

//        if (ui.isVisible()) {
//            viewport = leftEye.getViewport();
//            GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
//
//            viewport = rightEye.getViewport();
//            GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
//        }
    }

    private void clearColorAndDepthBuffers() {
        final Color backgroundColor = getBackgroundColor();
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private boolean shouldRenderMono() {
        return (videoPlayer.isStereoscopic() && isUiVisible) &&
                ui.getCurrentSettingKey() != VideoOptions.KEY_IPD &&
                ui.getCurrentSettingKey() != VideoOptions.KEY_EYE_ANGLE &&
                ui.getCurrentSettingKey() != VideoOptions.KEY_ZOOM;
    }

    private void setCameraViewFromEye(Eye eye, VrCamera camera) {
//        camera.view.set(eye.getEyeView());
        final Vector3 pos = Pools.obtain(Vector3.class);
        final Vector3 dir = Pools.obtain(Vector3.class);
        final float defaultIpd = ((VrActivityGVR) GdxVr.app.getContext()).getGvrView().getInterpupillaryDistance();
        final float ipdHalf = defaultIpd * ipd / 2f;
        pos.set(eye.getType() == Eye.Type.LEFT ? -ipdHalf : ipdHalf, 0, 2f - zoom * 2f);

        final double a = Math.toRadians(eyeAngle / 2. * (eye.getType() == Eye.Type.LEFT ? 1. : -1.));
        dir.set((float) Math.sin(a), 0, (float) -Math.cos(a));

//        final float a = Math.toRadians(eyeAngle / 2f * (eye.getType() == Eye.Type.LEFT ? 1f : -1f));
//        dir.set(0, 0, -1f).rotate(Vector3.Y, a);
        
        camera.view.setToLookAt(pos, dir.add(pos), Vector3.Y);
        camera.position.set(pos);
        Pools.free(pos);
        Pools.free(dir);
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
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void render(Camera camera, int whichEye) {
    }

    private void renderUI(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        container.draw(camera);
    }

    private void renderRightVideo() {
        Quaternion tmpQ = Pools.obtain(Quaternion.class);
        final Quaternion headQuaternion = getHeadQuaternion();
        rotation.set(headQuaternion).conjugate();
        tmpQ.set(Vector3.X, tilt * -90f);
        transform.idt().rotate(rotation.mul(tmpQ));
//                    .translate(-rightCamera.position.x, -rightCamera.position.y, -rightCamera.position.z)
//                    .rotate(rotation.mulLeft(tmpQ))
//                    .translate(rightCamera.position.x, rightCamera.position.y, rightCamera.position.z);
        Pools.free(tmpQ);

        videoPlayer.render(rightCamera, shouldRenderMono() ? Eye.Type.MONOCULAR : Eye.Type.RIGHT, transform);
        getModelBatch().end();
//            ((TextureAttribute) rectEntity.modelInstance.materials.get(0).get(TextureAttribute.Diffuse)).offsetU = 0.5f;
    }

    private void renderLeftVideo() {

        Quaternion tmpQ = Pools.obtain(Quaternion.class);
        final Quaternion headQuaternion = getHeadQuaternion();
        rotation.set(headQuaternion).conjugate();
        tmpQ.set(Vector3.X, tilt * -90f);
        transform.idt().rotate(rotation.mul(tmpQ));
//                    .translate(-leftCamera.position.x, -leftCamera.position.y, -leftCamera.position.z)
//                    .rotate(rotation.mulLeft(tmpQ))
//                    .translate(leftCamera.position.x, leftCamera.position.y, leftCamera.position.z);
        Pools.free(tmpQ);

        videoPlayer.render(leftCamera, shouldRenderMono() ? Eye.Type.MONOCULAR : Eye.Type.LEFT, transform);
//            ((TextureAttribute) rectEntity.modelInstance.materials.get(0).get(TextureAttribute.Diffuse)).offsetU = 0f;
    }

    private void setUiVisible(boolean uiVisible) {
        isUiVisible = uiVisible;
        ui.setVisible(isUiVisible);
        GdxVr.input.setUpdateRayEnabled(uiVisible);
        game.setCursorVisible(uiVisible);
        game.setControllerVisible(uiVisible);
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
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        super.onControllerButtonEvent(controller, event);
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
                    GdxVr.input.setUpdateRayEnabled(true);
                    ui.backButtonClicked();
                }
                break;
        }
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        super.onControllerTouchPadEvent(controller, event);
        if (!isButtonClicked) {
            if (ui.sliderLayout.isVisible())
                ui.sliderLayout.onTouchPadEvent(event);
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
        this.ipd = MathUtils.clamp(ipd, VideoOptions.MIN_IPD, VideoOptions.MAX_IPD);
        invalidateProjection();
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public void setEyeAngle(float angle) {
        this.eyeAngle = angle;
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
        setIpd(videoOptions.ipd);
        setEyeAngle(videoOptions.eyeAngle);
        setZoom(videoOptions.zoom);
        videoPlayer.setVideoOptions(videoOptions);
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
