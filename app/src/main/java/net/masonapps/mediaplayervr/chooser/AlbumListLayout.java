package net.masonapps.mediaplayervr.chooser;

import android.content.Context;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.masonapps.mediaplayervr.media.AlbumDetails;

/**
 * Created by Bob on 2/22/2017.
 */

public class AlbumListLayout extends GridUiLayout<AlbumDetails> {

    public AlbumListLayout(Context context, Skin skin, Batch batch) {
        super(context, skin, batch);
    }

    @Override
    protected GridItemHolder<AlbumDetails> createHolder(Table table, Image image, Label label) {
        return null;
    }

    @Override
    protected Pixmap getImagePixmap(Context context, AlbumDetails obj) {
        return null;
    }
}
