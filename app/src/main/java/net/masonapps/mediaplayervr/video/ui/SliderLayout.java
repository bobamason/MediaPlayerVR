package net.masonapps.mediaplayervr.video.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 3/13/2017.
 */

public class SliderLayout extends SingleStageUi {

    private static final float MIN_MOVEMENT = 0.125f;
    private static final float SENSITIVITY = 0.025f;
    public Label labelLow;
    public Label label;
    public Label labelHigh;
    public Slider slider;
    private float downX;
    private float currentX;
    @Nullable
    private OnThumbSeekListener listener = null;

    public SliderLayout(Batch spriteBatch, Skin skin) {
        super(spriteBatch, skin);
        setSize(720, 160);
        setTouchable(true);

        labelLow = new Label("-", skin);
        table.add(labelLow)
                .left()
                .pad(padding);

        label = new Label("Value 100%", skin);
        table.add(label)
                .expandX()
                .center()
                .pad(padding);

        labelHigh = new Label("+", skin);
        table.add(labelHigh)
                .right()
                .pad(padding)
                .row();

        slider = new Slider(0f, 1f, 1f / 10000f, false, skin);
        slider.setValue(0.5f);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null)
                    listener.onSeekChanged(SliderLayout.this, slider.getValue());
            }
        });
        table.add(slider)
                .colspan(3)
                .growX()
                .minWidth(720)
                .padBottom(padding)
                .padLeft(padding)
                .padRight(padding)
                .row();

        final TextButton reset = new TextButton("reset", skin);
        reset.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                slider.setValue(0.5f);
            }
        });
        table.add(reset)
                .colspan(3)
                .expandX()
                .center()
                .padBottom(padding)
                .padLeft(padding)
                .padRight(padding);

        setBackground(skin.newDrawable(Style.Drawables.window, Color.BLACK));
        setVisible(false);
        resizeToFitTable();
    }

    public void setSliderValue(float value) {
        slider.setValue(value);
//        if (listener != null)
//            listener.onSeekChanged(this, slider.getValue());
    }

    public void setListener(@Nullable OnThumbSeekListener listener) {
        this.listener = listener;
    }

    public void onTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                downX = currentX = event.x;
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                currentX = event.x;
                final float diff = currentX - downX;
                final float abs = Math.abs(diff);
                if (abs > MIN_MOVEMENT) {
                    if (listener != null) {
                        final float x = diff > 0 ? (diff - MIN_MOVEMENT) : (diff + MIN_MOVEMENT);
                        slider.setValue(MathUtils.clamp(slider.getValue() + x * GdxVr.graphics.getDeltaTime() * SENSITIVITY, 0f, 1f));
//                        if (listener != null)
//                            listener.onSeekChanged(this, slider.getValue());
                    }
                }
                break;
            case DaydreamTouchEvent.ACTION_UP:
                break;
        }
    }

    public interface OnThumbSeekListener {
        void onSeekChanged(SliderLayout sliderLayout, float value);
    }
}
