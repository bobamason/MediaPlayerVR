package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.shaders.HighlightShader;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaPlayerGame extends VrGame {
    public static final String SKIN_FILENAME = "skin/neon/neon-ui.json";
    public static final String SKIN_ATLAS_FILENAME = "skin/neon/neon-ui.atlas";
    public static final String ROOM_FILENAME = "room/dome_room.g3db";
    public static final String HIGHLIGHT_FILENAME = "room/dome_highlight.g3db";
    public static final String FLOOR_FILENAME = "room/dome_floor.g3db";
    public static final String CONTROLLER_FILENAME = "ddcontroller.g3db";
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

    public MediaPlayerGame(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void create() {
        super.create();
        loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);
        assets = new AssetManager();
        assets.load(Icons.buttons_pack, TextureAtlas.class);
        assets.load(SKIN_ATLAS_FILENAME, TextureAtlas.class);
        assets.load(SKIN_FILENAME, Skin.class, new SkinLoader.SkinParameter(SKIN_ATLAS_FILENAME));
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
                skin = assets.get(SKIN_FILENAME, Skin.class);
                skin.addRegions(assets.get(Icons.buttons_pack, TextureAtlas.class));
                final Model model = assets.get(ROOM_FILENAME, Model.class);
                model.materials.get(0).set(ColorAttribute.createDiffuse(Color.DARK_GRAY));
                roomEntity = new Entity(new ModelInstance(model, worldOffset));
                roomEntity.setLightingEnabled(true);
                highlightEntity = new Entity(new ModelInstance(assets.get(HIGHLIGHT_FILENAME, Model.class), worldOffset));
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

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
    }

    public void playVideo(VideoDetails videoDetails) {
        setScreen(new VideoPlayerScreen(this, context, videoDetails));
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

    public ModelBatch getPhongModelBatch() {
        return phongModelBatch;
    }
}
