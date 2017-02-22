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

    public VideoPlayerScreen(VrGame game, Context context, VideoDetails videoDetails, @Nullable VideoOptions videoOptions) {
        super(game);
        this.context = context;
        this.videoDetails = videoDetails;
        ipd = 0;
        videoCamera = new PerspectiveCamera();
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        setBackgroundColor(Color.BLACK);
        if (videoOptions == null) {
            videoOptions = new VideoOptions();
        }
        inputMultiplexer = new VrInputMultiplexer();
        ui = new VideoPlayerGUI(this, ((MediaPlayerGame) game).getSkin(), videoOptions);
        ui.attach(inputMultiplexer);
        getVrCamera().near = 0.25f;
        getVrCamera().far = 100f;
        controllerEntity = getWorld().add(((MediaPlayerGame) game).getControllerEntity());
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
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.dispose();
        }
        videoPlayer = null;
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setProcessor(null);
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
        if (doRatioCalc) {
            final FieldOfView eyeFov = eye.getFov();
            Log.i(VideoPlayerScreen.class.getSimpleName(), eyeFov.toString());
            final float t1 = (float) Math.tan(Math.toRadians(eyeFov.getTop())) * getVrCamera().near;
            final float b1 = (float) Math.tan(Math.toRadians(eyeFov.getBottom())) * getVrCamera().near;
            yRatio = Math.abs(b1 / t1);
            doRatioCalc = false;
        }
//        videoCamera.view.setToLookAt(getForwardVector(), getUpVector()).mul(tempM.setToTranslation(tempV.set(getRightVector()).scl(eye.getType() == Eye.Type.RIGHT ? ipd * 0.5f : -ipd * 0.5f).add(getVrCamera().position).scl(-1)));
//        final float aspect = getVrCamera().viewportWidth / getVrCamera().viewportHeight;
//        final float fov = 105f * zoom;
//        final float wd2 = getVrCamera().near * (float) Math.tan(Math.toRadians(fov) / 2d);
//        final float shift = ipd * 0.5f * getVrCamera().near / projZ;
//        final float l = -aspect * wd2 + (eye.getType() == Eye.Type.RIGHT ? -shift : shift);
//        final float r = aspect * wd2 + (eye.getType() == Eye.Type.RIGHT ? -shift : shift);
//        final float ratio = yRatio;
//        final float b = -wd2;
//        final float t = wd2;
//        videoCamera.projection.setToProjection(l, r, b, t, getVrCamera().near, getVrCamera().far);
        final float l = (float) -Math.tan(Math.toRadians(eye.getFov().getLeft())) * getVrCamera().near;
        final float r = (float) Math.tan(Math.toRadians(eye.getFov().getRight())) * getVrCamera().near;
        final float t = (float) Math.tan(Math.toRadians(eye.getFov().getTop())) * getVrCamera().near;
        final float b = (float) -Math.tan(Math.toRadians(eye.getFov().getBottom())) * getVrCamera().near;
        videoCamera.projection.setToProjection(l / zoom, r / zoom, b / zoom, t / zoom, getVrCamera().near, getVrCamera().far);

//        videoCamera.view.setToTranslation(tempV.set(getForwardVector()).scl((1f - zoom) * 2f));
        videoCamera.view.set(eye.getEyeView());
        videoCamera.combined.set(videoCamera.projection);
        Matrix4.mul(videoCamera.combined.val, videoCamera.view.val);
//        this.fov.setAngles(eyeFov.getLeft() * zoom, eyeFov.getRight(), eyeFov.getBottom() * zoom, eyeFov.getTop() * zoom);
        getModelBatch().begin(getVrCamera());
        getWorld().render(getModelBatch(), environment);
        getModelBatch().end();

        getModelBatch().begin(videoCamera);
        videoPlayer.render(getModelBatch(), eye.getType());
        getModelBatch().end();
        render(videoCamera, eye.getType());
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
        this.ipd = MathUtils.clamp(ipd, -0.1f, 0.1f);
        videoPlayer.set3dShift(ipd);
    }

    public void exit() {
        ((MediaPlayerGame) game).goToSelectionScreen();
    }

    public VideoDetails getVideoDetails() {
        return videoDetails;
    }
}
