package net.masonapps.mediaplayervr.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.base.Eye;

/**
 * Created by Bob on 12/21/2016.
 */

public abstract class VrVideoPlayer implements Disposable, SurfaceTexture.OnFrameAvailableListener {

    public static final String TAG = VrVideoPlayer.class.getSimpleName();
    protected static Handler handler;
    protected final ModelInstance rectModelInstance;
    //    protected final ModelInstance halfSphereModelInstance;
    protected final ModelInstance sphereModelInstance;
    protected final ModelInstance cylinderModelInstance;
    protected final Object lock = new Object();
    protected ModelInstance modelInstance;
    protected Array<Disposable> disposables = new Array<>();
    protected VideoShader shader;
    protected int[] textures = new int[1];
    protected SurfaceTexture videoTexture;
    protected boolean prepared = false;
    protected boolean frameAvailable = false;
    protected boolean isStereoscopic = false;
    protected boolean isHorizontalSplit = false;
    @Nullable
    protected VideoSizeListener sizeListener;
    @Nullable
    protected CompletionListener completionListener;
    @Nullable
    protected ErrorListener errorListener;
    protected DisplayMode displayMode;
    protected float aspectRatio = 1f;
    //    protected float targetAspectRatio = 1f;
    protected float modelSize = 10f;
    protected Context context;
    private int width;
    private int height;
    private Vector2 stretch = new Vector2();
    private Rectangle srcRect = new Rectangle(0, 0, 1, 1);
    private Rectangle dstRect = new Rectangle(0, 0, 1, 1);
    private Quaternion headRotation = new Quaternion();
    private Quaternion invHeadRotation = new Quaternion();
    private float zoom = 1f;
    private boolean useFishEyeProjection = false;
    private boolean useCylinder = false;

    public VrVideoPlayer(Context context, Uri uri, int width, int height, Model rectModel, Model sphereModel, Model cylinderModel) {
        this(context, uri, width, height, DisplayMode.Mono, rectModel, sphereModel, cylinderModel);
    }

    public VrVideoPlayer(Context context, Uri uri, int width, int height, DisplayMode displayMode, Model rectModel, Model sphereModel, Model cylinderModel) {
        this.context = context;
        this.width = width;
        this.height = height;
        shader = new VideoShader();
        rectModelInstance = new ModelInstance(rectModel);
        sphereModelInstance = new ModelInstance(sphereModel);
        cylinderModelInstance = new ModelInstance(cylinderModel);
        setupRenderTexture();
        initializeMediaPlayer();
        setDisplayMode(displayMode);
        play(uri);
    }

