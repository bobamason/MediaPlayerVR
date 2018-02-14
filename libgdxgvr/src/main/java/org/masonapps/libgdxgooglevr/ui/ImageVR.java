package org.masonapps.libgdxgooglevr.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

/**
 * Created by Bob Mason on 10/12/2017.
 */

public class ImageVR extends VirtualStage {

    private final Image image;

    public ImageVR(Batch batch, NinePatch patch) {
        this(batch, new NinePatchDrawable(patch), Scaling.stretch, Align.center);
    }

    public ImageVR(Batch batch, TextureRegion region) {
        this(batch, new TextureRegionDrawable(region), Scaling.stretch, Align.center);
    }

    public ImageVR(Batch batch, Texture texture) {
        this(batch, new TextureRegionDrawable(new TextureRegion(texture)));
    }

    public ImageVR(Batch batch, Skin skin, String drawableName) {
        this(batch, skin.getDrawable(drawableName), Scaling.stretch, Align.center);
    }

    public ImageVR(Batch batch, Drawable drawable) {
        this(batch, drawable, Scaling.stretch, Align.center);
    }

    public ImageVR(Batch batch, Drawable drawable, Scaling scaling) {
        this(batch, drawable, scaling, Align.center);
    }

    public ImageVR(Batch batch, Drawable drawable, Scaling scaling, int align) {
        super(batch, (int) drawable.getMinWidth(), (int) drawable.getMinHeight());
        image = new Image(drawable, scaling, align);
        addActor(image);
        image.setPosition(0, 0);
        setSize((int) image.getWidth(), (int) image.getHeight());
    }

    public Image getImage() {
        return image;
    }
}
