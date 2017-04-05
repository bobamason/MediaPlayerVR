package org.masonapps.libgdxgooglevr.vr;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidClipboard;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.AndroidNet;
import com.badlogic.gdx.backends.android.AndroidPreferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SnapshotArray;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import org.masonapps.libgdxgooglevr.GdxVr;

import java.lang.reflect.Method;

/**
 * Created by Bob on 10/9/2016.
 */

public class VrActivity extends GvrActivity implements AndroidApplicationBase {
    static {
        GdxNativesLoader.load();
    }

    protected final Array<Runnable> runnables = new Array<Runnable>();
    protected final Array<Runnable> executedRunnables = new Array<Runnable>();
    protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<>();
    private final Array<AndroidEventListener> androidEventListeners = new Array<AndroidEventListener>();
    public Handler handler;
    protected VrGraphics graphics;
    protected VrAndroidInput input;
    //    protected AndroidAudio audio;
    protected AndroidFiles files;
    protected AndroidNet net;
    protected VrApplicationAdapter vrApplicationAdapter;
    protected boolean firstResume = true;
    protected int logLevel = LOG_INFO;
    protected ControllerManager controllerManager;
    protected Controller controller;
    AndroidClipboard clipboard;
//    private int wasFocusChanged = -1;
//    private boolean isWaitingForAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GvrView gvrView = new GvrView(this);
        setContentView(gvrView);
        setGvrView(gvrView);
        final EventListener listener = new EventListener();
        controllerManager = new ControllerManager(this, listener);
        controller = controllerManager.getController();
        controller.setEventListener(listener);
    }

    public void initialize(VrApplicationAdapter adapter) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(adapter, config);
    }

    public void initialize(VrApplicationAdapter adapter, AndroidApplicationConfiguration config) {
        init(adapter, config);
    }

    private void init(VrApplicationAdapter adapter, AndroidApplicationConfiguration config) {
        if (this.getVersion() < MINIMUM_SDK) {
            throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
        }

        graphics = new VrGraphics(this, getGvrView(), config);
        input = VrAndroidInput.newInstance(this);
        input.setController(controller);
//        audio = new AndroidAudio(this, config);
        this.getFilesDir();
        files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        net = new AndroidNet(this);
        this.vrApplicationAdapter = adapter;
        this.handler = new Handler();

        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();
    }

//    protected void hideStatusBar(boolean hide) {
//        if (!hide || getVersion() < 11) return;
//
//        View rootView = getWindow().getDecorView();
//
//        try {
//            Method m = View.class.getMethod("setSystemUiVisibility", int.class);
//            if (getVersion() <= 13) m.invoke(rootView, 0x0);
//            m.invoke(rootView, 0x1);
//        } catch (Exception e) {
//            log("AndroidApplication", "Can't hide status bar", e);
//        }
//    }

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

    @TargetApi(19)
    @Override
    public void useImmersiveMode(boolean use) {
        if (!use || getVersion() < Build.VERSION_CODES.KITKAT) return;

        View view = getWindow().getDecorView();
        try {
            Method m = View.class.getMethod("setSystemUiVisibility", int.class);
            int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            m.invoke(view, code);
        } catch (Exception e) {
            log("AndroidApplication", "Can't set immersive mode", e);
        }
    }

    @Override
    protected void onPause() {
        // calls to setContinuousRendering(false) from other thread (ex: GLThread)
        // will be ignored at this point...
        getGvrView().queueEvent(new Runnable() {
            @Override
            public void run() {
                graphics.pause();
            }
        });

        input.onPause();

//        if (isFinishing()) {
//            graphics.clearManagedCaches();
//            graphics.destroy();
//        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        assert getGvrView() != null;

        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();

        assert GdxVr.app != null;

        input.onResume();


        if (!firstResume) {
            getGvrView().queueEvent(new Runnable() {
                @Override
                public void run() {
                    graphics.resume();
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
        if (gvrView != null && graphics != null) {
            gvrView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    graphics.destroy();
                }
            });
            gvrView.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        controllerManager.start();
    }

    @Override
    protected void onStop() {
        controllerManager.stop();
        super.onStop();
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return vrApplicationAdapter;
    }

    public VrApplicationAdapter getVrApplicationAdapter() {
        return vrApplicationAdapter;
    }

    @Override
    public Audio getAudio() {
        // TODO: 10/11/2016 fix audio 
        return null;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public AndroidInput getInput() {
        return input;
    }

    @Override
    public SnapshotArray<LifecycleListener> getLifecycleListeners() {
        return lifecycleListeners;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    @Override
    public int getVersion() {
        return Build.VERSION.SDK_INT;
    }

    @Override
    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

    @Override
    public Preferences getPreferences(String name) {
        return new AndroidPreferences(getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    @Override
    public Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = new AndroidClipboard(this);
        }
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
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
    public void exit() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                VrActivity.this.finish();
            }
        });
    }

    @Override
    public void debug(String tag, String message) {
        if (logLevel >= LOG_DEBUG) {
            Log.d(tag, message);
        }
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_DEBUG) {
            Log.d(tag, message, exception);
        }
    }

    @Override
    public void log(String tag, String message) {
        if (logLevel >= LOG_INFO) Log.i(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_INFO) Log.i(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        if (logLevel >= LOG_ERROR) Log.e(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_ERROR) Log.e(tag, message, exception);
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // forward events to our listeners if there are any installed
        synchronized (androidEventListeners) {
            for (int i = 0; i < androidEventListeners.size; i++) {
                androidEventListeners.get(i).onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Adds an event vrApplicationAdapter for Android specific event such as onActivityResult(...).
     */
    public void addAndroidEventListener(AndroidEventListener listener) {
        synchronized (androidEventListeners) {
            androidEventListeners.add(listener);
        }
    }

    /**
     * Removes an event vrApplicationAdapter for Android specific event such as onActivityResult(...).
     */
    public void removeAndroidEventListener(AndroidEventListener listener) {
        synchronized (androidEventListeners) {
            androidEventListeners.removeValue(listener, true);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Array<Runnable> getRunnables() {
        return runnables;
    }

    @Override
    public Array<Runnable> getExecutedRunnables() {
        return executedRunnables;
    }

    @Override
    public Window getApplicationWindow() {
        return this.getWindow();
    }

    @Override
    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public void onCardboardTrigger() {
        getGvrView().queueEvent(new Runnable() {
            @Override
            public void run() {
                input.onCardboardTrigger();
                VrActivity.this.vrApplicationAdapter.onCardboardTrigger();
            }
        });
    }

    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener, Runnable {

        // The status of the overall controller API. This is primarily used for error handling since
        // it rarely changes.
        private String apiStatus;

        // The state of a specific Controller connection.
        private int connectionState = Controller.ConnectionStates.DISCONNECTED;

        @Override
        public void onApiStatusChanged(int state) {
            apiStatus = ControllerManager.ApiStatus.toString(state);
        }

        @Override
        public void onConnectionStateChanged(int state) {
            connectionState = state;
            getGvrView().queueEvent(this);
        }

        @Override
        public void onRecentered() {
            // In a real GVR application, this would have implicitly called recenterHeadTracker().
            // Most apps don't care about this, but apps that want to implement custom behavior when a
            // recentering occurs should use this callback.
        }

        @Override
        public void onUpdate() {
            getGvrView().queueEvent(this);
        }

        // Update the various TextViews in the UI thread.
        @Override
        public void run() {
            controller.update();
            input.onDaydreamControllerUpdate(controller, connectionState);
            vrApplicationAdapter.onDaydreamControllerUpdate(controller, connectionState);
        }
    }
}
