package net.masonapps.mediaplayervr.chooser;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.AsyncTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.MediaPlayerGame;
import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.image.ImageDisplay;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.video.ui.ModeLayout;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;
import org.masonapps.libgdxgooglevr.ui.ImageButtonVR;
import org.masonapps.libgdxgooglevr.ui.VirtualStage;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bob on 4/3/2017.
 */

public class VideoPreviewUi {
    private final Model rectModel;
    private final Model sphereModel;
    public List<VideoDetails> list = new CopyOnWriteArrayList<>();
    private List<Texture> thumbnailTextures = new CopyOnWriteArrayList<>();
    private ImageDisplay imageDisplay;
    private WeakReference<Context> contextRef;
    private OnPlayClickedListener listener = null;
    private VideoDetails videoDetails = null;
    private VideoOptions videoOptions;
    private VirtualStage playButton;

    public VideoPreviewUi(WeakReference<Context> contextRef, Model rectModel, Model sphereModel, Batch batch, Skin skin) {
        this.contextRef = contextRef;
        this.rectModel = rectModel;
        this.sphereModel = sphereModel;
        imageDisplay = new ImageDisplay(rectModel, sphereModel);
        playButton = new ImageButtonVR(batch, Style.createImageButtonStyle(skin, Style.Drawables.ic_play_circle_filled_white_48dp, true));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null)
                    listener.onClicked(videoDetails);
            }
        });
    }

    public void attach(VrUiContainer vrUiContainer) {
        vrUiContainer.addProcessor(playButton);
    }

    public void render(ModelBatch batch, int eyeType) {
        imageDisplay.render(batch, eyeType);
    }

    public void load(final MediaPlayerGame mediaPlayerGame, final VideoDetails videoDetails) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final VideoOptions videoOptions = mediaPlayerGame.getVideoOptionsDatabaseHelper().getVideoOptionsByTitle(videoDetails.title);
                GdxVr.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        imageDisplay.setDisplayMode(ModeLayout.getMode(videoOptions.modeSelection));
                    }
                });
            }
        }).start();
        this.videoDetails = videoDetails;
        new ImageTask(contextRef, new WeakReference<>(imageDisplay)).execute(videoDetails);
    }

    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
    }

    public ImageDisplay getImageDisplay() {
        return imageDisplay;
    }

    public VirtualStage getPlayButton() {
        return playButton;
    }

    public void setListener(OnPlayClickedListener listener) {
        this.listener = listener;
    }

    public interface OnPlayClickedListener {
        void onClicked(VideoDetails videoDetails);
    }

    private static class ImageTask extends AsyncTask<VideoDetails, Void, Bitmap> {

        private final WeakReference<Context> contextRef;
        private final WeakReference<ImageDisplay> imageDisplayRef;

        public ImageTask(WeakReference<Context> contextRef, WeakReference<ImageDisplay> imageDisplayRef) {
            this.contextRef = contextRef;
            this.imageDisplayRef = imageDisplayRef;
        }

        @Override
        protected Bitmap doInBackground(VideoDetails... params) {
            final Context context = contextRef.get();
            if (isCancelled() || context == null) return null;
            return MediaUtils.getVideoFullscreenBitmap(context, params[0].id);
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            if (bitmap == null) return;
            if (isCancelled()) {
                bitmap.recycle();
                return;
            }
            GdxVr.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    final ImageDisplay imageDisplay = imageDisplayRef.get();
                    if (imageDisplay != null) {
                        final Texture texture = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
                        Gdx.gl.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                        Gdx.gl.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                        imageDisplay.setTexture(texture);
                    }
                    bitmap.recycle();
                }
            });
        }
    }
}
