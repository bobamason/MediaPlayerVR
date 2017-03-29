package net.masonapps.mediaplayervr;

import android.content.Context;
import android.provider.MediaStore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.chooser.AlbumListLayout;
import net.masonapps.mediaplayervr.chooser.ArtistListLayout;
import net.masonapps.mediaplayervr.chooser.GridUiLayout;
import net.masonapps.mediaplayervr.chooser.ImageListLayout;
import net.masonapps.mediaplayervr.chooser.SongListLayout;
import net.masonapps.mediaplayervr.chooser.VideoListLayout;
import net.masonapps.mediaplayervr.media.AlbumDetails;
import net.masonapps.mediaplayervr.media.ArtistDetails;
import net.masonapps.mediaplayervr.media.ImageDetails;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;

import java.util.List;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaSelectionScreen extends MediaPlayerScreen implements DaydreamControllerInputListener {
    private static final int STATE_NO_LIST = 0;
    private static final int STATE_VIDEO_LIST = 1;
    private static final int STATE_ALBUM_LIST = 2;
    private static final int STATE_ARTIST_LIST = 3;
    private static final int STATE_SONG_LIST = 4;
    private final Context context;
    //    private final Label3D label3d;
    private final SpriteBatch spriteBatch;
    private Table tableStart;
    private VirtualStage stageStart;
    private VirtualStage stageBack;
    private VrUiContainer container;
    private volatile int currentState = STATE_NO_LIST;
    private volatile int lastState = STATE_NO_LIST;
    private Table tablePermissions;
    private GridUiLayout<VideoDetails> layoutVideoList;
    private GridUiLayout<ImageDetails> layoutImageList;
    private GridUiLayout<AlbumDetails> layoutAlbumList;
    private GridUiLayout<ArtistDetails> layoutArtistList;
    private SongListLayout layoutSongList;

    public MediaSelectionScreen(final Context context, VrGame game) {
        super(game);
        this.context = context;
        spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
//        label3d = new Label3D("Test Label", skin.get(Style.DEFAULT, Label.LabelStyle.class));
//        label3d.setFontSize(0.1f);
//        label3d.setPosition(-2f, 2f, -4f);
//        label3d.setAlignment(Align.center);
        container = new VrUiContainer();
        initStage();
        switchToStartScreen();
        setBackgroundColor(Color.SLATE);
    }

    private void initStage() {
        final SpriteBatch batch = new SpriteBatch();
        manageDisposable(batch);
        stageStart = new VirtualStage(batch, 720, 540);
        container.addProcessor(stageStart);
//        stageSongList = new VirtualStage(batch, 720, 420);
        stageBack = new VirtualStage(batch, 100, 100);
        container.addProcessor(stageBack);

        layoutVideoList = new VideoListLayout(context, skin, spriteBatch);
        layoutVideoList.attach(container);
        layoutVideoList.setOnItemClickedListener(new GridUiLayout.OnGridItemClickedListener<VideoDetails>() {
            @Override
            public void onItemClicked(int index, VideoDetails obj) {
                mediaPlayerGame.playVideo(obj);
            }
        });

        layoutImageList = new ImageListLayout(context, skin, spriteBatch);
        layoutImageList.attach(container);
        layoutImageList.setOnItemClickedListener(new GridUiLayout.OnGridItemClickedListener<ImageDetails>() {
            @Override
            public void onItemClicked(int index, ImageDetails obj) {
                mediaPlayerGame.displayPicture(obj);
            }
        });

        layoutAlbumList = new AlbumListLayout(context, skin, spriteBatch);
        layoutAlbumList.attach(container);
        layoutAlbumList.setOnItemClickedListener(new GridUiLayout.OnGridItemClickedListener<AlbumDetails>() {
            @Override
            public void onItemClicked(int index, AlbumDetails obj) {
                final String projection = MediaStore.Audio.Media.ALBUM_ID + "=" + obj.albumId;
                if (!isLoading()) {
                    setLoading(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<SongDetails> list = MediaUtils.getSongList(context, projection);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    layoutSongList.clear();
                                    layoutSongList.getList().addAll(list);
                                    layoutSongList.displayList(0);
                                    switchToSongScreen();
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        layoutArtistList = new ArtistListLayout(context, skin, spriteBatch);
        layoutArtistList.attach(container);
        layoutArtistList.setOnItemClickedListener(new GridUiLayout.OnGridItemClickedListener<ArtistDetails>() {
            @Override
            public void onItemClicked(int index, ArtistDetails obj) {
                final String projection = MediaStore.Audio.Media.ARTIST_ID + "=" + obj.artistId;
                if (!isLoading()) {
                    setLoading(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<SongDetails> list = MediaUtils.getSongList(context, projection);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    layoutSongList.clear();
                                    layoutSongList.getList().addAll(list);
                                    layoutSongList.displayList(0);
                                    switchToSongScreen();
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        layoutSongList = new SongListLayout(skin, spriteBatch);
        layoutSongList.attach(container);
        layoutSongList.setOnItemClickedListener(new SongListLayout.OnSongItemClickedListener() {
            @Override
            public void onItemClicked(int index, SongDetails obj) {
                mediaPlayerGame.playMusic(layoutSongList.getList(), index);
            }
        });

        layoutVideoList.setVisible(false);
        layoutAlbumList.setVisible(false);
        layoutArtistList.setVisible(false);
        layoutSongList.setVisible(false);

        manageDisposable(stageStart);
        manageDisposable(stageBack);
        manageDisposable(layoutVideoList);
        manageDisposable(layoutAlbumList);
        manageDisposable(layoutArtistList);
        manageDisposable(layoutSongList);

        stageStart.setPosition(0, 0, -2f);
//        stageSongList.setPosition(0, 0.5f, -3f);

        stageStart.addActor(Style.newBackgroundImage(skin));

        addPermissionsTable();
        addStartTable();

        final ImageButton backButton = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, true));
        stageBack.addActor(backButton);
        backButton.setPosition(0, 0, Align.bottomLeft);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stageBack.getViewport().update((int) backButton.getWidth(), (int) backButton.getHeight(), false);
        stageBack.setPosition(0, -0.8f, -1.8f);
//        stageBack.setRotation(Vector3.Z, Vector3.Y);
        setBackButtonVisible(false);
    }

    private void setBackButtonVisible(boolean visible) {
        stageBack.setVisible(visible);
    }

    private void addPermissionsTable() {
        tablePermissions = new Table(skin);
        tablePermissions.setFillParent(true);
        stageStart.addActor(tablePermissions);
        tablePermissions.setVisible(false);
        final Label label = new Label(context.getString(R.string.permission_explaination), skin);
        tablePermissions.add(label).pad(12).row();
        final TextButton okButton = new TextButton(context.getString(android.R.string.ok), skin);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((MainActivity) context).requestReadStoragePermission(new MainActivity.StoragePermissionResultListener() {
                    @Override
                    public void onResult(boolean granted) {
                        tableStart.setVisible(granted);
                        tablePermissions.setVisible(!granted);
                    }
                });
            }
        });
        tablePermissions.add(okButton).expandX().center();
    }

    private void addStartTable() {
        tableStart = new Table(skin);
        tableStart.setFillParent(true);
        stageStart.addActor(tableStart);

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
                                    switchToVideoScreen();
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(videosButton).center().pad(6).row();

        final ImageTextButton pictureButton = new ImageTextButton(context.getString(R.string.pictures), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_movie_white_48dp));
        pictureButton.addListener(new ClickListener() {
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
                                    switchToPictureScreen();
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(pictureButton).center().pad(6).row();

        final ImageTextButton musicButton = new ImageTextButton(context.getString(R.string.music), Style.createImageTextButtonStyle(skin, Style.Drawables.ic_album_white_48dp));
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isLoading()) {
                    setLoading(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<AlbumDetails> list = MediaUtils.getAlbumList(context);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    layoutAlbumList.clear();
                                    layoutAlbumList.getList().addAll(list);
                                    layoutAlbumList.displayList(0);
                                    switchToAlbumScreen();
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(musicButton).center().pad(6);
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
//        label3d.rotateY(GdxVr.graphics.getDeltaTime() * LOADING_SPIN_SPEED);
        container.act();
        layoutVideoList.update();
        layoutAlbumList.update();
        layoutArtistList.update();
        layoutSongList.update();
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
//        if (holders != null)
//            holders.clear();
//        if (videoList != null)
//            videoList.clear();
//        disposeTextures();
    }

    private void backButtonClicked() {
        if (currentState != STATE_NO_LIST) {
            if (currentState == STATE_SONG_LIST) {
                if (lastState == STATE_ALBUM_LIST)
                    switchToAlbumScreen();
                else if (lastState == STATE_ARTIST_LIST)
                    switchToArtistScreen();
                else
                    switchToStartScreen();
            } else {
                switchToStartScreen();
            }
        }
    }

    @Override
    public void onConnectionStateChange(int connectionState) {

    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
        if (event.button == DaydreamButtonEvent.BUTTON_APP) {
            if (event.action == DaydreamButtonEvent.ACTION_UP) {
                backButtonClicked();
            }
        }
    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
    }

    private void switchToStartScreen() {
        lastState = STATE_NO_LIST;
        currentState = STATE_NO_LIST;
        stageBack.setVisible(false);
        stageStart.setVisible(true);
        if (!((MainActivity) context).isReadStoragePermissionGranted()) {
            tableStart.setVisible(false);
            tablePermissions.setVisible(true);
        }
        layoutVideoList.setVisible(false);
        layoutImageList.setVisible(false);
        layoutAlbumList.setVisible(false);
        layoutArtistList.setVisible(false);
        layoutSongList.setVisible(false);
    }

    private void switchToVideoScreen() {
        lastState = STATE_NO_LIST;
        currentState = STATE_VIDEO_LIST;
        stageBack.setVisible(true);
        stageStart.setVisible(false);
        layoutVideoList.setVisible(true);
        layoutImageList.setVisible(false);
        layoutAlbumList.setVisible(false);
        layoutArtistList.setVisible(false);
        layoutSongList.setVisible(false);
    }

    private void switchToPictureScreen() {
        lastState = STATE_NO_LIST;
        currentState = STATE_VIDEO_LIST;
        stageBack.setVisible(true);
        stageStart.setVisible(false);
        layoutVideoList.setVisible(false);
        layoutImageList.setVisible(true);
        layoutAlbumList.setVisible(false);
        layoutArtistList.setVisible(false);
        layoutSongList.setVisible(false);
    }

    private void switchToAlbumScreen() {
        lastState = STATE_NO_LIST;
        currentState = STATE_ALBUM_LIST;
        stageBack.setVisible(true);
        stageStart.setVisible(false);
        layoutVideoList.setVisible(false);
        layoutImageList.setVisible(false);
        layoutAlbumList.setVisible(true);
        layoutArtistList.setVisible(false);
        layoutSongList.setVisible(false);
    }

    private void switchToArtistScreen() {
        lastState = STATE_NO_LIST;
        currentState = STATE_ARTIST_LIST;
        stageBack.setVisible(true);
        stageStart.setVisible(false);
        layoutVideoList.setVisible(false);
        layoutImageList.setVisible(false);
        layoutAlbumList.setVisible(false);
        layoutArtistList.setVisible(true);
        layoutSongList.setVisible(false);
    }

    private void switchToSongScreen() {
        lastState = currentState;
        currentState = STATE_SONG_LIST;
        stageBack.setVisible(true);
        stageStart.setVisible(false);
        layoutVideoList.setVisible(false);
        layoutImageList.setVisible(false);
        layoutAlbumList.setVisible(false);
        layoutArtistList.setVisible(false);
        layoutSongList.setVisible(true);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
