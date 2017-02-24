package net.masonapps.mediaplayervr.chooser;

import android.content.Context;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.media.AlbumDetails;

import org.masonapps.libgdxgooglevr.GdxVr;

/**
 * Created by Bob on 2/22/2017.
 */

public class AlbumListLayout extends GridUiLayout<AlbumDetails> {

    private final Drawable defaultVideoDrawable;

    public AlbumListLayout(Context context, Skin skin, Batch batch) {
        super(context, skin, batch);
        defaultVideoDrawable = skin.newDrawable(Style.Drawables.ic_album_white_48dp);
    }

    @Override
    protected GridItemHolder<AlbumDetails> createHolder(Table table, Image image, Label label) {
        return new AlbumItemHolder(table, image, label, defaultVideoDrawable);
    }

    @Override
    protected Pixmap getImagePixmap(Context context, AlbumDetails obj) {
        return new Pixmap(GdxVr.files.external(obj.thumbnailPath));
    }

    private class AlbumItemHolder extends GridItemHolder<AlbumDetails> {

        AlbumItemHolder(Table table, Image image, Label label, Drawable defaultDrawable) {
            super(table, image, label, defaultDrawable);
        }

        @Override
        public void bind(AlbumDetails newObj) {
            super.bind(newObj);
            label.setText(getTruncatedTitle(newObj.title));
        }
    }
}
