package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.chooser.GridUiLayout;
import net.masonapps.mediaplayervr.chooser.VideoListLayout;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.ui.VirtualStage;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaSelectionScreen extends MediaPlayerScreen implements DaydreamControllerInputListener {
    private final Context context;
    private final SpriteBatch spriteBatch;
    //    private final VideoPreviewUi videoPreviewUi;
    private VirtualStage stagePermissions;
    private VrUiContainer container;
    private GridUiLayout<VideoDetails> layoutVideoList;
    private ExecutorService executor;

    public MediaSelectionScreen(final Context context, VrGame game) {
        super(game);
        this.context = context;
        spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        container = new VrUiContainer();
//        final ModelBuilder modelBuilder = new ModelBuilder();
//        final Model rectModel = ModelGenerator.createRect(modelBuilder);
//        final int divisionsU = 24;
//        final int divisionsV = 24;
//        final Model sphereModel = ModelGenerator.createSphere(modelBuilder, 0.5f, divisionsU * 2, divisionsV);
//        manageDisposable(rectModel, sphereModel);
//        videoPreviewUi = new VideoPreviewUi(new WeakReference<>(context), rectModel, sphereModel, spriteBatch, skin);
//        videoPreviewUi.setListener(new VideoPreviewUi.OnPlayClickedListener() {
//            @Override
//            public void onClicked(VideoDetails videoDetails) {
//                mediaPlayerGame.playVideo(videoDetails);
//            }
//        });
//        videoPreviewUi.attach(container);
//        videoPreviewUi.getImageDisplay().position.set(0f, 1f, -2.5f);
//        videoPreviewUi.getImageDisplay().setScale(1.5f);
//        videoPreviewUi.getPlayButton().setPosition(1f, 0f, -2.5f);
        initStage();
        final boolean permissionGranted = isPermissionGranted();
        stagePermissions.setVisible(!permissionGranted);
        if (permissionGranted) {
            showVideoList();
            loadVideoList();
        }
        setBackgroundColor(Color.SLATE);

        new Thread() {
            @Override
            public void run() {
                final String roomFileName = "room/room_textured.g3db";
                final ModelData modelData = ((G3dModelLoader) MediaSelectionScreen.this.game.getAssets().getLoader(Model.class, roomFileName)).loadModelData(GdxVr.files.internal(roomFileName));
                GdxVr.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        final Entity room = getWorld().add(new Entity(new ModelInstance(new Model(modelData))));
                        room.modelInstance.transform.rotate(Vector3.Y, -90);
                        room.setLightingEnabled(false);
                    }
                });
            }
        }.start();
    }

    private void initStage() {
        manageDisposable(spriteBatch);
        stagePermissions = new VirtualStage(spriteBatch, 720, 540);
        container.addProcessor(stagePermissions);
//        stageSongList = new VirtualStage(batch, 720, 420);

        executor = Executors.newCachedThreadPool();

        layoutVideoList = new VideoListLayout(context, skin, spriteBatch, executor);
        layoutVideoList.attach(container);
        layoutVideoList.setOnItemClickedListener(new GridUiLayout.OnGridItemClickedListener<VideoDetails>() {
            @Override
            public void onItemClicked(int index, VideoDetails obj) {
                mediaPlayerGame.playVideo(obj);
//                videoPreviewUi.load(mediaPlayerGame, obj);
            }
        });

        manageDisposable(container);
        manageDisposable(layoutVideoList);

        stagePermissions.setPosition(0.4f, 0, -2f);
        
        stagePermissions.addActor(Style.newBackgroundImage(skin));

        addPermissionsTable();
    }

    private void addPermissionsTable() {
        final Table tablePermissions = new Table(skin);
        tablePermissions.setFillParent(true);
        stagePermissions.addActor(tablePermissions);
        tablePermissions.add(context.getString(R.string.permission_explaination)).pad(12).center().row();
        final TextButton okButton = new TextButton(context.getString(android.R.string.ok), skin);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((MainActivity) context).requestReadStoragePermission(new MainActivity.StoragePermissionResultListener() {
                    @Override
                    public void onResult(boolean granted) {
                        stagePermissions.setVisible(!granted);
                        if (granted) {
                            showVideoList();
                            loadVideoList();
                        }
                    }
                });
            }
        });
        tablePermissions.add(okButton).expandX().center();
    }

    private void loadVideoList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<VideoDetails> list = MediaUtils.getVideoList(context);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        layoutVideoList.clear();
                        layoutVideoList.getList().addAll(list);
                        layoutVideoList.displayList(0);
                        showVideoList();
                    }
                });
            }
        }).start();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void show() {
        super.show();
        GdxVr.input.getDaydreamControllerHandler().addListener(this);
        GdxVr.input.setInputProcessor(container);
        getVrCamera().position.set(Vector3.Zero);
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setInputProcessor(null);
    }

    @Override
    public void update() {
        super.update();
        container.act();
        layoutVideoList.update();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        container.draw(camera);

//        getModelBatch().begin(camera);
//        videoPreviewUi.render(getModelBatch(), whichEye);
//        getModelBatch().end();
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            executor.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        super.onControllerButtonEvent(controller, event);
        if (event.button == DaydreamButtonEvent.BUTTON_APP) {
            if (event.action == DaydreamButtonEvent.ACTION_UP) {
            }
        }
    }

    private boolean isPermissionGranted() {
        return ((MainActivity) context).isReadStoragePermissionGranted();
    }

    private void showVideoList() {
        stagePermissions.setVisible(false);
        layoutVideoList.setVisible(true);
    }

    private void showImageList() {
        stagePermissions.setVisible(false);
        layoutVideoList.setVisible(false);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
