package net.masonapps.mediaplayervr;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;
import net.masonapps.mediaplayervr.audiovisualization.TrippyVisualizer;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.database.VideoOptionsDatabaseHelper;
import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.util.List;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaPlayerGame extends VrGame {
    public static final String ROOM_FILENAME = "room/dome_room.g3db";
    //    public static final String HIGHLIGHT_FILENAME = "room/dome_highlight.g3db";
    public static final String FLOOR_FILENAME = "room/dome_floor.g3db";
    public static final String CONTROLLER_FILENAME = "ddcontroller.g3db";
    private final Context context;
    private Skin skin;
    //    private Entity roomEntity;
    private Entity floorEntity;
    private Entity controllerEntity;
    private AssetManager assets;
    private boolean loading;
    private ModelBatch phongModelBatch;
    private Vector3 worldOffset = new Vector3(0, -1.2f, 0);
    private MediaSelectionScreen mediaSelectionScreen;
    private LoadingScreen loadingScreen;
    private boolean waitingToPlayVideo = false;

    public MediaPlayerGame(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void create() {
        super.create();
        loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);
        final GlobalSettings globalSettings = GlobalSettings.getInstance();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        globalSettings.tint = sharedPreferences.getFloat(GlobalSettings.KEY_TINT, globalSettings.tint);
        globalSettings.brightness = sharedPreferences.getFloat(GlobalSettings.KEY_BRIGHTNESS, globalSettings.brightness);
        globalSettings.contrast = sharedPreferences.getFloat(GlobalSettings.KEY_CONTRAST, globalSettings.contrast);
        globalSettings.colorTemp = sharedPreferences.getFloat(GlobalSettings.KEY_COLOR_TEMP, globalSettings.colorTemp);
        skin = new Skin();
        assets = new AssetManager();
        assets.load(Style.ATLAS_FILE, TextureAtlas.class);
//        assets.load(ROOM_FILENAME, Model.class);
//        assets.load(FLOOR_FILENAME, Model.class);
        assets.load(CONTROLLER_FILENAME, Model.class);
        loading = true;
    }

    @Override
    public void pause() {
        if (getScreen() instanceof VideoPlayerScreen) {
            final VideoPlayerScreen videoPlayerScreen = (VideoPlayerScreen) getScreen();
            getVideoOptionsDatabaseHelper().saveVideoOptions(videoPlayerScreen.getVideoOptions());
        }
        final GlobalSettings globalSettings = GlobalSettings.getInstance();
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putFloat(GlobalSettings.KEY_TINT, globalSettings.tint);
        editor.putFloat(GlobalSettings.KEY_BRIGHTNESS, globalSettings.brightness);
        editor.putFloat(GlobalSettings.KEY_CONTRAST, globalSettings.contrast);
        editor.putFloat(GlobalSettings.KEY_COLOR_TEMP, globalSettings.colorTemp);
        editor.apply();
        super.pause();
    }

    @Override
    public void update() {
        super.update();
        if (loading) {
            if (assets.update()) {
                skin.addRegions(assets.get(Style.ATLAS_FILE, TextureAtlas.class));
                setupSkin();

                final ModelBuilder modelBuilder = new ModelBuilder();

//                roomEntity = new Entity(new ModelInstance(createWorldModel(modelBuilder), worldOffset));
//                roomEntity.setLightingEnabled(true);

                floorEntity = new Entity(new ModelInstance(createFloorModel(modelBuilder), worldOffset));
                floorEntity.setLightingEnabled(true);

                controllerEntity = new Entity(new ModelInstance(assets.get(CONTROLLER_FILENAME, Model.class)));
                controllerEntity.setLightingEnabled(false);

                goToSelectionScreen();
                loading = false;
            }
        }
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final VideoOptions videoOptions = getVideoOptionsDatabaseHelper().getVideoOptionsByTitle(videoDetails.title);
                    Log.d(MediaPlayerGame.class.getSimpleName(), (videoOptions == null ? "no save video options found for " : "video options loaded for ") + videoDetails.title);
                    GdxVr.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            setScreen(new VideoPlayerScreen(MediaPlayerGame.this, context, videoDetails, videoOptions));
                            waitingToPlayVideo = false;
                        }
                    });
                }
            }).start();
        }
    }

    public void playMusic(List<SongDetails> songList, int index) {
        setScreen(new TrippyVisualizer(this, context, songList, index));
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
    }

    public void goToSelectionScreen() {
        if (mediaSelectionScreen == null)
            mediaSelectionScreen = new MediaSelectionScreen(context, this);
        if (getScreen() instanceof MusicVisualizerScreen) {
            MusicVisualizerScreen musicVisualizerScreen = (MusicVisualizerScreen) getScreen();
            setScreen(mediaSelectionScreen);
            musicVisualizerScreen.dispose();
            Log.d(MediaPlayerGame.class.getSimpleName(), "MusicVisualizerScreen disposed");
        } else if (getScreen() instanceof VideoPlayerScreen) {
            final VideoPlayerScreen videoPlayerScreen = (VideoPlayerScreen) getScreen();
            getVideoOptionsDatabaseHelper().saveVideoOptions(videoPlayerScreen.getVideoOptions());
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    getVideoOptionsDatabaseHelper().saveVideoOptions(videoPlayerScreen.getVideoOptions());
//                }
//            }).start();
            setScreen(mediaSelectionScreen);
            videoPlayerScreen.dispose();
            Log.d(MediaPlayerGame.class.getSimpleName(), "VideoPlayerScreen disposed");
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
        if (controllerEntity != null)
            controllerEntity.dispose();
        controllerEntity = null;
        if (phongModelBatch != null)
            phongModelBatch.dispose();
        phongModelBatch = null;
    }

    public Skin getSkin() {
        return skin;
    }

//    public Entity getRoomEntity() {
//        return roomEntity;
//    }

    public Entity getFloorEntity() {
        return floorEntity;
    }

    public Entity getControllerEntity() {
        return controllerEntity;
    }

    public VideoOptionsDatabaseHelper getVideoOptionsDatabaseHelper() {
        return ((MainActivity) context).getVideoOptionsDatabaseHelper();
    }
}
