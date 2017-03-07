package net.masonapps.mediaplayervr.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Created by Bob on 3/7/2017.
 */

public class BasicPhongShader extends BaseShader {

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
    private PointLight light = new PointLight();
    private Matrix4 modelView = new Matrix4();
    private Matrix4 view = new Matrix4();

    public BasicPhongShader() {
        light.setPosition(new Vector3(0, 0, 2));
        light.setIntensity(10f);
        program = new ShaderProgram(Gdx.files.internal("shaders/phong_vert.glsl"), Gdx.files.internal("shaders/phong_frag.glsl"));

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
        set(u_lightPos, light.position);
        set(u_lightIntensity, light.intensity);

        renderable.meshPart.render(program);
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
