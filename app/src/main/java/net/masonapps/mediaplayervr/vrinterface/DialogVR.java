package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleByAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import org.masonapps.libgdxgooglevr.ui.TableVR;

/**
 * Created by Bob Mason on 10/12/2017.
 */

public class DialogVR extends TableVR {


    private final Array<Action> actions = new Array<>(0);
    private final Actor dummyActor = new Actor();
    private Interpolation animationInterpolation = Interpolation.swing;
    private float animationDuration = 0.5f;

    public DialogVR(Batch batch, int tableWidth, int tableHeight) {
        super(batch, tableWidth, tableHeight);
    }

    public DialogVR(Batch batch, Skin skin, int tableWidth, int tableHeight) {
        super(batch, skin, tableWidth, tableHeight);
    }

    public void show() {
//        dummyActor.getColor().a = 0;
        dummyActor.setScale(0);
        setVisible(true);
        addAction(Actions.scaleTo(1f, 1f, animationDuration, animationInterpolation));
    }

    public void hide() {
        addAction(Actions.sequence(Actions.scaleTo(0f, 0f), Actions.run(() -> setVisible(false))));
    }

    @Override
    public void addAction(Action action) {
        action.setActor(dummyActor);
        actions.add(action);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Array<Action> actions = this.actions;
        if (actions.size > 0) {
            for (int i = 0; i < actions.size; i++) {
                Action action = actions.get(i);
                final boolean done = action.act(delta);
                if (done && i < actions.size) {
                    Action current = actions.get(i);
                    int actionIndex = current == action ? i : actions.indexOf(action, true);
                    if (actionIndex != -1) {
                        actions.removeIndex(actionIndex);
                        action.setActor(null);
                        i--;
                    }
                } else if (!done) {
                    final Actor actor = action.getActor();
                    if (actor != null) {
                        if (action instanceof AlphaAction)
                            setAlpha(actor.getColor().a);
                        if (action instanceof ScaleByAction || action instanceof ScaleToAction)
                            setScale(actor.getScaleX(), actor.getScaleY());
                    }
                }
            }
        }
    }

    public void removeAction(Action action) {
        if (actions.removeValue(action, true)) action.setActor(null);
    }

    public Array<Action> getActions() {
        return actions;
    }

    public boolean hasActions() {
        return actions.size > 0;
    }

    public void clearActions() {
        for (int i = actions.size - 1; i >= 0; i--)
            actions.get(i).setActor(null);
        actions.clear();
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
