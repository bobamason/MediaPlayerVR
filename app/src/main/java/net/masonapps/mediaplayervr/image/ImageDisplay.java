package net.masonapps.mediaplayervr.image;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.base.Eye;

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
    public Vector3 position = new Vector3();
    public Quaternion rotation = new Quaternion();
    public float scale = 1f;
    protected ModelInstance modelInstance;
    protected Array<Disposable> disposables = new Array<>();
    protected ImageShader shader;
    protected boolean isStereoscopic = false;
    protected boolean isHorizontalSplit = false;
    protected DisplayMode displayMode;
    protected float aspectRatio = 1f;
    //    protected float targetAspectRatio = 1f;
    private int width;
    private int height;
    private Vector2 stretch = new Vector2();
    private Rectangle srcRect = new Rectangle(0, 0, 1, 1);
    private Rectangle dstRect = new Rectangle(0, 0, 1, 1);
    private float shift = 0f;

    public ImageDisplay(Model rect, Model sphere) {
        this(DisplayMode.Mono, rect, sphere);
    }

    public ImageDisplay(DisplayMode displayMode, Model rect, Model sphere) {
        this.width = 1;
        this.height = 1;
        shader = new ImageShader();
        rectModelInstance = new ModelInstance(rect);
        sphereModelInstance = new ModelInstance(sphere);
        setDisplayMode(displayMode);
    }

    protected void updateAspectRatio() {
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

    public void render(ModelBatch batch) {
        render(batch, Eye.Type.MONOCULAR);
    }

    public void render(ModelBatch batch, int eyeType) {
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
        modelInstance.transform.set(position.x, position.y, position.z, rotation.x, rotation.y, rotation.z, rotation.w, scale, scale, scale);
    }

    protected void mapDistortModel() {
        if (aspectRatio <= 1f) {
            modelInstance.transform.set(position.x, position.y, position.z, rotation.x, rotation.y, rotation.z, rotation.w, aspectRatio * scale + stretch.x * scale, scale + stretch.y * scale, scale);
        } else {
            modelInstance.transform.set(position.x, position.y, position.z, rotation.x, rotation.y, rotation.z, rotation.w, scale + stretch.x * scale, scale / aspectRatio + stretch.y * scale, scale);
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

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public ImageShader getShader() {
        return shader;
    }

    public void setTexture(Texture texture) {
        width = texture.getWidth();
        height = texture.getHeight();

        if (shader.texture != null)
            shader.texture.dispose();
        shader.texture = texture;

        updateAspectRatio();
    }
}
