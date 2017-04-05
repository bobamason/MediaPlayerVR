package org.masonapps.libgdxgooglevr.vr;

import android.support.annotation.CallSuper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidApplicationBase;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGL20;
import com.badlogic.gdx.backends.android.AndroidGL30;
import com.badlogic.gdx.backends.android.surfaceview.GdxEglConfigChooser;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.masonapps.libgdxgooglevr.GdxVr;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Bob on 10/9/2016.
 */

public class VrGraphics implements Graphics, GvrView.Renderer {

    private static final String LOG_TAG = VrGraphics.class.getSimpleName();

    private static final int OFFSET_RIGHT = 0;
    private static final int OFFSET_UP = OFFSET_RIGHT + 3;
    private static final int OFFSET_FORWARD = OFFSET_UP + 3;
    private static final int OFFSET_TRANSLATION = OFFSET_FORWARD + 3;
    private static final int OFFSET_EULER = OFFSET_TRANSLATION + 3;
    private static final int OFFSET_QUATERNION = OFFSET_EULER + 3;
    protected final GvrView view;
    protected final AndroidApplicationConfiguration config;
    final Object synch = new Object();
    private final float[] array = new float[3 * 5 + 4 + 16];
    private final Vector3 forward = new Vector3();
    private final Vector3 up = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 headTranslation = new Vector3();
    private final Quaternion headQuaternion = new Quaternion();
    private final Matrix4 headMatrix = new Matrix4();
    protected int width;
    protected int height;
    protected AndroidApplicationBase app;
    protected GL20 gl20;
    protected GL30 gl30;
    protected EGLContext eglContext;
    protected GLVersion glVersion;
    protected String extensions;
    protected long lastFrameTime = System.nanoTime();
    protected float deltaTime = 0;
    protected long frameStart = System.nanoTime();
    protected long frameId = -1;
    protected int frames = 0;
    protected int fps;
    protected WindowedMean mean = new WindowedMean(5);
    protected volatile boolean created = false;
    protected volatile boolean running = false;
    protected volatile boolean pause = false;
    protected volatile boolean resume = false;
    protected volatile boolean destroy = false;
    int[] value = new int[1];
    private float ppiX = 0;
    private float ppiY = 0;
    private float ppcX = 0;
    private float ppcY = 0;
    private float density = 1;
    private BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);
    private boolean isContinuous = true;

    public VrGraphics(AndroidApplicationBase application, GvrView view, AndroidApplicationConfiguration config) {
        this(application, view, config, true);
    }

    public VrGraphics(AndroidApplicationBase application, GvrView view, AndroidApplicationConfiguration config, boolean focusableView) {
        this.config = config;
        this.app = application;
        this.view = view;
        this.view.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        this.view.setRenderer(this);
        if (focusableView) {
            this.view.setFocusable(true);
            this.view.setFocusableInTouchMode(true);
        }
    }

    private void updatePpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ppiX = metrics.xdpi;
        ppiY = metrics.ydpi;
        ppcX = metrics.xdpi / 2.54f;
        ppcY = metrics.ydpi / 2.54f;
        density = metrics.density;
    }

    protected boolean checkGL20() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(display, version);

        int EGL_OPENGL_ES2_BIT = 4;
        int[] configAttribs = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

        EGLConfig[] configs = new EGLConfig[10];
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
        egl.eglTerminate(display);
        return num_config[0] > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GL20 getGL20() {
        return gl20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getBackBufferWidth() {
        return width;
    }

    @Override
    public int getBackBufferHeight() {
        return height;
    }

    private void setupGL() {
        if (config.useGL30 && glVersion.getMajorVersion() > 2) {
            if (gl30 != null) return;
            gl20 = gl30 = new AndroidGL30();

            Gdx.gl = gl30;
            Gdx.gl20 = gl30;
            Gdx.gl30 = gl30;
        } else {
            if (gl20 != null) return;
            gl20 = new AndroidGL20();

            Gdx.gl = gl20;
            Gdx.gl20 = gl20;
        }
        String versionString = Gdx.gl.glGetString(GL10.GL_VERSION);
        String vendorString = Gdx.gl.glGetString(GL10.GL_VENDOR);
        String rendererString = Gdx.gl.glGetString(GL10.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.Android, versionString, vendorString, rendererString);
    }

    private void logConfig(EGLConfig config) {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int r = getAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
        int g = getAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
        int b = getAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
        int a = getAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
        int d = getAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
        int s = getAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
        int samples = Math.max(getAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0),
                getAttrib(egl, display, config, GdxEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0));
        boolean coverageSample = getAttrib(egl, display, config, GdxEglConfigChooser.EGL_COVERAGE_SAMPLES_NV, 0) != 0;

        Gdx.app.log(LOG_TAG, "framebuffer: (" + r + ", " + g + ", " + b + ", " + a + ")");
        Gdx.app.log(LOG_TAG, "depthbuffer: (" + d + ")");
        Gdx.app.log(LOG_TAG, "stencilbuffer: (" + s + ")");
        Gdx.app.log(LOG_TAG, "samples: (" + samples + ")");
        Gdx.app.log(LOG_TAG, "coverage sampling: (" + coverageSample + ")");

        bufferFormat = new BufferFormat(r, g, b, a, d, s, samples, coverageSample);
    }

    private int getAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue) {
        if (egl.eglGetConfigAttrib(display, config, attrib, value)) {
            return value[0];
        }
        return defValue;
    }

    void resume() {
        synchronized (synch) {
            running = true;
            resume = true;
        }
        final Array<LifecycleListener> listeners = app.getLifecycleListeners();
        synchronized (listeners) {
            for (LifecycleListener listener : listeners) {
                listener.resume();
            }
        }
        app.getApplicationListener().resume();
        Gdx.app.log(LOG_TAG, "resumed");
    }

    void pause() {
        synchronized (synch) {
            if (!running) return;
            running = false;
            pause = true;
        }
        final Array<LifecycleListener> listeners = app.getLifecycleListeners();
        synchronized (listeners) {
            for (LifecycleListener listener : listeners) {
                listener.pause();
            }
        }
        app.getApplicationListener().pause();
        Gdx.app.log(LOG_TAG, "paused");
//        synchronized (synch) {
//            if (!running) return;
//            running = false;
//            pause = true;
//            while (pause) {
//                try {
//                    // TODO: fix deadlock race condition with quick resume/pause.
//                    // Temporary workaround:
//                    // Android ANR time is 5 seconds, so wait up to 4 seconds before assuming
//                    // deadlock and killing process. This can easily be triggered by opening the
//                    // Recent Apps list and then double-tapping the Recent Apps button with
//                    // ~500ms between taps.
//                    synch.wait(4000);
//                    if (pause) {
//                        // pause will never go false if onDrawFrame is never called by the GLThread
//                        // when entering this method, we MUST enforce continuous rendering
//                        Gdx.app.error(LOG_TAG, "waiting for pause synchronization took too long; assuming deadlock and killing");
//                        android.os.Process.killProcess(android.os.Process.myPid());
//                    }
//                } catch (InterruptedException ignored) {
//                    Gdx.app.log(LOG_TAG, "waiting for pause synchronization failed!");
//                }
//            }
//        }
    }

    void destroy() {
        Array<LifecycleListener> listeners = app.getLifecycleListeners();
        synchronized (listeners) {
            for (LifecycleListener listener : listeners) {
                listener.dispose();
            }
        }
        app.getApplicationListener().dispose();
        clearManagedCaches();
    }

    @Override
    public long getFrameId() {
        return frameId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getDeltaTime() {
        return mean.getMean() == 0 ? deltaTime : mean.getMean();
    }

    @Override
    public float getRawDeltaTime() {
        return deltaTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphicsType getType() {
        return GraphicsType.AndroidGL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GLVersion getGLVersion() {
        return glVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFramesPerSecond() {
        return fps;
    }

    public void clearManagedCaches() {
        Mesh.clearAllMeshes(app);
        Texture.clearAllTextures(app);
        Cubemap.clearAllCubemaps(app);
        TextureArray.clearAllTextureArrays(app);
        ShaderProgram.clearAllShaderPrograms(app);
        FrameBuffer.clearAllFrameBuffers(app);

        logManagedCachesStatus();
    }

    protected void logManagedCachesStatus() {
        Gdx.app.log(LOG_TAG, Mesh.getManagedStatus());
        Gdx.app.log(LOG_TAG, Texture.getManagedStatus());
        Gdx.app.log(LOG_TAG, Cubemap.getManagedStatus());
        Gdx.app.log(LOG_TAG, ShaderProgram.getManagedStatus());
        Gdx.app.log(LOG_TAG, FrameBuffer.getManagedStatus());
    }

    public View getView() {
        return view;
    }

    @Override
    public float getPpiX() {
        return ppiX;
    }

    @Override
    public float getPpiY() {
        return ppiY;
    }

    @Override
    public float getPpcX() {
        return ppcX;
    }

    @Override
    public float getPpcY() {
        return ppcY;
    }

    @Override
    public float getDensity() {
        return density;
    }

    @Override
    public boolean supportsDisplayModeChange() {
        return false;
    }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode) {
        return false;
    }

    @Override
    public Monitor getPrimaryMonitor() {
        return new AndroidMonitor(0, 0, "Primary Monitor");
    }

    @Override
    public Monitor getMonitor() {
        return getPrimaryMonitor();
    }

    @Override
    public Monitor[] getMonitors() {
        return new Monitor[]{getPrimaryMonitor()};
    }

    @Override
    public DisplayMode[] getDisplayModes(Monitor monitor) {
        return getDisplayModes();
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor) {
        return getDisplayMode();
    }

    @Override
    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[]{getDisplayMode()};
    }

    @Override
    public boolean setWindowedMode(int width, int height) {
        return false;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void setUndecorated(boolean undecorated) {
        final int mask = (undecorated) ? 1 : 0;
        app.getApplicationWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, mask);
    }

    @Override
    public void setResizable(boolean resizable) {

    }

    @Override
    public DisplayMode getDisplayMode() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new AndroidDisplayMode(metrics.widthPixels, metrics.heightPixels, 0, 0);
    }

    @Override
    public BufferFormat getBufferFormat() {
        return bufferFormat;
    }

    @Override
    public void setVSync(boolean vsync) {
    }

    @Override
    public boolean supportsExtension(String extension) {
        if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
    public boolean isContinuousRendering() {
        return true;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {
        // not supported
    }

    @Override
    public void requestRendering() {
        // not supported
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public boolean isGL30Available() {
        return gl30 != null;
    }

    @Override
    public GL30 getGL30() {
        return gl30;
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
        return null;
    }

    @Override
    public void setCursor(Cursor cursor) {
    }

    @Override
    public void setSystemCursor(Cursor.SystemCursor systemCursor) {
    }

    @Override
    @CallSuper
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        long time = System.nanoTime();
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;

        // After pause deltaTime can have somewhat huge value that destabilizes the mean, so let's cut it off
        if (!resume) {
            mean.addValue(deltaTime);
        } else {
            deltaTime = 0;
        }

        boolean lrunning = false;
        boolean lpause = false;
        boolean ldestroy = false;
        boolean lresume = false;

        synchronized (synch) {
            lrunning = running;
            lpause = pause;
            ldestroy = destroy;
            lresume = resume;

            if (resume) {
                resume = false;
            }

            if (pause) {
                pause = false;
                synch.notifyAll();
            }

            if (destroy) {
                destroy = false;
                synch.notifyAll();
            }
        }

//        if (lresume) {
//        }

        if (lrunning) {
            synchronized (app.getRunnables()) {
                app.getExecutedRunnables().clear();
                app.getExecutedRunnables().addAll(app.getRunnables());
                app.getRunnables().clear();
            }

            for (int i = 0; i < app.getExecutedRunnables().size; i++) {
                try {
                    app.getExecutedRunnables().get(i).run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            handleHeadTransform(headTransform);
            final VrAndroidInput input = GdxVr.input;
            if (!input.isControllerConnected())
                input.updateInputRay(null);
            input.processEvents();
            ((VrApplicationAdapter) GdxVr.app.getApplicationListener()).onDrawFrame(headTransform, leftEye, rightEye);
            frameId++;
        }

//        if (lpause) {
//        }

//        if (ldestroy) {
//        }

        if (time - frameStart > 1000000000) {
            fps = frames;
            frames = 0;
            frameStart = time;
        }
        frames++;
    }

    private void handleHeadTransform(HeadTransform headTransform) {
        headTransform.getForwardVector(array, OFFSET_FORWARD);
        forward.set(array[OFFSET_FORWARD], array[OFFSET_FORWARD + 1], array[OFFSET_FORWARD + 2]);
        headTransform.getUpVector(array, OFFSET_UP);
        up.set(array[OFFSET_UP], array[OFFSET_UP + 1], array[OFFSET_UP + 2]);
        headTransform.getRightVector(array, OFFSET_RIGHT);
        right.set(array[OFFSET_RIGHT], array[OFFSET_RIGHT + 1], array[OFFSET_RIGHT + 2]);
        headTransform.getTranslation(array, OFFSET_TRANSLATION);
        headTranslation.set(array[OFFSET_TRANSLATION], array[OFFSET_TRANSLATION + 1], array[OFFSET_TRANSLATION + 2]);
        headTransform.getQuaternion(array, OFFSET_QUATERNION);
        headQuaternion.set(array[OFFSET_QUATERNION], array[OFFSET_QUATERNION + 1], array[OFFSET_QUATERNION + 2], array[OFFSET_QUATERNION + 3]);
        headMatrix.set(headTransform.getHeadView());
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        ((VrApplicationAdapter) GdxVr.app.getApplicationListener()).onFinishFrame(viewport);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;
        updatePpi();
        if (!created) {
            app.getApplicationListener().create();
            created = true;
            synchronized (this) {
                running = true;
            }
        }
//        app.getApplicationListener().resize(width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        eglContext = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        setupGL();
        logConfig(eglConfig);
        updatePpi();

        Mesh.invalidateAllMeshes(app);
        Texture.invalidateAllTextures(app);
        Cubemap.invalidateAllCubemaps(app);
        ShaderProgram.invalidateAllShaderPrograms(app);
        FrameBuffer.invalidateAllFrameBuffers(app);

        logManagedCachesStatus();

        Display display = app.getWindowManager().getDefaultDisplay();
        this.width = display.getWidth();
        this.height = display.getHeight();
        this.mean = new WindowedMean(5);
        this.lastFrameTime = System.nanoTime();
    }

    @Override
    public void onRendererShutdown() {
        Gdx.app.log(LOG_TAG, "onRendererShutdown");
    }

    public Vector3 getForward() {
        return forward;
    }

    public Vector3 getUp() {
        return up;
    }

    public Vector3 getRight() {
        return right;
    }

    public Vector3 getHeadTranslation() {
        return headTranslation;
    }

    public Matrix4 getHeadMatrix() {
        return headMatrix;
    }

    public Quaternion getHeadQuaternion() {
        return headQuaternion;
    }

    private class AndroidDisplayMode extends DisplayMode {
        protected AndroidDisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
            super(width, height, refreshRate, bitsPerPixel);
        }
    }

    private class AndroidMonitor extends Monitor {

        public AndroidMonitor(int virtualX, int virtualY, String name) {
            super(virtualX, virtualY, name);
        }

    }
}
