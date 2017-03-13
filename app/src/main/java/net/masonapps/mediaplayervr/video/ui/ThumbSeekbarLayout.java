package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 3/13/2017.
 */

public class ThumbSeekbarLayout extends SingleStageUi {
    public Label label;
    public Slider slider;

    public ThumbSeekbarLayout(Batch spriteBatch, Skin skin) {
        super(new VirtualStage(spriteBatch, 720, 180), skin);

        final Table table = new Table(skin);

        label = new Label("Value 100%", skin);
        table.add(label).center().pad(padding);

        slider = new Slider(0f, 1f, 1f / 10000f, false, skin);
        slider.setValue(0.5f);
        table.add(slider).expandX().fillX().pad(padding);
    }

    public void onTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                break;
            case DaydreamTouchEvent.ACTION_UP:
                break;
        }
    }

    public interface OnThumbSeekListener {
        void onSeekChanged(float value);
    }
}
