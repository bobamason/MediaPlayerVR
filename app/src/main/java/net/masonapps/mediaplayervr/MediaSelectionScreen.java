package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.chooser.GridUiLayout;
import net.masonapps.mediaplayervr.chooser.ImageListLayout;
import net.masonapps.mediaplayervr.chooser.VideoListLayout;
import net.masonapps.mediaplayervr.media.ImageDetails;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaSelectionScreen extends MediaPlayerScreen implements DaydreamControllerInputListener {
    private final Context context;
    private final SpriteBatch spriteBatch;
    private VirtualStage stagePermissions;
    private VirtualStage stageMedia;
    private VrUiContainer container;
    private GridUiLayout<VideoDetails> layoutVideoList;
    private GridUiLayout<ImageDetails> layoutImageList;
    private ExecutorService executor;

    public MediaSelectionScreen(final Context context, VrGame game) {
        super(game);
        this.context = context;
        spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        container = new VrUiContainer();
        initStage();
        final boolean permissionGranted = isPermissionGranted();
        stageMedia.setVisible(permissionGranted);
        stagePermissions.setVisible(!permissionGranted);
        if (permissionGranted)
            showVideoList();
        setBackgroundColor(Color.SLATE);
    }

    private void initStage() {
        manageDisposable(spriteBatch);
        stageMedia = new VirtualStage(spriteBatch, 360, 540);
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
            }
        });

        layoutImageList = new ImageListLayout(context, skin, spriteBatch, executor);
        layoutImageList.attach(container);
        layoutImageList.setOnItemClickedListener(new GridUiLayout.OnGridItemClickedListener<ImageDetails>() {
            @Override
            public void onItemClicked(int index, ImageDetails obj) {
                mediaPlayerGame.displayImage(obj);
            }
        });

        manageDisposable(stageMedia);
        manageDisposable(stagePermissions);
        manageDisposable(layoutVideoList);

        stagePermissions.setPosition(0, 0, -2f);
        stageMedia.setPosition(-2f, 0, -1.6f);
        stageMedia.lookAt(getVrCamera().position, Vector3.Y);
        
//        stageSongList.setPosition(0, 0.5f, -3f);

        stagePermissions.addActor(Style.newBackgroundImage(skin));
        stageMedia.addActor(Style.newBackgroundImage(skin));

        addPermissionsTable();
        addStartTable();
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
                        stageMedia.setVisible(granted);
                        stagePermissions.setVisible(!granted);
                        if (granted)
                            showVideoList();
                    }
                });
            }
        });
        tablePermissions.add(okButton).expandX().center();
    }

    private void addStartTable() {
        final Table tableStart = new Table(skin);
        tableStart.setFillParent(true);
        stageMedia.addActor(tableStart);

        final ImageTextButton videosButton = new ImageTextButton(context.getString(R.string.videos), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_movie_white_48dp));
        videosButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isLoading()) {
                    setLoading(true);
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
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(videosButton).center().pad(6).row();

        final ImageTextButton photosButton = new ImageTextButton(context.getString(R.string.photos), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_movie_white_48dp));
        photosButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isLoading()) {
                    setLoading(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<ImageDetails> list = MediaUtils.getImageList(context);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    layoutImageList.clear();
                                    layoutImageList.getList().addAll(list);
                                    layoutImageList.displayList(0);
                                    showImageList();
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(photosButton).center().pad(6).row();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void show() {
        GdxVr.app.getGvrView().setNeckModelEnabled(true);
        GdxVr.app.getGvrView().setNeckModelFactor(1f);
        GdxVr.input.getDaydreamControllerHandler().addListener(this);
        GdxVr.input.setProcessor(container);
        getVrCamera().position.set(Vector3.Zero);
    }

    @Override
    public void hide() {
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setProcessor(null);
    }

    @Override
    public void update() {
        super.update();
        container.act();
        layoutVideoList.update();
        layoutImageList.update();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        container.draw(camera);
        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(camera.combined);
//        label3d.draw(spriteBatch);
        spriteBatch.end();
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
    public void onConnectionStateChange(int connectionState) {

    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
        if (event.button == DaydreamButtonEvent.BUTTON_APP) {
            if (event.action == DaydreamButtonEvent.ACTION_UP) {
            }
        }
    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
    }

    private boolean isPermissionGranted() {
        return ((MainActivity) context).isReadStoragePermissionGranted();
    }

    private void showVideoList() {
        stagePermissions.setVisible(false);
        layoutVideoList.setVisible(true);
        layoutImageList.setVisible(false);
    }

    private void showImageList() {
        stagePermissions.setVisible(false);
        layoutVideoList.setVisible(false);
        layoutImageList.setVisible(true);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
