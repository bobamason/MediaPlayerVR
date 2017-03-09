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
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.FieldOfView;
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
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

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
    private final PerspectiveCamera videoCamera;
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
    private VrInputMultiplexer inputMultiplexer;
    private ModelInstance sphereOutlineInstance;
    private boolean useCustomCamera = false;
    private Vector3 translation = new Vector3();

    public VideoPlayerScreen(VrGame game, Context context, VideoDetails videoDetails, @Nullable VideoOptions videoOptions) {
        super(game);
        this.context = context;
        this.videoDetails = videoDetails;
        this.videoOptions = videoOptions;
        if (this.videoOptions == null) {
            this.videoOptions = new VideoOptions();
            this.videoOptions.title = videoDetails.title;
        }
        videoCamera = new PerspectiveCamera();
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        setBackgroundColor(Color.BLACK);
        inputMultiplexer = new VrInputMultiplexer();
        ui = new VideoPlayerGUI(this, ((MediaPlayerGame) game).getSkin(), this.videoOptions);
        ui.attach(inputMultiplexer);
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
        GdxVr.input.setProcessor(inputMultiplexer);
        Log.i(VideoPlayerScreen.class.getSimpleName(), "default IPD = " + ipd);
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
        ui.update();
        videoPlayer.update();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onDrawEye(Eye eye) {
        getVrCamera().onDrawEye(eye);
        Gdx.gl.glClearColor(getBackgroundColor().r, getBackgroundColor().g, getBackgroundColor().b, getBackgroundColor().a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (useCustomCamera) {
            if (doRatioCalc) {
                final FieldOfView eyeFov = eye.getFov();
                Log.i(VideoPlayerScreen.class.getSimpleName(), eyeFov.toString());
                final float t1 = (float) Math.tan(Math.toRadians(eyeFov.getTop())) * getVrCamera().near;
                final float b1 = (float) Math.tan(Math.toRadians(eyeFov.getBottom())) * getVrCamera().near;
                yRatio = Math.abs(b1 / t1);
                doRatioCalc = false;
            }

//            setCameraProjectionZoom(eye);
            
            videoCamera.projection.set(getVrCamera().projection);

            translation.set(Vector3.Zero).mul(tempM.set(eye.getEyeView()));
            translation.scl(1f + ipd);
            translation.add(tempV.set(getForwardVector()).scl((1f - zoom) * 5f));
            Log.d(VideoPlayerScreen.class.getSimpleName(), (eye.getType() == Eye.Type.LEFT ? "left" : "right") + " eye position: " + translation.toString());
            videoCamera.view.setToLookAt(translation, tempV.set(translation).add(getForwardVector()), getUpVector());
        } else {
            videoCamera.view.set(getVrCamera().view);
            videoCamera.projection.set(getVrCamera().projection);
        }
        videoCamera.combined.set(videoCamera.projection);
        Matrix4.mul(videoCamera.combined.val, videoCamera.view.val);
        
        getModelBatch().begin(getVrCamera());
        getWorld().render(getModelBatch(), environment);
        getModelBatch().end();

        getModelBatch().begin((videoPlayer.useFlatRectangle() || !videoPlayer.isStereoscopic()) ? getVrCamera() : videoCamera);
        videoPlayer.render(getModelBatch(), eye.getType());
//        if (isUiVisible()) {
//            final float s = videoPlayer.getModelSize();
//            sphereOutlineInstance.transform.idt().scale(s, s, s);
//            getModelBatch().render(sphereOutlineInstance);
//        }
        getModelBatch().end();
        render(getVrCamera(), eye.getType());
    }

    private void setCameraProjectionZoom(Eye eye) {
        final float l = (float) -Math.tan(Math.toRadians(eye.getFov().getLeft())) * getVrCamera().near;
        final float r = (float) Math.tan(Math.toRadians(eye.getFov().getRight())) * getVrCamera().near;
        final float t = (float) Math.tan(Math.toRadians(eye.getFov().getTop())) * getVrCamera().near;
        final float b = (float) -Math.tan(Math.toRadians(eye.getFov().getBottom())) * getVrCamera().near;
        videoCamera.projection.setToProjection(l / zoom, r / zoom, b / zoom, t / zoom, getVrCamera().near, getVrCamera().far);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void render(Camera camera, int whichEye) {
        if (isUiVisible()) {
            ui.draw(camera);
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
                            if (!inputMultiplexer.isCursorOver()) {
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

    public void setZ(float z) {
//        getVrCamera().position.z = z;
        setZoom(z);
    }

    public void setZoom(float zoom) {
        this.zoom = MathUtils.clamp(zoom, 0.1f, 2f);
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

    public void setUseCustomCamera(boolean useCustomCamera) {
        this.useCustomCamera = useCustomCamera;
    }
}
