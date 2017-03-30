package net.masonapps.mediaplayervr.image;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLES20;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.base.Eye;

import net.masonapps.mediaplayervr.utils.ModelGenerator;
import net.masonapps.mediaplayervr.video.DisplayMode;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;

/**
 * Created by Bob on 3/30/2017.
 */

public class ImageDisplay implements Disposable {

    public static final String TAG = VrVideoPlayer.class.getSimpleName();
    protected final ModelInstance rectModelInstance;
    //    protected final ModelInstance halfSphereModelInstance;
    protected final ModelInstance sphereModelInstance;
    protected final Object lock = new Object();
    protected ModelInstance modelInstance;
    protected Array<Disposable> disposables = new Array<>();
    protected ImageShader shader;
    protected int[] textures = new int[1];
    protected SurfaceTexture videoTexture;
    protected boolean prepared = false;
    protected boolean frameAvailable = false;
    protected boolean isStereoscopic = false;
    protected boolean isHorizontalSplit = false;
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
    private float shift = 0f;

    public ImageDisplay(Context context, Uri uri, int width, int height) {
        this(context, uri, width, height, DisplayMode.Mono);
    }

    public ImageDisplay(Context context, Uri uri, int width, int height, DisplayMode displayMode) {
        this.context = context;
        this.width = width;
        this.height = height;
        shader = new ImageShader();
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Model rect = ModelGenerator.createRect(modelBuilder);
        disposables.add(rect);
        rectModelInstance = new ModelInstance(rect);
        final int divisionsU = 64;
        final int divisionsV = 64;
        final Model sphere = ModelGenerator.createSphere(modelBuilder, 0.5f, divisionsU * 2, divisionsV);
        disposables.add(sphere);
        sphereModelInstance = new ModelInstance(sphere);
        setDisplayMode(displayMode);
    }

    protected void updateAspectRatio() {
        if (!prepared) return;
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
        } else if (use180Sphere()) {
            srcRect.set(0, 0, 1, 1);
//            final float invAspect = 1f / aspectRatio;
//            dstRect.set(0.25f + stretch.x * 0.5f, (1f - invAspect) * 0.5f - stretch.y * 0.5f, 0.5f + stretch.x, invAspect + stretch.y);
            dstRect.set(0.25f - stretch.x * 0.5f, -stretch.y * 0.5f, 0.5f + stretch.x, 1f + stretch.y);
        } else {
            srcRect.set(0, 0, 1, 1);
            dstRect.set(-stretch.x * 0.5f, -stretch.y * 0.5f, 1f + stretch.x, 1f + stretch.y);
        }
    }

    public void update() {
        if (!prepared) {
            return;
        }
        synchronized (this) {
            if (frameAvailable) {
                videoTexture.updateTexImage();
                frameAvailable = false;
            }
        }
    }

    public void render(ModelBatch batch) {
        render(batch, Eye.Type.MONOCULAR);
    }

    public void render(ModelBatch batch, int eyeType) {
        if (!prepared) {
            return;
        }

        if (useFlatRectangle())
            mapDistortModel();
        else
            mapDistortTextureCoordinates();

        if (isStereoscopic) {
            switch (eyeType) {
                case Eye.Type.LEFT:
                    if (isHorizontalSplit) {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width * 0.5f, srcRect.height);
                    } else {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width, srcRect.height * 0.5f);
                    }
                    shader.setDstRect(dstRect.x - shift, dstRect.y, dstRect.width, dstRect.height);
                    break;
                case Eye.Type.RIGHT:
                    if (isHorizontalSplit) {
                        shader.setSrcRect(srcRect.x + 0.5f, srcRect.y, srcRect.width * 0.5f, srcRect.height);
                    } else {
                        shader.setSrcRect(srcRect.x, srcRect.y + 0.5f, srcRect.width, srcRect.height * 0.5f);
                    }
                    shader.setDstRect(dstRect.x + shift, dstRect.y, dstRect.width, dstRect.height);
                    break;
                default:
                    if (isHorizontalSplit) {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width * 0.5f, srcRect.height);
                    } else {
                        shader.setSrcRect(srcRect.x, srcRect.y, srcRect.width, srcRect.height * 0.5f);
                    }
                    shader.setDstRect(dstRect.x - shift, dstRect.y, dstRect.width, dstRect.height);
                    break;
            }
        } else {
            shader.getSrcRect().set(srcRect);
            shader.getDstRect().set(dstRect);
        }

        batch.render(modelInstance, shader);
    }

    protected void mapDistortTextureCoordinates() {
        modelInstance.transform.idt().scale(modelSize, modelSize, modelSize);
    }

    protected void mapDistortModel() {
        if (aspectRatio <= 1f) {
            modelInstance.transform.idt().translate(0, 0, -4).scale(aspectRatio * modelSize + stretch.x * modelSize, modelSize + stretch.y * modelSize, modelSize);
        } else {
            modelInstance.transform.idt().translate(0, 0, -4).scale(modelSize + stretch.x * modelSize, modelSize / aspectRatio + stretch.y * modelSize, modelSize);
        }
    }

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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

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

    public boolean use180Sphere() {
        return displayMode == DisplayMode.Mono180 || displayMode == DisplayMode.LR180 || displayMode == DisplayMode.TB180;
    }

    public boolean use360Sphere() {
        return displayMode == DisplayMode.Mono360 || displayMode == DisplayMode.LR360 || displayMode == DisplayMode.TB360;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        if (useFlatRectangle()) {
            modelInstance = rectModelInstance;
        } else if (use180Sphere()) {
//            modelInstance = halfSphereModelInstance;
            modelInstance = sphereModelInstance;
        } else {
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

    public void setAspectRatio(float ratio) {
        if (ratio < 0)
            updateAspectRatio();
        else
            aspectRatio = ratio;
    }

    public void setStretch(Vector2 stretch) {
        this.stretch.set(stretch);
        updateAspectRatio();
    }

    public float getModelSize() {
        return modelSize;
    }

    public void setModelSize(float modelSize) {
        this.modelSize = modelSize;
    }

    public ImageShader getShader() {
        return shader;
    }
}
