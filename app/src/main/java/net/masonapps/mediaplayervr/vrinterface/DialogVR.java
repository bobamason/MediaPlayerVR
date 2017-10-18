package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import org.masonapps.libgdxgooglevr.ui.TableVR;

/**
 * Created by Bob Mason on 10/12/2017.
 */

public class DialogVR extends TableVR {


    private Interpolation animationInterpolation = Interpolation.swing;
    private float animationDuration = 0.5f;

    public DialogVR(Batch batch, int tableWidth, int tableHeight) {
        super(batch, tableWidth, tableHeight);
    }

    public DialogVR(Batch batch, Skin skin, int tableWidth, int tableHeight) {
        super(batch, skin, tableWidth, tableHeight);
    }

    public void show() {
        setVisible(true);
        getRoot().setScale(0);
        addAction(Actions.scaleTo(1f, 1f, animationDuration, animationInterpolation));
    }

    public void hide() {
        addAction(Actions.sequence(Actions.scaleTo(0f, 0f, animationDuration, animationInterpolation), Actions.run(() -> setVisible(false))));
    }

    public void dismiss() {
        hide();
    }

    @Override
    public void setAnimationDuration(float animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setAnimationInterpolation(Interpolation animationInterpolation) {
        this.animationInterpolation = animationInterpolation;
    }
}
