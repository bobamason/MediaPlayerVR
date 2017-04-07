package net.masonapps.mediaplayervr;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.mediaplayervr.utils.ModelGenerator;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;

/**
 * Created by Bob on 12/28/2016.
 */
public class LoadingScreen extends VrWorldScreen {

    private static final float SPEED = -360f;
    private final Entity entity;

    public LoadingScreen(VrGame game) {
        super(game);
        setBackgroundColor(Color.BLACK);
        getVrCamera().near = 0.1f;
        final Model rect = ModelGenerator.createRect(new ModelBuilder());
        final Texture texture = new Texture("loading.png");
        manageDisposable(texture);
        rect.materials.get(0).set(new BlendingAttribute(), TextureAttribute.createDiffuse(texture));
        entity = getWorld().add(new Entity(new ModelInstance(rect, 0, 0, -6)));
        entity.setLightingEnabled(false);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void show() {
        try {
            GdxVr.app.getGvrView().setNeckModelEnabled(true);
            GdxVr.app.getGvrView().setNeckModelFactor(1f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void update() {
        super.update();
        entity.transform.rotate(Vector3.Z, GdxVr.graphics.getDeltaTime() * SPEED);
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
    }
}
