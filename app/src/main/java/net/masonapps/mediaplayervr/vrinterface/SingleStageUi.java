package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;

import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

/**
 * Created by Bob on 3/13/2017.
 */

public class SingleStageUi extends DialogVR {

    protected final Skin skin;
    protected int padding = 10;

    public SingleStageUi(Batch spriteBatch, Skin skin) {
        super(spriteBatch, skin, 512, 512);
        this.skin = skin;

        final ImageButton closeButton = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        getTable().add(closeButton).expandX().right().row();
        setAnimationDuration(0.25f);
    }

    public void attach(VrUiContainer container) {
        container.addProcessor(this);
    }

    public void update() {
        act();
    }
}
