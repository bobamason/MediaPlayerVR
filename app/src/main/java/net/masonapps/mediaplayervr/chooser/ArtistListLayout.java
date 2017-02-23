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
import net.masonapps.mediaplayervr.media.ArtistDetails;

/**
 * Created by Bob on 2/23/2017.
 */

public class ArtistListLayout extends GridUiLayout<ArtistDetails> {

    public ArtistListLayout(Context context, Skin skin, Batch batch) {
        super(context, skin, batch);
    }

    @Override
    protected GridItemHolder<ArtistDetails> createHolder(Table table, Image image, Label label) {
        return new ArtistItemHolder(table, image, label, skin.newDrawable(Style.Drawables.ic_album_white_48dp));
    }

    @Override
    protected Pixmap getImagePixmap(Context context, ArtistDetails obj) {
        return null;
    }

    private class ArtistItemHolder extends GridItemHolder<ArtistDetails> {

        ArtistItemHolder(Table table, Image image, Label label, Drawable defaultDrawable) {
            super(table, image, label, defaultDrawable);
        }

        @Override
        public void bind(ArtistDetails newObj) {
            super.bind(newObj);
            label.setText(getTruncatedTitle(newObj.title));
        }
    }
}
