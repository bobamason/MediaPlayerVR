package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Disposable;

import org.masonapps.libgdxgooglevr.input.VrUiContainer;

/**
 * Created by Bob on 2/8/2017.
 */

public abstract class BaseUiLayout implements Disposable {
    protected int padding = 10;

    public void update() {
    }

    public void draw(Camera camera) {
    }

    public abstract void attach(VrUiContainer container);

    public abstract boolean isVisible();

    public abstract void setVisible(boolean visible);
}
