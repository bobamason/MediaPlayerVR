package net.masonapps.mediaplayervr.audiovisualization.tests;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.gfx.RepeatingMesh;
import net.masonapps.mediaplayervr.media.SongDetails;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 3/4/2017.
 */

public class OptimizedModelsVisualizer extends MusicVisualizerScreen {

    private static final Vector3 tmp = new Vector3();
    private static final float MODEL_Z = -10f;
    private final RelativePositionShader shader;
    private final ModelInstance modelInstance;
    private Array<ModelInstance> instances = new Array<>();
    private Environment enviroment;
    private ModelInstance starsInstance;
    private float t = 0;


    public OptimizedModelsVisualizer(VrGame game, Context context, List<SongDetails> songList, int index) {
        super(game, context, songList, index);
        setUiVisible(false);
        enviroment = new Environment();
        enviroment.set(ColorAttribute.createAmbient(Color.DARK_GRAY));
        DirectionalLight light = new DirectionalLight();
        light.setDirection(tmp.set(0.05f, -0.1f, -1f).nor());
        light.setColor(Color.WHITE);
        enviroment.add(light);

        final ModelBuilder modelBuilder = new ModelBuilder();

        shader = new RelativePositionShader();
        final Model model = modelBuilder.createBox(1.5f, 1.5f, 1.5f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        final Mesh mesh = new RepeatingMesh(model.meshes.get(0));
        modelBuilder.begin();
        modelBuilder.part("meshes", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.BLUE)));
        modelInstance = new ModelInstance(modelBuilder.end());
        modelInstance.transform.idt().translate(0, 0, MODEL_Z);

        int numStars = 2000;
        final Mesh stars = new Mesh(true, numStars * 3, numStars, VertexAttribute.Position());
        final float[] vertices = new float[numStars * 3];
        final short[] indices = new short[numStars];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
            final float a = MathUtils.random(0, MathUtils.PI2);
            final float r = MathUtils.random(10, 40);
            final float z = MathUtils.random(-1000, 40);
            vertices[i * 3] = r * MathUtils.cos(a);
            vertices[i * 3 + 1] = r * MathUtils.sin(a);
            vertices[i * 3 + 2] = z;
        }
        stars.setIndices(indices);
        stars.setVertices(vertices);

        modelBuilder.begin();
        modelBuilder.part("stars", stars, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        starsInstance = new ModelInstance(modelBuilder.end());
        manageDisposable(starsInstance.model);
    }

    @Override
    public void update() {
        super.update();
        final float dT = Math.min(Gdx.graphics.getDeltaTime(), 0.03333f);
        starsInstance.transform.setToTranslation(tmp.set(getVrCamera().position).scl(0.5f));
        t += dT;
        t %= MathUtils.PI2;
//        shader.setScale(Math.max(MathUtils.sin(t) + 1f, 1f));
//        shader.setScale(Math.max(MathUtils.sin(t), 0f));
        shader.setScale(MathUtils.sin(t) + 1f);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        getModelBatch().begin(camera);
        getModelBatch().render(starsInstance);
        getModelBatch().render(modelInstance, shader);
//        getModelBatch().render(boxes, enviroment);
        getModelBatch().end();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private static class RelativePositionShader extends BaseShader {

        protected final int u_projTrans = register(new Uniform("u_projTrans"));
        protected final int u_worldTrans = register(new Uniform("u_worldTrans"));
        protected final int u_modelView = register(new Uniform("u_modelView"));
        protected final int u_color = register(new Uniform("u_color"));
        protected final int u_lightPos = register(new Uniform("u_lightPos"));
        protected final int u_lightIntensity = register(new Uniform("u_lightIntensity"));
        protected final int u_time = register(new Uniform("u_time"));
        protected final int u_scale = register(new Uniform("u_scale"));
//        protected final int u_diffuseTexture = register(new Uniform("diffuseTexture"));

        protected final ShaderProgram program;
        //        public float time = 0f;
        public float scale = 1f;
        private PointLight light = new PointLight();
        private Matrix4 modelView = new Matrix4();
        private Matrix4 view = new Matrix4();

        public RelativePositionShader() {
            light.setPosition(Vector3.Zero);
            light.setIntensity(40f);
            program = new ShaderProgram(Gdx.files.internal("shaders/relative_position_vert.glsl"), Gdx.files.internal("shaders/relative_position_frag.glsl"));

            if (!program.isCompiled())
                throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
            String log = program.getLog();
            if (log.length() > 0) Gdx.app.error("ShaderTest", "Shader compilation log: " + log);
            init();
        }

        @Override
        public void init() {
            super.init(program, null);
        }

        @Override
        public int compareTo(Shader other) {
            return 0;
        }

        @Override
        public boolean canRender(Renderable instance) {
            return true;
//        return instance.material.has(ColorAttribute.Diffuse);
//        & instance.material.has(TimeAttribute.ID);
        }

        @Override
        public void begin(Camera camera, RenderContext context) {
            program.begin();
            context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
            context.setDepthMask(true);
//            context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            set(u_projTrans, camera.combined);
            view.set(camera.view);
        }

        @Override
        public void render(Renderable renderable) {
            set(u_worldTrans, renderable.worldTransform);
            modelView.set(renderable.worldTransform).mul(view);
            set(u_modelView, modelView);
            if (renderable.material.has(ColorAttribute.Diffuse)) {
                ColorAttribute colorAttr = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
                set(u_color, colorAttr.color);
            }
//            if (renderable.material.has(ColorAttribute.Ambient)) {
//                ColorAttribute colorAttr2 = (ColorAttribute) renderable.material.get(ColorAttribute.Ambient);
//                set(u_color2, colorAttr2.color);
//            }
//            if (renderable.material.has(TextureAttribute.Diffuse)) {
//                TextureAttribute texAttr = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
//                texAttr.textureDescription.texture.bind();
//                set(u_diffuseTexture, 0);
//            }

//            set(u_time, time);
            set(u_scale, scale);
            set(u_lightPos, light.position);
            set(u_lightIntensity, light.intensity);

            renderable.meshPart.render(program);
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        @Override
        public void end() {
            program.end();
        }

        @Override
        public void dispose() {
            super.dispose();
            program.dispose();
        }
    }
}
