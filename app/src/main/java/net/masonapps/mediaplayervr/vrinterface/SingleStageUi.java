package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

/**
 * Created by Bob on 3/13/2017.
 */

public class SingleStageUi extends BaseUiLayout {

    public final DialogVR dialogVR;
    public final Table table;
    public final Skin skin;

    public SingleStageUi(Batch spriteBatch, Skin skin) {
        this.dialogVR = new DialogVR(spriteBatch, skin, 512, 512);
        this.table = dialogVR.getTable();
        this.skin = skin;
    }

    @Override
    public void attach(VrUiContainer container) {
        container.addProcessor(dialogVR);
    }

    @Override
    public void update() {
        dialogVR.act();
    }

    @Override
    public void draw(Camera camera) {
        dialogVR.draw(camera);
    }

    @Override
    public boolean isVisible() {
        return dialogVR.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) dialogVR.show();
        else dialogVR.hide();
    }

    @Override
    public void dispose() {
        if (dialogVR != null)
            dialogVR.dispose();
    }
}
