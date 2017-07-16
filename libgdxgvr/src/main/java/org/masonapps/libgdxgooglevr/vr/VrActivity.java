package org.masonapps.libgdxgooglevr.vr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidClipboard;
import com.badlogic.gdx.backends.android.AndroidEventListener;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.backends.android.AndroidNet;
import com.badlogic.gdx.backends.android.AndroidPreferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SnapshotArray;
import com.google.vr.cardboard.FullscreenMode;
import com.google.vr.ndk.base.GvrApi;
import com.google.vr.ndk.base.GvrLayout;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;
import com.google.vrtoolkit.cardboard.ScreenOnFlagHelper;

import org.masonapps.libgdxgooglevr.GdxVr;

import java.lang.ref.WeakReference;

/**
 * Created by Bob on 10/9/2016.
 * based on AndroidApplication originally written by mzechner
 */

public class VrActivity extends Activity {
    static {
        GdxNativesLoader.load();
    }

    private final ScreenOnFlagHelper screenOnFlagHelper = new ScreenOnFlagHelper(this);
    protected boolean firstResume = true;
    protected ControllerManager controllerManager;
    protected Controller controller;
    private GvrLayout gvrLayout;
    private GLSurfaceView surfaceView;
    private FullscreenMode fullscreenMode;
    private VrApplication app;
//    private int wasFocusChanged = -1;
//    private boolean isWaitingForAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.fullscreenMode = new FullscreenMode(this.getWindow());
        this.screenOnFlagHelper.setScreenAlwaysOn(true);
        this.app = new VrApplication(new WeakReference<>(this));

        AndroidCompat.setVrModeEnabled(this, true);
        AndroidCompat.setSustainedPerformanceMode(this, true);
        
        gvrLayout = new GvrLayout(this);
        surfaceView = new GLSurfaceView(this);

