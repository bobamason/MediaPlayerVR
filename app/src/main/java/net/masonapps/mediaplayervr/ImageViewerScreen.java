package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.image.ImageDisplay;
import net.masonapps.mediaplayervr.media.ImageDetails;
import net.masonapps.mediaplayervr.video.DisplayMode;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;

/**
 * Created by Bob on 12/24/2016.
 */

public class ImageViewerScreen extends VrWorldScreen implements DaydreamControllerInputListener {

    private static final Vector3 tempV = new Vector3();
    private static final Vector3 controllerScale = new Vector3(10f, 10f, 10f);
    private final Context context;
    private final ImageDetails imageDetails;
    private final Entity controllerEntity;
    private final VrUiContainer container;
    private final ImageDisplay imageDisplay;
    private boolean isButtonClicked = false;

    public ImageViewerScreen(VrGame game, Context context, ImageDetails imageDetails) {
        super(game);
        this.context = context;
        this.imageDetails = imageDetails;
        setBackgroundColor(Color.BLACK);
        imageDisplay = new ImageDisplay(DisplayMode.Mono, null, null);
        manageDisposable(imageDisplay);
        final SpriteBatch spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        container = new VrUiContainer();
        final Skin skin = ((MediaPlayerGame) game).getSkin();
        getVrCamera().near = 0.25f;
        getVrCamera().far = 100f;
        controllerEntity = getWorld().add(((MediaPlayerGame) game).getControllerEntity());
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {
        GdxVr.input.getDaydreamControllerHandler().addListener(this);
        GdxVr.input.setProcessor(container);
    }

    @Override
    public void hide() {
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setProcessor(null);
        dispose();
    }

    @Override
    public void update() {
        super.update();
        container.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
//        if(isUiVisible())
        container.draw(camera);
        getModelBatch().begin(camera);
        imageDisplay.render(getModelBatch(), whichEye);
        getModelBatch().end();
    }

    @Override
    public void setUiVisible(boolean uiVisible) {
        super.setUiVisible(uiVisible);
        controllerEntity.setRenderingEnabled(uiVisible);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        if (controllerEntity.isRenderingEnabled())
            controllerEntity.modelInstance.transform.set(GdxVr.input.getControllerPosition(), GdxVr.input.getControllerOrientation(), controllerScale);
    }

    @Override
    public void onConnectionStateChange(int connectionState) {
        if (connectionState == Controller.ConnectionStates.CONNECTED) {

        } else {

        }
    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
        switch (event.button) {
            case DaydreamButtonEvent.BUTTON_TOUCHPAD:
                if (event.action == DaydreamButtonEvent.ACTION_DOWN) {
                    isButtonClicked = true;
//                    if (videoPlayer.isPrepared()) {
                    if (isUiVisible()) {
                        if (!container.isCursorOver()) {
                            setUiVisible(false);
                        }
                    } else {
                        setUiVisible(true);
                    }
//                    }
                } else if (event.action == DaydreamButtonEvent.ACTION_UP) {
                    isButtonClicked = false;
                }
                break;
            case DaydreamButtonEvent.BUTTON_APP:
                if (event.action == DaydreamButtonEvent.ACTION_UP) {
//                    if (ui.thumbSeekbarLayout.isVisible()) {
                    setUiVisible(false);
//                        ui.hideThumbSeekbarLayout();
//                    } else
//                        ui.backButtonClicked();
                }
                break;
        }
    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        if (!isButtonClicked) {
            switch (event.action) {
                case DaydreamTouchEvent.ACTION_DOWN:
                    break;
                case DaydreamTouchEvent.ACTION_MOVE:
                    break;
                case DaydreamTouchEvent.ACTION_UP:
                    break;
            }
        }
    }

    public String getStringResource(int resId) {
        return context.getString(resId);
    }

    public void exit() {
        ((MediaPlayerGame) game).goToSelectionScreen();
    }
}
