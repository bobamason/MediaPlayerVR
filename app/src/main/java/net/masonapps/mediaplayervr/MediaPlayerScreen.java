package net.masonapps.mediaplayervr;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;

/**
 * Created by Bob on 2/7/2017.
 */

public abstract class MediaPlayerScreen extends VrWorldScreen {
    private static final Vector3 tempV = new Vector3();
    protected final MediaPlayerGame mediaPlayerGame;
    //    protected final Entity roomEntity;
    protected final Skin skin;
    private final Quaternion tempQ = new Quaternion();
    private Vector3 startPosition = new Vector3(0, 0, -10);
    private Vector3 endPosition = new Vector3(0, 0, 10);

    public MediaPlayerScreen(VrGame game) {
        super(game);
        mediaPlayerGame = (MediaPlayerGame) game;
        skin = mediaPlayerGame.getSkin();
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        DirectionalLight light = new DirectionalLight();
        light.direction.set(0, -1, -0.5f).nor();
        lights.add(light);
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
    }

    private Model createModel(ModelBuilder modelBuilder) {
        Matrix4 transform = new Matrix4();
        modelBuilder.begin();
        final int n = 12;
        final float aStep = 360f / n;
        final float r = 5f;
        for (int i = 0; i < n; i++) {
            MeshPartBuilder builder = modelBuilder.part("", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material());
            transform.idt().translate(MathUtils.cosDeg(i * aStep) * r, MathUtils.sinDeg(i * aStep) * r, 0f).rotate(Vector3.Z, aStep * i).scale(0.5f, 0.5f, 0.5f);
            BoxShapeBuilder.build(builder, transform);
        }
        return modelBuilder.end();
    }

    @Override
    public void update() {
        super.update();

        final float dT = GdxVr.graphics.getDeltaTime() * 0.5f;
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
    }

    public Skin getSkin() {
        return skin;
    }

//    private static class Transform {
//        public final Vector3 position = new Vector3();
//        public final Vector3 axis = new Vector3();
//        public final Quaternion rotation = new Quaternion();
//        public final Vector3 scale = new Vector3();
//    }
}
