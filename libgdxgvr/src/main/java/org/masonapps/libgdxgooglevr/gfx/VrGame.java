package org.masonapps.libgdxgooglevr.gfx;

import com.badlogic.gdx.graphics.Camera;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.vr.VrApplicationAdapter;

/**
 * Created by Bob on 12/22/2016.
 */

public abstract class VrGame extends VrApplicationAdapter {
    protected VrScreen screen;

    @Override
    public void pause() {
        if (screen != null) screen.pause();
    }

    @Override
    public void resume() {
        if (screen != null) screen.resume();
    }

    @Override
    public void update() {
        super.update();
        if (screen != null) screen.update();
    }

    @Override
    public void preRender() {
        if (screen != null) screen.preRender();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        if (screen != null) screen.render(camera, whichEye);
        super.render(camera, whichEye);
        if (screen != null) screen.renderCursor(camera);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        if (screen != null) screen.onNewFrame(headTransform);
    }

    @Override
    public void onDrawEye(Eye eye) {
        super.onDrawEye(eye);
        if (screen != null) screen.onDrawEye(eye);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (screen != null) screen.onDaydreamControllerUpdate(controller, connectionState);
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
        if (screen != null) screen.onCardboardTrigger();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (screen != null) {
            try {
                screen.hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                screen.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public VrScreen getScreen() {
        return screen;
    }

    public void setScreen(VrScreen screen) {
        try {
            if (this.screen != null)
                this.screen.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.screen = screen;
        try {
            if (this.screen != null)
                this.screen.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}