        gvrLayout.setPresentationView(surfaceView);
        initGvrLayout(gvrLayout);

//        if (getGvrLayout().setAsyncReprojectionEnabled(true)) {
////            // Async reprojection decouples the app framerate from the display framerate,
////            // allowing immersive interaction even at the throttled clockrates set by
////            // sustained performance mode.
//            Log.d(VrActivity.class.getSimpleName(), "Async Reprojection Enabled");
//        }
        setContentView(gvrLayout);
        final EventListener listener = new EventListener();
        controllerManager = new ControllerManager(this, listener);
        controller = controllerManager.getController();
        controller.setEventListener(listener);
    }

    protected void initGvrLayout(GvrLayout layout) {
        gvrLayout.setKeepScreenOn(true);
    }

    public void initialize(VrApplicationAdapter adapter) {

        app.graphics = new VrGraphics(app, new WeakReference<>(getSurfaceView()), gvrLayout.getGvrApi());
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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.fullscreenMode.onWindowFocusChanged(hasFocus);
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
        getSurfaceView().queueEvent(new Runnable() {
            @Override
            public void run() {
                app.graphics.pause();
            }
        });
        gvrLayout.onPause();
        this.screenOnFlagHelper.stop();

        app.input.onPause();

//        if (isFinishing()) {
//            graphics.clearManagedCaches();
//            graphics.destroy();
//        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gvrLayout.onResume();


        this.fullscreenMode.goFullscreen();
        this.screenOnFlagHelper.start();

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
            getSurfaceView().queueEvent(new Runnable() {
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
        final GLSurfaceView surfaceView = getSurfaceView();
        if (surfaceView != null && app.graphics != null) {
            app.graphics.shutdown();
            surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    app.graphics.destroy();
                }
            });
        }
        if (gvrLayout != null) {
            gvrLayout.shutdown();
            gvrLayout = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gvrLayout.onBackPressed();
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

    public GvrLayout getGvrLayout() {
        return gvrLayout;
    }

    public GLSurfaceView getSurfaceView() {
        return surfaceView;
    }

    public Application getVrApp() {
        return app;
    }

    /**
     * Moved AndroidApplicationBase implementation to separate class with a WeakReference to the Activity to get rid of static references to the Context in Gdx.app and GdxVr.app
     */
    public static class VrApplication implements AndroidApplicationBase {

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
        protected int logLevel = LOG_INFO;
        protected AndroidClipboard clipboard;

        private WeakReference<VrActivity> activityRef;

        private VrApplication(WeakReference<VrActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        @Nullable
        public Context getContext() {
            return activityRef.get();
        }

        @Override
        public void runOnUiThread(Runnable runnable) {
            final VrActivity activity = activityRef.get();
            if (activity != null)
                activity.runOnUiThread(runnable);
        }

        @Override
        public void startActivity(Intent intent) {
            final VrActivity activity = activityRef.get();
            if (activity != null)
                activity.startActivity(intent);
        }

        @Override
        public ApplicationListener getApplicationListener() {
            return vrApplicationAdapter;
        }

        public VrApplicationAdapter getVrApplicationAdapter() {
            return vrApplicationAdapter;
        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            synchronized (androidEventListeners) {
                for (int i = 0; i < androidEventListeners.size; i++) {
                    androidEventListeners.get(i).onActivityResult(requestCode, resultCode, data);
                }
            }
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
            throw new UnsupportedOperationException("method not supported in " + VrAndroidInput.class.getSimpleName());
        }

        public VrAndroidInput getVrInput() {
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
        public Array<Runnable> getRunnables() {
            return runnables;
        }

        @Override
        public Array<Runnable> getExecutedRunnables() {
            return executedRunnables;
        }

        @Override
        @Nullable
        public Window getApplicationWindow() {
            final VrActivity activity = activityRef.get();
            return activity == null ? null : activity.getWindow();
        }

        @Override
        public Handler getHandler() {
            return this.handler;
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
            final VrActivity activity = activityRef.get();
            if (activity != null)
                return new AndroidPreferences(activity.getSharedPreferences(name, Context.MODE_PRIVATE));
            else
                return null;
        }

        @Override
        public Clipboard getClipboard() {
            if (clipboard == null) {
                final VrActivity activity = activityRef.get();
                if (activity != null)
                    clipboard = new AndroidClipboard(activity);
            }
            return clipboard;
        }

        @Override
        public void postRunnable(Runnable runnable) {
            synchronized (runnables) {
                runnables.add(runnable);
            }
        }

        @Override
        public void exit() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final VrActivity activity = activityRef.get();
                    if (activity != null)
                        activity.finish();
                    else
                        System.exit(0);
                }
            });
        }

        @Override
        public ApplicationType getType() {
            return ApplicationType.Android;
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
        @Nullable
        public WindowManager getWindowManager() {
            final VrActivity activity = activityRef.get();
            return activity == null ? null : activity.getWindowManager();
        }

        @Override
        public void useImmersiveMode(boolean b) {

        }

        @Nullable
        public GvrLayout getGvrLayout() {
            final VrActivity activity = activityRef.get();
            return activity == null ? null : activity.getGvrLayout();
        }

        @Nullable
        public GvrApi getGvrApi() {
            final VrActivity activity = activityRef.get();
            return activity == null ? null : activity.getGvrLayout().getGvrApi();
        }
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
            getSurfaceView().queueEvent(this);
        }

        @Override
        public void onRecentered() {
            // In a real GVR application, this would have implicitly called recenterHeadTracker().
            // Most apps don't care about this, but apps that want to implement custom behavior when a
            // recentering occurs should use this callback.
        }

        @Override
        public void onUpdate() {
            getSurfaceView().queueEvent(this);
        }

        // Update the various TextViews in the UI thread.
        @Override
        public void run() {
            controller.update();
            app.input.onDaydreamControllerUpdate(controller, connectionState);
            app.vrApplicationAdapter.onDaydreamControllerUpdate(controller, connectionState);
        }
    }
}