    private void initializeMediaPlayer() {
        if (handler == null)
            handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    createMediaPlayer();
                    lock.notify();
                }
            }
        });
    }

    protected abstract void createMediaPlayer();

    public abstract boolean play(final Uri uri);

    protected void updateAspectRatio() {
        if (!prepared)
            return;
        float w;
        float h;
        if (isStereoscopic) {
            if (isHorizontalSplit) {
                w = width / 2f;
                h = height;
            } else {
                w = width;
                h = height / 2f;
            }
        } else {
            w = width;
            h = height;
        }
        aspectRatio = w / h;

        if (useFlatRectangle()) {
            srcRect.set(0, 0, 1, 1);
            dstRect.set(0, 0, 1, 1);
        } else if (is180Video() && !useFishEyeProjection) {
            srcRect.set(0, 0, 1, 1);
            dstRect.set(0.25f, 0f, 0.5f, 1f);
//            dstRect.set(0.25f - stretch.x * 0.5f, -stretch.y * 0.5f, 0.5f + stretch.x, 1f + stretch.y);
        } else {
            srcRect.set(0, 0, 1, 1);
            dstRect.set(0, 0, 1, 1);
//            dstRect.set(-stretch.x * 0.5f, -stretch.y * 0.5f, 1f + stretch.x, 1f + stretch.y);
        }
    }

    public void update() {
        if (!prepared)
            return;
        synchronized (this) {
            if (frameAvailable) {
                videoTexture.updateTexImage();
                frameAvailable = false;
            }
        }
    }

    public void render(ModelBatch batch, int eyeType, Matrix4 transform) {
        if (!prepared)
            return;

        shader.setUseFishEye(useFishEyeProjection);

        if (useFlatRectangle())
            mapDistortModel();
        else
            mapDistortTextureCoordinates();

        modelInstance.transform.mulLeft(transform);

        if (isStereoscopic) {
            switch (eyeType) {
                case Eye.Type.LEFT:
                    if (isHorizontalSplit) {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width * 0.5f, srcRect.height);
                    } else {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width, srcRect.height * 0.5f);
                    }
                    shader.setDstRect(dstRect.x, dstRect.y, dstRect.width, dstRect.height);
                    break;
                case Eye.Type.RIGHT:
                    if (isHorizontalSplit) {
                        shader.setSrcRect(srcRect.x + 0.5f, srcRect.y, srcRect.width * 0.5f, srcRect.height);
                    } else {
                        shader.setSrcRect(srcRect.x, srcRect.y + 0.5f, srcRect.width, srcRect.height * 0.5f);
                    }
                    shader.setDstRect(dstRect.x, dstRect.y, dstRect.width, dstRect.height);
                    break;
                default:
                    if (isHorizontalSplit) {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width * 0.5f, srcRect.height);
                    } else {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width, srcRect.height * 0.5f);
                    }
                    shader.setDstRect(dstRect.x, dstRect.y, dstRect.width, dstRect.height);
                    break;
            }
        } else {
            shader.getSrcRect().set(srcRect);
            shader.getDstRect().set(dstRect);
        }

        batch.render(modelInstance, shader);
    }

    protected void mapDistortTextureCoordinates() {
        modelInstance.transform.idt().rotate(Vector3.X, stretch.y * -90f).scale(modelSize, modelSize, modelSize);
//        modelInstance.transform.idt().scale(modelSize + stretch.x * modelSize, modelSize + stretch.y * modelSize, modelSize);
//        modelInstance.transform.idt().rotate(invHeadRotation).scale(modelSize, modelSize, modelSize * zoom).rotate(headRotation);
    }

    protected void mapDistortModel() {
        if (aspectRatio <= 1f) {
            modelInstance.transform.idt().translate(0, 0, -4).scale(aspectRatio * modelSize + stretch.x * modelSize, modelSize + stretch.y * modelSize, modelSize);
        } else {
            modelInstance.transform.idt().translate(0, 0, -4).scale(modelSize + stretch.x * modelSize, modelSize / aspectRatio + stretch.y * modelSize, modelSize);
        }
    }

    public abstract void stop();

    protected void setupRenderTexture() {
        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, textures, 0);
        final int textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        shader.setTextureId(textureId);
        Gdx.gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
        Gdx.gl.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);

        videoTexture = new SurfaceTexture(textureId);
        videoTexture.setOnFrameAvailableListener(this);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            frameAvailable = true;
        }
    }

    public abstract void pause();

    public abstract void resume();

    @Override
    public void dispose() {
        if (disposables != null) {
            for (Disposable disposable : disposables) {
                try {
                    disposable.dispose();
                } catch (Exception ignored) {
                }
            }
            disposables.clear();
        }
        videoTexture.detachFromGLContext();

        GLES20.glDeleteTextures(1, textures, 0);

        if (shader != null) {
            shader.dispose();
        }
    }

    public void setOnVideoSizeListener(VideoSizeListener listener) {
        sizeListener = listener;
    }

    public void setOnCompletionListener(CompletionListener listener) {
        completionListener = listener;
    }

    public void setOnErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public abstract boolean isPlaying();

    public boolean isStereoscopic() {
        return isStereoscopic;
    }

    public void setStereoscopic(boolean stereoscopic) {
        isStereoscopic = stereoscopic;
        updateAspectRatio();
    }

    public boolean isHorizontalSplit() {
        return isHorizontalSplit;
    }

    public void setHorizontalSplit(boolean horizontalSplit) {
        isHorizontalSplit = horizontalSplit;
        updateAspectRatio();
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public boolean useFlatRectangle() {
        return displayMode == DisplayMode.Mono || displayMode == DisplayMode.LR3D || displayMode == DisplayMode.TB3D;
    }

    public boolean is180Video() {
        return displayMode == DisplayMode.Mono180 || displayMode == DisplayMode.LR180 || displayMode == DisplayMode.TB180;
    }

    public boolean is360Video() {
        return displayMode == DisplayMode.Mono360 || displayMode == DisplayMode.LR360 || displayMode == DisplayMode.TB360;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        if (useFlatRectangle()) {
            modelInstance = rectModelInstance;
        } else {
            if (useCylinder)
                modelInstance = cylinderModelInstance;
            else
                modelInstance = sphereModelInstance;
        }
        switch (displayMode) {
            case Mono:
                isStereoscopic = false;
                isHorizontalSplit = false;
                updateAspectRatio();
                break;
            case Mono180:
                isStereoscopic = false;
                isHorizontalSplit = false;
                updateAspectRatio();
                break;
            case Mono360:
                isStereoscopic = false;
                isHorizontalSplit = false;
                updateAspectRatio();
                break;
            case LR3D:
                isStereoscopic = true;
                isHorizontalSplit = true;
                updateAspectRatio();
                break;
            case LR180:
                isStereoscopic = true;
                isHorizontalSplit = true;
                updateAspectRatio();
                break;
            case LR360:
                isStereoscopic = true;
                isHorizontalSplit = true;
                updateAspectRatio();
                break;
            case TB3D:
                isStereoscopic = true;
                isHorizontalSplit = false;
                updateAspectRatio();
                break;
            case TB180:
                isStereoscopic = true;
                isHorizontalSplit = false;
                updateAspectRatio();
                break;
            case TB360:
                isStereoscopic = true;
                isHorizontalSplit = false;
                updateAspectRatio();
                break;
        }
    }

    public abstract void seekTo(long position);

    public abstract long getCurrentPosition();

    public abstract long getDuration();

    public void setAspectRatio(float ratio) {
        if (ratio < 0)
            updateAspectRatio();
        else
            aspectRatio = ratio;
    }

    public Vector2 getStretch() {
        return stretch;
    }

    public void setStretch(Vector2 stretch) {
        this.stretch.set(stretch);
        updateAspectRatio();
    }

    public void setUseFishEyeProjection(boolean useFishEyeProjection) {
        this.useFishEyeProjection = useFishEyeProjection;
    }

    public void setUseCylinder(boolean useCylinder) {
        this.useCylinder = useCylinder;
        setDisplayMode(displayMode);
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getModelSize() {
        return modelSize;
    }

    public void setModelSize(float modelSize) {
        this.modelSize = modelSize;
    }

    public VideoShader getShader() {
        return shader;
    }

    public void setHeadRotation(Quaternion headRotation) {
        this.headRotation.set(headRotation);
        invHeadRotation.set(headRotation).conjugate();
    }

    public interface VideoSizeListener {
        void onVideoSizeChanged(float width, float height);
    }

    public interface CompletionListener {
        void onCompletion();
    }

    public interface ErrorListener {
        void onError(String error);
    }
}
