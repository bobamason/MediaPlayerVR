package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.database.VideoOptionsDatabaseHelper;
import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.shaders.HighlightShader;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaPlayerGame extends VrGame {
    public static final String ROOM_FILENAME = "room/dome_room.g3db";
    public static final String HIGHLIGHT_FILENAME = "room/dome_highlight.g3db";
    public static final String FLOOR_FILENAME = "room/dome_floor.g3db";
    public static final String CONTROLLER_FILENAME = "ddcontroller.g3db";
    private static final String HIGHLIGHT_TEXTURE_FILENAME = "room/tiled_bg.png";
    private final Context context;
    private Skin skin;
    private Entity roomEntity;
    private Entity highlightEntity;
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
        skin = new Skin();
        assets = new AssetManager();
        assets.load(Style.ATLAS_FILE, TextureAtlas.class);
        assets.load(HIGHLIGHT_TEXTURE_FILENAME, Texture.class);
        assets.load(ROOM_FILENAME, Model.class);
        assets.load(HIGHLIGHT_FILENAME, Model.class);
        assets.load(FLOOR_FILENAME, Model.class);
        assets.load(CONTROLLER_FILENAME, Model.class);
        loading = true;
    }

    @Override
    public void update() {
        super.update();
        if (loading) {
            if (assets.update()) {
                skin.addRegions(assets.get(Style.ATLAS_FILE, TextureAtlas.class));
                setupSkin();

                final Model model = assets.get(ROOM_FILENAME, Model.class);
                model.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK), ColorAttribute.createSpecular(Color.WHITE), FloatAttribute.createShininess(8f));
                roomEntity = new Entity(new ModelInstance(model, worldOffset));
                roomEntity.setLightingEnabled(true);

                final Model highlightModel = assets.get(HIGHLIGHT_FILENAME, Model.class);
                final Texture highlightTexture = assets.get(HIGHLIGHT_TEXTURE_FILENAME, Texture.class);
                highlightTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                highlightModel.materials.get(0).set(ColorAttribute.createDiffuse(Color.CYAN), TextureAttribute.createDiffuse(highlightTexture));
                highlightEntity = new Entity(new ModelInstance(highlightModel, worldOffset));
                highlightEntity.setLightingEnabled(false);
                highlightEntity.setShader(new HighlightShader());

                floorEntity = new Entity(new ModelInstance(assets.get(FLOOR_FILENAME, Model.class), worldOffset));
                floorEntity.setLightingEnabled(false);

                controllerEntity = new Entity(new ModelInstance(assets.get(CONTROLLER_FILENAME, Model.class)));
                controllerEntity.setLightingEnabled(false);

                goToSelectionScreen();
                loading = false;
            }
        }
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
        skin.add("default-horizontal", new Slider.SliderStyle(skin.newDrawable(Style.Drawables.slider, Style.COLOR_UP), skin.newDrawable(Style.Drawables.slider_knob, Style.COLOR_UP)), Slider.SliderStyle.class);
    }

    private void addButtonStyle() {
        final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = skin.getFont(Style.DEFAULT);
        textButtonStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
        textButtonStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        textButtonStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
        textButtonStyle.checked = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
        textButtonStyle.fontColor = Color.WHITE;
        skin.add(Style.DEFAULT, textButtonStyle, TextButton.TextButtonStyle.class);
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

    public void playMusic(SongDetails songDetails) {
        setScreen(new ParticlesVisualizerScreen(this, context, songDetails));
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
    }

    public void goToSelectionScreen() {
        if (mediaSelectionScreen == null)
            mediaSelectionScreen = new MediaSelectionScreen(context, this);
        if (getScreen() instanceof VideoPlayerScreen) {
            VideoPlayerScreen videoPlayerScreen = (VideoPlayerScreen) getScreen();
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
        if (roomEntity != null)
            roomEntity.dispose();
        roomEntity = null;
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

    public Entity getRoomEntity() {
        return roomEntity;
    }

    public Entity getHighlightEntity() {
        return highlightEntity;
    }

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
