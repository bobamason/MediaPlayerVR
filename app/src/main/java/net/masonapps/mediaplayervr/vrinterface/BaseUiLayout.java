package net.masonapps.mediaplayervr.vrinterface;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Disposable;

import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

/**
 * Created by Bob on 2/8/2017.
 */

public abstract class BaseUiLayout implements Disposable {
    protected int padding = 10;

    public abstract void update();

    public abstract void draw(Camera camera);

    public abstract void attach(VrInputMultiplexer inputMultiplexer);

    public abstract boolean isVisible();

    public abstract void setVisible(boolean visible);
}
