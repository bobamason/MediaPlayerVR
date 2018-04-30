package net.masonapps.mediaplayervr;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.database.VideoOptionsDatabaseHelper;
import net.masonapps.mediaplayervr.loaders.VideoModelLoader;
import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.utils.ModelGenerator;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaPlayerGame extends VrGame {
    public static final String ROOM_FILENAME = "room/dome_room.g3db";
    //    public static final String HIGHLIGHT_FILENAME = "room/dome_highlight.g3db";
//    public static final String FLOOR_FILENAME = "room/dome_floor.g3db";
    public static final String SPHERE_FILENAME = "sphere.vidmodel";
    public static final String CYLINDER_FILENAME = "cylinder.vidmodel";
    private final Context context;
    private Skin skin;
    //    private Entity roomEntity;
    private ModelBatch phongModelBatch;
    private Vector3 worldOffset = new Vector3(0, -1.2f, 0);
    private MediaSelectionScreen mediaSelectionScreen;
    private boolean waitingToPlayVideo = false;
    private Model sphereModel;
    private Model cylinderModel;
    private Model rectModel;

    public MediaPlayerGame(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void create() {
        super.create();
        setScreen(new LoadingScreen(this));
        rectModel = ModelGenerator.createRect(new ModelBuilder());
        skin = new Skin();
        loadAsset(Style.ATLAS_FILE, TextureAtlas.class);
        getAssetManager().setLoader(Model.class, "vidmodel", new VideoModelLoader(new InternalFileHandleResolver()));
        loadAsset(SPHERE_FILENAME, Model.class);
//        ElapsedTimer.getInstance().setLogEnabled(false);
//        GdxVr.graphics.setPostProcessingShader(new ShaderProgram(GdxVr.files.internal("shaders/stereo_debug.vertex.glsl"), GdxVr.files.internal("shaders/stereo_debug.fragment.glsl")));
    }

    @Override
    protected void doneLoading(AssetManager assets) {
        super.doneLoading(assets);
        Log.d(MediaPlayerGame.class.getSimpleName(), "doneLoading()");
        skin.addRegions(assets.get(Style.ATLAS_FILE, TextureAtlas.class));
        setupSkin();

        final ModelBuilder modelBuilder = new ModelBuilder();
        sphereModel = assets.get(SPHERE_FILENAME, Model.class);
        cylinderModel = ModelGenerator.createCylinder(modelBuilder, 0.5f, 64, 64);

        goToSelectionScreen();
    }

    @Override
    public void pause() {
        if (getScreen() instanceof VideoPlayerScreen) {
            final VideoPlayerScreen videoPlayerScreen = (VideoPlayerScreen) getScreen();
            getVideoOptionsDatabaseHelper().saveVideoOptions(videoPlayerScreen.getVideoOptions());
        }
        super.pause();
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        super.onDrawFrame(headTransform, leftEye, rightEye);
    }

    private Model createFloorModel(ModelBuilder modelBuilder) {
        final Material material = new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createAmbient(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.GRAY));
        return modelBuilder.createBox(4f, 0.2f, 4f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    private void setupSkin() {
        addFont();
        addSliderStyle();
        addButtonStyle();
        addLabelStyle();
        addImageButtons();
    }

    private void addFont() {
        skin.add(Style.DEFAULT, new BitmapFont(Gdx.files.internal(Style.FONT_FILE), skin.getRegion(Style.FONT_REGION)), BitmapFont.class);
    }

    private void addSliderStyle() {
        skin.add("default-horizontal", new Slider.SliderStyle(skin.newDrawable(Style.Drawables.slider, Style.COLOR_UP_2), skin.newDrawable(Style.Drawables.slider_knob, Style.COLOR_UP_2)), Slider.SliderStyle.class);
    }

    private void addButtonStyle() {
        final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = skin.getFont(Style.DEFAULT);
        textButtonStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        textButtonStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        textButtonStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        textButtonStyle.checked = null;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add(Style.DEFAULT, textButtonStyle, TextButton.TextButtonStyle.class);

        final TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle();
        toggleStyle.font = skin.getFont(Style.DEFAULT);
        toggleStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        toggleStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        toggleStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        toggleStyle.checked = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        toggleStyle.fontColor = Color.WHITE;
        skin.add(Style.TOGGLE, toggleStyle, TextButton.TextButtonStyle.class);

        final TextButton.TextButtonStyle listBtnStyle = new TextButton.TextButtonStyle();
        listBtnStyle.font = skin.getFont(Style.DEFAULT);
        listBtnStyle.up = skin.newDrawable(Style.Drawables.button, new Color(0, 0, 0, 0.84706f));
        listBtnStyle.over = skin.newDrawable(Style.Drawables.button, new Color(0.15f, 0.15f, 0.15f, 0.84706f));
        listBtnStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        listBtnStyle.checked = null;
        listBtnStyle.fontColor = Color.WHITE;
        skin.add(Style.LIST_ITEM, listBtnStyle, TextButton.TextButtonStyle.class);
    }

    private void addLabelStyle() {
        skin.add(Style.DEFAULT, new Label.LabelStyle(skin.getFont(Style.DEFAULT), Color.WHITE), Label.LabelStyle.class);
    }

    private void addImageButtons() {

    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
    }

    public void playVideo(final VideoDetails videoDetails) {
        if (!waitingToPlayVideo) {
            waitingToPlayVideo = true;
            new Thread(() -> {
                final VideoOptions videoOptions = getVideoOptionsDatabaseHelper().getVideoOptionsByTitle(videoDetails.title);
                Log.d(MediaPlayerGame.class.getSimpleName(), (videoOptions == null ? "no save video options found for " : "video options loaded for ") + videoDetails.title);
                GdxVr.app.postRunnable(() -> {
                    setScreen(new VideoPlayerScreen(MediaPlayerGame.this, context, videoDetails, videoOptions));
                    waitingToPlayVideo = false;
                });
            }).start();
        }
    }

    public void playMusic(List<SongDetails> songList, int index) {
//        setScreen(new DecalsPerformanceTest(this, context, songList, index));
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
    }

    public void goToSelectionScreen() {
        setCursorVisible(true);
        setControllerVisible(true);
        if (mediaSelectionScreen == null)
            mediaSelectionScreen = new MediaSelectionScreen(context, this);
        if (getScreen() instanceof VideoPlayerScreen) {
            final VideoPlayerScreen videoPlayerScreen = (VideoPlayerScreen) getScreen();
            getVideoOptionsDatabaseHelper().saveVideoOptions(videoPlayerScreen.getVideoOptions());
            setScreen(mediaSelectionScreen);
            videoPlayerScreen.dispose();
        } else {
            setScreen(mediaSelectionScreen);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (skin != null)
            skin.dispose();
        skin = null;

//        if (roomEntity != null)
//            roomEntity.dispose();
//        roomEntity = null;

        if (sphereModel != null)
            sphereModel.dispose();
        sphereModel = null;
        
        if (phongModelBatch != null)
            phongModelBatch.dispose();
        phongModelBatch = null;
    }

    public Skin getSkin() {
        return skin;
    }

    public Model getSphereModel() {
        return sphereModel;
    }

    public Model getCylinderModel() {
        return cylinderModel;
    }

    public VideoOptionsDatabaseHelper getVideoOptionsDatabaseHelper() {
        return ((MainActivity) context).getVideoOptionsDatabaseHelper();
    }

    public Model getRectModel() {
        return rectModel;
    }
}
