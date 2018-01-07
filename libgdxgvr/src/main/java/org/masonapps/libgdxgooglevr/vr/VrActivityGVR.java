package org.masonapps.libgdxgooglevr.vr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.AndroidNet;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import org.masonapps.libgdxgooglevr.GdxVr;

import java.lang.ref.WeakReference;

/**
 * Created by Bob on 7/20/2017.
 */

public class VrActivityGVR extends GvrActivity {

    public static final String TAG = VrActivityGVR.class.getName();

    static {
        GdxNativesLoader.load();
    }

    protected boolean firstResume = true;
    protected ControllerManager controllerManager;
    protected Controller controller;
    private GLSurfaceView surfaceView;
    private VrActivity.VrApplication app;
    private GvrView gvrView;
    private GvrAudioEngine gvrAudioEngine;
//    private int wasFocusChanged = -1;
//    private boolean isWaitingForAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.app = new VrActivity.VrApplication(new WeakReference<>((Activity) this));

        AndroidCompat.setVrModeEnabled(this, true);

        gvrView = new GvrView(this);
        initGvrView(gvrView);
        Log.d(TAG, "Gvr Viewer Params: " + gvrView.getGvrViewerParams().toString());

        final EventListener listener = new EventListener();
        controllerManager = new ControllerManager(this, listener);
        controller = controllerManager.getController();
        controller.setEventListener(listener);
    }

    protected void initGvrView(GvrView gvrView) {
    }

    public void initialize(VrApplicationAdapter adapter) {

        gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        app.graphics = new VrGraphicsGVR(app, gvrAudioEngine);
        gvrView.setRenderer(app.graphics);

        app.input = new VrAndroidInput(app, new WeakReference<Context>(this));
        app.input.setController(controller);
//        audio = new AndroidAudio(this, config);
        app.files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        app.net = new AndroidNet(app);
        app.vrApplicationAdapter = adapter;
        app.handler = new Handler();


        Gdx.app = app;
        Gdx.input = app.input;
        Gdx.audio = app.getAudio();
        Gdx.files = app.getFiles();
        Gdx.graphics = app.graphics;
        Gdx.gl = app.graphics.getGL20();
        Gdx.gl20 = app.graphics.getGL20();
        Gdx.net = app.getNet();

        GdxVr.app = app;
        GdxVr.input = app.input;
        GdxVr.audio = app.getAudio();
        GdxVr.files = app.getFiles();
        GdxVr.graphics = app.graphics;
        GdxVr.gl = app.graphics.getGL20();
        GdxVr.gl20 = app.graphics.getGL20();
        GdxVr.net = app.getNet();

        setContentView(gvrView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            this.wasFocusChanged = 1;
//            if (this.isWaitingForAudio) {
//                // TODO: 10/11/2016 fix audio 
////                this.audio.resume();
////                this.isWaitingForAudio = false;
//            }
//        } else {
//            this.wasFocusChanged = 0;
//        }
    }

    @Override
    protected void onPause() {
        // calls to setContinuousRendering(false) from other thread (ex: GLThread)
        // will be ignored at this point...
        getGvrView().queueEvent(new Runnable() {
            @Override
            public void run() {
                app.graphics.pause();
            }
        });
        gvrAudioEngine.pause();

        app.input.onPause();

//        if (isFinishing()) {
//            graphics.clearManagedCaches();
//            graphics.destroy();
//        }

        Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        gvrAudioEngine.resume();

        Gdx.app = app;
        Gdx.input = app.input;
        Gdx.audio = app.getAudio();
        Gdx.files = app.files;
        Gdx.graphics = app.graphics;
        Gdx.gl = app.graphics.getGL20();
        Gdx.gl20 = app.graphics.getGL20();
        Gdx.net = app.getNet();

        GdxVr.app = app;
        GdxVr.input = app.input;
        GdxVr.audio = app.getAudio();
        GdxVr.files = app.files;
        GdxVr.graphics = app.graphics;
        GdxVr.gl = app.graphics.getGL20();
        GdxVr.gl20 = app.graphics.getGL20();
        GdxVr.net = app.getNet();

        app.input.onResume();


        if (!firstResume) {
            getGvrView().queueEvent(new Runnable() {
                @Override
                public void run() {
                    app.graphics.resume();
                }
            });
        } else
            firstResume = false;

//        this.isWaitingForAudio = true;
//        if (this.wasFocusChanged == 1 || this.wasFocusChanged == -1) {
//            // TODO: 10/11/2016 fix audio 
////            this.audio.resume();
//            this.isWaitingForAudio = false;
//        }
    }

    @Override
    protected void onDestroy() {
        final GvrView gvrView = getGvrView();
        if (gvrView != null && app.graphics != null) {
            gvrView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    app.graphics.destroy();
                }
            });
        }
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
        controllerManager.start();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        controllerManager.stop();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == 24 || keyCode == 25;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return keyCode == 24 || keyCode == 25;
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
//        boolean keyboardAvailable = false;
//        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
//            keyboardAvailable = true;
        // TODO: 10/11/2016 fix input 
//        input.keyboardAvailable = keyboardAvailable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        app.onActivityResult(requestCode, resultCode, data);
    }

    public Application getVrApp() {
        return app;
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
        if (!GdxVr.input.isControllerConnected()) {
            GdxVr.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GdxVr.input.onCardboardTrigger();
                    GdxVr.app.getVrApplicationAdapter().onCardboardTrigger();
                }
            });
        }
    }

    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener, Runnable {

        // The status of the overall controller API. This is primarily used for error handling since
        // it rarely changes.
        private String apiStatus;

        // The state of a specific Controller connection.
        private int connectionState = Controller.ConnectionStates.CONNECTED;

        @Override
        public void onApiStatusChanged(int state) {
            apiStatus = ControllerManager.ApiStatus.toString(state);
        }

        @Override
        public void onConnectionStateChanged(int state) {
            connectionState = state;
//            gvrView.queueEvent(this);
        }

        @Override
        public void onRecentered() {
            // In a real GVR application, this would have implicitly called recenterHeadTracker().
            // Most apps don't care about this, but apps that want to implement custom behavior when a
            // recentering occurs should use this callback.
        }

        @Override
        public void onUpdate() {
//            gvrView.queueEvent(this);
        }

        // Update the various TextViews in the UI thread.
        @Override
        public void run() {
//            app.input.onDaydreamControllerUpdate(controller, connectionState);
        }
    }
}
