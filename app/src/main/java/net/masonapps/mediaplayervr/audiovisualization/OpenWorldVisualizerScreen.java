package net.masonapps.mediaplayervr.audiovisualization;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/10/2017.
 */

public class OpenWorldVisualizerScreen extends MusicVisualizerScreen {

    private static final String BOX = "box";
    private static final float PERIOD = 0.5f;
    private static final Matrix4 tmpM = new Matrix4();
    private static final Vector3 tmpV = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();
    private final Vector3 position = new Vector3();
    private final Vector3 velocity = new Vector3();
    private final Vector3 acceleration = new Vector3();
    private final Vector3 steeringForce = new Vector3();
    private ModelInstance starsInstance;
    private float spawnTime = 0f;
    private Array<Entity> boxes = new Array<>();
    private PointLight pointLight;

    public OpenWorldVisualizerScreen(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);

        final ModelBuilder modelBuilder = new ModelBuilder();
        final Model starsModel = createStarsModel(modelBuilder);
        manageDisposable(starsModel);
        starsInstance = new ModelInstance(starsModel);

        final Model boxModel = createBox(modelBuilder);
        manageDisposable(boxModel);
        getWorld().addConstructor(BOX, new Entity.EntityConstructor(boxModel));
        getVrCamera().far = 100f;
    }

    private static Model createBox(ModelBuilder modelBuilder) {
        final Material material = new Material(ColorAttribute.createDiffuse(Color.CYAN), ColorAttribute.createAmbient(Color.CYAN), ColorAttribute.createSpecular(Color.WHITE));
        return modelBuilder.createBox(1f, 1f, 1f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        pointLight = new PointLight();
        pointLight.setIntensity(10f);
        pointLight.setColor(Color.WHITE);
        lights.add(pointLight);
        final DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.set(Color.LIGHT_GRAY, new Vector3(0.1f, -1f, -0.1f).nor());
        lights.add(directionalLight);
    }

    private Model createStarsModel(ModelBuilder modelBuilder) {
        int numStars = 6000;
        final Mesh stars = new Mesh(true, numStars * 3, numStars, VertexAttribute.Position());
        final float[] vertices = new float[numStars * 3];
        final short[] indices = new short[numStars];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
//            final float a = MathUtils.random(0, MathUtils.PI2);
//            final float r = MathUtils.random(10, 40);
            final float z = MathUtils.random(-200, 200);
            final float x = MathUtils.random(-200, 200);
            final float y = MathUtils.random(-200, 200);
//            vertices[i * 3] = r * MathUtils.cos(a);
//            vertices[i * 3 + 1] = r * MathUtils.sin(a);
            vertices[i * 3] = x;
            vertices[i * 3 + 1] = y;
            vertices[i * 3 + 2] = z;
        }
        stars.setIndices(indices);
        stars.setVertices(vertices);

        modelBuilder.begin();
        modelBuilder.part("stars", stars, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        return modelBuilder.end();
    }

    @Override
    public void update() {
        super.update();
        final float dT = Math.min(Gdx.graphics.getDeltaTime(), 0.03333f);
//        acceleration.scl(0);
//        acceleration.add(steeringForce);
//        velocity.add(acceleration);
        velocity.add(getForwardVector()).scl(dT * 10f);
        position.add(velocity);
        getVrCamera().position.set(position);
        pointLight.setPosition(position);

        spawnTime += dT;
        if (spawnTime > PERIOD) {
            tmpV.set(getControllerRay().direction).scl(5f).add(position);
//        tmpV2.set(getRightVector()).rotate(getForwardVector(), MathUtils.random(360)).scl(3f);
            final float s = 0.5f * intensityValues[0] * 0.25f;
            tmpM.idt().translate(tmpV).rotate(getRightVector(), MathUtils.random(360)).scale(s, s, s);
            final Entity entity = getWorld().add(BOX, tmpM);
            entity.modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(new Color(0.25f, intensityValues[1], intensityValues[2], 1f)), ColorAttribute.createSpecular(Color.WHITE));
            boxes.add(entity);
            spawnTime = 0f;
        }
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        getModelBatch().render(starsInstance);
        getModelBatch().end();
    }
}
