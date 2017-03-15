package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 3/13/2017.
 */

public class ThumbSeekbarLayout extends SingleStageUi {

    private static final float MIN_MOVEMENT = 0.25f;
    private static final float SENSITIVITY = 0.125f;
    public Label label;
    public Slider slider;
    private float downX;
    private float currentX;
    private float lastX;
    private boolean activated;
    private OnThumbSeekListener listener = null;

    public ThumbSeekbarLayout(Batch spriteBatch, Skin skin) {
        super(new VirtualStage(spriteBatch, 720, 200), skin);
        stage.setTouchable(false);

        label = new Label("Value 100%", skin);
        table.add(label).center().pad(padding).row();

        slider = new Slider(0f, 1f, 1f / 10000f, false, skin);
        slider.setValue(0.5f);
        table.add(slider).expandX().fillX().pad(padding);
    }

    public void setListener(OnThumbSeekListener listener) {
        this.listener = listener;
    }

    public void onTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                activated = false;
                downX = lastX = currentX = event.x;
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                currentX = event.x;
                if (!activated) {
                    if (Math.abs(currentX - downX) > MIN_MOVEMENT) {
                        activated = true;
                    }
                } else {
                    if (listener != null) {
                        slider.setValue(MathUtils.clamp(slider.getValue() + (currentX - lastX) * SENSITIVITY, 0f, 1f));
                        listener.onSeekChanged(slider.getValue());
                    }
                }
                lastX = currentX;
                break;
            case DaydreamTouchEvent.ACTION_UP:
                activated = false;
                break;
        }
    }

    public interface OnThumbSeekListener {
        void onSeekChanged(float value);
    }
}
