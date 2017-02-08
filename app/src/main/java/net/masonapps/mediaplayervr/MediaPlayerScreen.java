package net.masonapps.mediaplayervr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;

/**
 * Created by Bob on 2/7/2017.
 */

public abstract class MediaPlayerScreen extends VrWorldScreen {
    private static final Vector3 tempV = new Vector3();
    protected final MediaPlayerGame mediaPlayerGame;
    protected final Entity controllerEntity;
    protected final Entity highlightEntity;
    protected final Entity roomEntity;
    protected final Entity floorEntity;
    protected final Skin skin;
    private final Vector3 scale = new Vector3(10f, 10f, 10f);

    public MediaPlayerScreen(VrGame game) {
        super(game);
        mediaPlayerGame = (MediaPlayerGame) game;
        skin = mediaPlayerGame.getSkin();
        roomEntity = getWorld().add(mediaPlayerGame.getRoomEntity());
        floorEntity = getWorld().add(mediaPlayerGame.getFloorEntity());
        highlightEntity = getWorld().add(mediaPlayerGame.getHighlightEntity());
        controllerEntity = getWorld().add(mediaPlayerGame.getControllerEntity());
    }

    @Override
    protected BaseLight createLight() {
        final PointLight pointLight = new PointLight();
        pointLight.setColor(Color.WHITE);
        pointLight.setPosition(0f, 3f, 0f);
        pointLight.setIntensity(10f);
        return pointLight;
    }

    @Override
    protected Environment createEnvironment() {
        final Environment environment = new Environment();
        environment.set(ColorAttribute.createAmbient(Color.GRAY));
        return environment;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
        controllerEntity.modelInstance.transform.set(tempV.set(GdxVr.input.getControllerPosition()).add(GdxVr.input.getHandPosition()), GdxVr.input.getControllerOrientation(), scale);
    }
}
