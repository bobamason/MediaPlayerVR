package net.masonapps.mediaplayervr;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
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
    protected final Array<Entity> boxes = new Array<>();
    //    protected final Entity roomEntity;
    protected final Entity floorEntity;
    protected final Skin skin;
    private final Vector3 scale = new Vector3(10f, 10f, 10f);
    private final Quaternion tempQ = new Quaternion();

    public MediaPlayerScreen(VrGame game) {
        super(game);
        mediaPlayerGame = (MediaPlayerGame) game;
        skin = mediaPlayerGame.getSkin();
        floorEntity = getWorld().add(mediaPlayerGame.getFloorEntity());
        controllerEntity = getWorld().add(mediaPlayerGame.getControllerEntity());
        final ModelBuilder modelBuilder = new ModelBuilder();

        final Model box = createBox(modelBuilder);
        final Matrix4 mat = new Matrix4();
        final Vector3 axis = new Vector3();
        for (int i = 0; i < 200; i++) {
            final float r = MathUtils.random(5f, 15f);
            final float a = MathUtils.random(MathUtils.PI2);
            axis.set(MathUtils.random(), MathUtils.random(), MathUtils.random()).nor();

            final Transform transform = new Transform();
            transform.position.set(r * MathUtils.cos(a), MathUtils.random(-10f, 10f), -r * MathUtils.sin(a));
            transform.rotation.set(axis, MathUtils.random(360f));
            transform.scale.set(MathUtils.random(0.1f, 1f), MathUtils.random(0.1f, 1f), MathUtils.random(0.1f, 1f));

            mat.set(transform.position, transform.rotation, transform.scale);
            final Entity entity = new Entity(new ModelInstance(box, mat.cpy()));
            entity.modelInstance.userData = transform;
            boxes.add(getWorld().add(entity));
        }
        
    }

    @Override
    protected ModelBatch createModelBatch() {
        return super.createModelBatch();
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final PointLight pointLight = new PointLight();
        pointLight.setColor(Color.WHITE);
        pointLight.setPosition(0f, 3f, 0f);
        pointLight.setIntensity(6f);
        lights.add(pointLight);
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

    private Model createBox(ModelBuilder modelBuilder) {
        final Material material = new Material(ColorAttribute.createDiffuse(Color.CYAN), ColorAttribute.createAmbient(Color.CYAN), ColorAttribute.createSpecular(Color.WHITE));
        return modelBuilder.createBox(1f, 1f, 1f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    @Override
    public void update() {
        super.update();
        if (GdxVr.graphics.getFrameId() % 60 == 0)
            Log.d(MediaPlayerScreen.class.getSimpleName(), "fps" + GdxVr.graphics.getFramesPerSecond());

        for (Entity entity : boxes) {
            final Transform transform = (Transform) entity.modelInstance.userData;
            tempQ.set(tempV.set(MathUtils.random(), MathUtils.random(), MathUtils.random()), Gdx.graphics.getDeltaTime() * 90f);
            transform.rotation.mul(tempQ);
            entity.transform.set(transform.position, transform.rotation, transform.scale);
        }
    }

    public Skin getSkin() {
        return skin;
    }

    private static class Transform {
        public final Vector3 position = new Vector3();
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3();
    }
}
