package net.masonapps.mediaplayervr.chooser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.media.AlbumDetails;

import org.masonapps.libgdxgooglevr.ui.TableVR;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * Created by Bob on 2/22/2017.
 */

public class AlbumListLayout extends GridUiLayout<AlbumDetails> {

    public AlbumListLayout(Context context, Skin skin, Batch batch, ExecutorService executor) {
        super(context, skin, batch, executor);
    }

    @Override
    protected GridItemHolder<AlbumDetails> createHolder(TableVR table, Image image, Label label) {
        return new AlbumItemHolder(table, image, label, skin.newDrawable(Style.Drawables.ic_album_white_48dp));
    }

    @Override
    protected Bitmap getImageBitmap(Context context, AlbumDetails obj) {
        final String thumbnailPath = obj.thumbnailPath;
        if (thumbnailPath == null) return null;
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(Uri.parse(obj.thumbnailPath));
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    private class AlbumItemHolder extends GridItemHolder<AlbumDetails> {

        AlbumItemHolder(TableVR table, Image image, Label label, Drawable defaultDrawable) {
            super(table, image, label, defaultDrawable);
        }

        @Override
        public void bind(AlbumDetails newObj) {
            super.bind(newObj);
            label.setText(getTruncatedTitle(newObj.title));
        }
    }
}
