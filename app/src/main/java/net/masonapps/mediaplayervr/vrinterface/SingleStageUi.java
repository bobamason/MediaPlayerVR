package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

/**
 * Created by Bob on 3/13/2017.
 */

public class SingleStageUi extends BaseUiLayout {

    public final VirtualStage stage;
    public final Skin skin;
    public final Table table;

    public SingleStageUi(Batch spriteBatch, Skin skin) {
        this(new VirtualStage(spriteBatch, 512, 512), skin);
    }

    public SingleStageUi(VirtualStage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        table = new Table(skin);
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        stage.addActor(table);
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
