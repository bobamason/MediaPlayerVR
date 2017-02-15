package net.masonapps.mediaplayervr;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.google.vr.sdk.controller.Controller;

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

/**
 * Created by Bob on 12/24/2016.
 */

public class VideoPlayerScreen extends VrWorldScreen implements DaydreamControllerInputListener, VrVideoPlayer.CompletionListener, VrVideoPlayer.ErrorListener {

    private static final Vector3 tempV = new Vector3();
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
    private float projZ;

    public VideoPlayerScreen(VrGame game, Context context, VideoDetails videoDetails) {
        super(game);
        this.context = context;
        this.videoDetails = videoDetails;
        videoCamera = new PerspectiveCamera();
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        setBackgroundColor(Color.BLACK);
        ui = new VideoPlayerGUI(this, ((MediaPlayerGame) game).getSkin());
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
        GdxVr.input.setProcessor(ui.getStage());
        ipd = getDefaultIpd();
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

    @Override
    public void onDrawEye(Eye eye) {
//        final FieldOfView eyeFov = eye.getFov();
//        Log.i(VideoPlayerScreen.class.getSimpleName(), eyeFov.toString());
//        if(eye.getProjectionChanged()) {
//            float l = (float) (-Math.tan(Math.toRadians((double) eyeFov.getLeft()))) * getVrCamera().near;
//            float r = (float) Math.tan(Math.toRadians((double) eyeFov.getRight())) * getVrCamera().near;
//            final float ipdHalf = GdxVr.app.getGvrView().getInterpupillaryDistance() / 2f;
//            final float shift = (l + r) / 2f;
//            projZ = ipdHalf * getVrCamera().near / shift;
//        }
//        this.fov.setAngles(eyeFov.getLeft() * zoom, eyeFov.getRight(), eyeFov.getBottom() * zoom, eyeFov.getTop() * zoom);
        super.onDrawEye(eye);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void render(Camera camera, int whichEye) {
        Gdx.gl.glClearColor(getBackgroundColor().r, getBackgroundColor().g, getBackgroundColor().b, getBackgroundColor().a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        getModelBatch().begin(camera);
        getWorld().render(getModelBatch(), environment);
        getModelBatch().end();
        if (videoPlayer != null) {
            videoCamera.view.setToLookAt(tempV.set(getRightVector()).scl(whichEye == Eye.Type.RIGHT ? ipd * 0.5f : -ipd * 0.5f).add(camera.position), getForwardVector(), getUpVector());
            float aspect = getVrCamera().viewportWidth / getVrCamera().viewportHeight;
            float fov = 100f * zoom;
            float projZ = 0.94f;
            float wd2 = getVrCamera().near * (float) Math.tan(Math.toRadians(fov) / 2d);
            float shift = ipd * 0.5f * getVrCamera().near / projZ;
            float l = -aspect * wd2 + (whichEye == Eye.Type.RIGHT ? -shift : shift);
            float r = aspect * wd2 + (whichEye == Eye.Type.RIGHT ? -shift : shift);
            float b = -wd2;
            float t = wd2;
            videoCamera.projection.setToProjection(l, r, b, t, getVrCamera().near, getVrCamera().far);
            videoCamera.combined.set(videoCamera.projection);
            Matrix4.mul(videoCamera.combined.val, videoCamera.view.val);

            getModelBatch().begin(videoCamera);
            videoPlayer.render(getModelBatch(), whichEye);
            getModelBatch().end();
        }
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
                            if (!ui.getStage().isCursorOver()) {
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

    public float getDefaultIpd() {
        return GdxVr.app.getGvrView().getInterpupillaryDistance();
    }

    public float getIpd() {
        return ipd;
    }

    public void setIpd(float ipd) {
        this.ipd = Math.max(ipd, 0f);
    }
}
