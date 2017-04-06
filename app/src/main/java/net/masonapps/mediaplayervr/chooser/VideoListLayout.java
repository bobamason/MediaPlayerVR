package net.masonapps.mediaplayervr.chooser;

import android.content.Context;
import android.graphics.Bitmap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.ui.TableVR;

import java.util.concurrent.ExecutorService;

/**
 * Created by Bob on 2/23/2017.
 */

public class VideoListLayout extends GridUiLayout<VideoDetails> {

    private Drawable defaultVideoDrawable;

    public VideoListLayout(Context context, Skin skin, Batch batch, ExecutorService executor) {
        super(context, skin, batch, executor);
        defaultVideoDrawable = skin.newDrawable(Style.Drawables.ic_movie_white_48dp);
    }

    @Override
    protected GridItemHolder<VideoDetails> createHolder(TableVR table, Image image, Label label) {
        return new VideoItemHolder(table, image, label, defaultVideoDrawable);
    }

    @Override
    protected Bitmap getImageBitmap(Context context, VideoDetails obj) {
        return MediaUtils.getVideoThumbnailBitmap(context, obj.id);
    }

    private class VideoItemHolder extends GridItemHolder<VideoDetails> {

        VideoItemHolder(TableVR table, Image image, Label label, Drawable defaultDrawable) {
            super(table, image, label, defaultDrawable);
        }

        @Override
        public void bind(VideoDetails newObj) {
            super.bind(newObj);
            label.setText(getTruncatedTitle(newObj.title));
        }
    }
}
