package net.masonapps.mediaplayervr.chooser;

import android.content.Context;
import android.graphics.Bitmap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.media.ImageDetails;
import net.masonapps.mediaplayervr.media.MediaUtils;

import org.masonapps.libgdxgooglevr.ui.TableVR;

import java.util.concurrent.ExecutorService;

/**
 * Created by Bob on 2/23/2017.
 */

public class ImageListLayout extends GridUiLayout<ImageDetails> {

    private Drawable defaultPictureDrawable;

    public ImageListLayout(Context context, Skin skin, Batch batch, ExecutorService executor) {
        super(context, skin, batch, executor);
        defaultPictureDrawable = skin.newDrawable(Style.Drawables.ic_movie_white_48dp);
    }

    @Override
    protected GridItemHolder<ImageDetails> createHolder(TableVR table, Image image, Label label) {
        return new PictureItemHolder(table, image, label, defaultPictureDrawable);
    }

    @Override
    protected Bitmap getImageBitmap(Context context, ImageDetails obj) {
        return MediaUtils.getImageThumbnailBitmap(context, obj.id);
    }

    private class PictureItemHolder extends GridItemHolder<ImageDetails> {

        PictureItemHolder(TableVR table, Image image, Label label, Drawable defaultDrawable) {
            super(table, image, label, defaultDrawable);
        }

        @Override
        public void bind(ImageDetails newObj) {
            super.bind(newObj);
            label.setText(getTruncatedTitle(newObj.title));
        }
    }
}
