package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

/**
 * Created by Bob on 3/13/2017.
 */

public class SingleStageUi extends BaseUiLayout {

    public final VirtualStage stage;
    protected final Skin skin;

    public SingleStageUi(Batch spriteBatch, Skin skin) {
        this(new VirtualStage(spriteBatch, 512, 512), skin);
    }

    public SingleStageUi(VirtualStage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        inputMultiplexer.addProcessor(stage);
    }

    @Override
    public boolean isVisible() {
        return stage.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        stage.setVisible(visible);
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
    }
}
