package net.masonapps.mediaplayervr;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.media.AlbumDetails;
import net.masonapps.mediaplayervr.media.ArtistDetails;
import net.masonapps.mediaplayervr.media.MediaDetails;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaSelectionScreen extends VrWorldScreen {
    private static final int ITEMS_PER_PAGE = 6;
    private static final int STATE_NO_LIST = 0;
    private static final int STATE_MUSIC_ALBUM_LIST = 1;
    private static final int STATE_MUSIC_ARTIST_LIST = 2;
    private static final int STATE_MUSIC_SONG_LIST = 3;
    private static final int STATE_VIDEO_LIST = 4;
    private final Context context;
    private final Entity highlightEntity;
    private Skin skin;
    private Table tableStart;
    private Table tableMediaList;
    private Table tableSongList;
    private List<VideoDetails> videoList = new ArrayList<>();
    private List<SongDetails> songList = new ArrayList<>();
    private List<AlbumDetails> albumList = new ArrayList<>();
    private List<ArtistDetails> artistList = new ArrayList<>();
    private VirtualStage stage;
    private boolean isButtonClicked = false;
    private int currentPage = 0;
    private int numPages = 0;
    private volatile int currentState = STATE_NO_LIST;
    private ArrayList<ImageButton> imageButtons = new ArrayList<>();
    private ArrayList<TextButton> textButtons = new ArrayList<>();
    private ArrayList<Label> listLabels = new ArrayList<>();
    private ArrayList<Texture> thumbnailTextures = new ArrayList<>();
    private Label pageLabel;
    private ImageButton prevPageButon;
    private ImageButton nextPageButton;
    private ImageButton backButton;
    private volatile boolean loading = false;
    private Entity controllerEntity;
    private Table tablePermissions;
    private Drawable defaultVideoDrawable;
    private Drawable defaultAlbumDrawable;

    public MediaSelectionScreen(final Context context, VrGame game) {
        super(game);
        this.context = context;
        final MediaPlayerGame mediaPlayerGame = (MediaPlayerGame) game;
        skin = mediaPlayerGame.getSkin();
        setBackgroundColor(Color.NAVY);
        defaultAlbumDrawable = skin.newDrawable(Icons.ic_album_white_48dp);
        defaultVideoDrawable = skin.newDrawable(Icons.ic_album_white_48dp);
        initStage();
        getWorld().add(mediaPlayerGame.getRoomEntity());
        getWorld().add(mediaPlayerGame.getFloorEntity());
        highlightEntity = getWorld().add(mediaPlayerGame.getHighlightEntity());
        controllerEntity = getWorld().add(mediaPlayerGame.getControllerEntity());
        if (!((MainActivity) context).isReadStoragePermissionGranted()) {
            tableStart.setVisible(false);
            tablePermissions.setVisible(true);
        }
    }

    private static int getTotalPages(int itemsPerPage, List list) {
        return list.size() / itemsPerPage + 1;
    }

    private void initStage() {
        final SpriteBatch batch = new SpriteBatch();
        manageDisposable(batch);
        stage = new VirtualStage(batch, 2f, 1.5f, 640, 480);
        manageDisposable(stage);
        stage.set3DTransform(new Vector3(0, 0, -3f), getVrCamera().position);

        addPermissionsTable();
        addStartTable();
        addListTable();
//        addSongListTable();
    }

    private void addPermissionsTable() {
        tablePermissions = new Table();
        tablePermissions.setFillParent(true);
        stage.addActor(tablePermissions);
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
        tableStart = new Table();
        tableStart.setFillParent(true);
        stage.addActor(tableStart);
        final TextButton videosButton = new TextButton(context.getString(R.string.videos), skin);
        videosButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!loading) {
                    loading = true;
                    videoList.clear();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<VideoDetails> list = MediaUtils.getVideoList(context);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tableStart.setVisible(false);
                                    tableMediaList.setVisible(true);
                                    backButton.setVisible(true);

                                    videoList.addAll(list);
                                    displayVideoList(0);
                                    loading = false;
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(videosButton).center().pad(6);
        final TextButton musicButton = new TextButton(context.getString(R.string.music), skin);
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!loading) {
                    loading = true;
                    albumList.clear();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<AlbumDetails> list = MediaUtils.getAlbumList(context);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tableStart.setVisible(false);
                                    tableMediaList.setVisible(true);
                                    backButton.setVisible(true);

                                    albumList.addAll(list);
                                    displayAlbumList(0);
                                    loading = false;
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(musicButton).center().pad(6).row();
    }

    private void addListTable() {
        tableMediaList = new Table();
        tableMediaList.setFillParent(true);
        stage.addActor(tableMediaList);
        tableMediaList.setVisible(false);

        backButton = new ImageButton(skin.newDrawable(Icons.ic_arrow_back_white_48dp, Color.WHITE), skin.newDrawable(Icons.ic_arrow_back_white_48dp, Color.LIGHT_GRAY));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (tableMediaList.isVisible()) {
                    tableStart.setVisible(true);
                    tableMediaList.setVisible(false);
                    backButton.setVisible(false);
                }
            }
        });
        stage.addActor(backButton);
        backButton.setPosition(2, stage.getViewport().getWorldHeight() - 2 - backButton.getHeight());
        backButton.setVisible(false);

        tableMediaList.padTop(backButton.getHeight());
        
        textButtons.clear();
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {

            TextButton textButton = new TextButton("", skin);
            textButtons.add(textButton);

            final int index = i;
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onListItemClicked(index);
                }
            });
            tableMediaList.add(textButton).colspan(3).expandX().fillX().row();
        }
        
//        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
//
//            final Table table = new Table(skin);
//            table.setTouchable(Touchable.enabled);
//
//            final ImageButton imageButton = new ImageButton(skin.newDrawable(Icons.ic_album_white_48dp));
//            table.add(imageButton).center().row();
//            imageButtons.add(imageButton);
//
//            final Label label = new Label("", skin);
//            label.setWrap(false);
//            table.add(label).center();
//            listLabels.add(label);
//
//            final int index = i;
//            table.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    onListItemClicked(index);
//                }
//            });
//            final Cell<Table> cell = tableMediaList.add(table).fill();
//            if (i % 3 == 2) cell.row();
//        }
        addPageArrows(tableMediaList);
    }

//    private void addSongListTable() {
//        tableSongList = new Table();
//        tableSongList.setFillParent(true);
//        stage.addActor(tableSongList);
//        tableSongList.setVisible(false);
//
//        tableSongList.padTop(backButton.getHeight());
//        textButtons.clear();
//        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
//            
//            TextButton textButton = new TextButton("", skin);
//            textButtons.add(textButton);
//
//            final int index = i;
//            textButton.addListener(new ClickListener() {
//                @Override
//                public void clicked(InputEvent event, float x, float y) {
//                    onListItemClicked(index);
//                }
//            });
//            tableSongList.add(textButton).colspan(3).expandX().fillX().row();
//        }
//        
//        addPageArrows(tableSongList);
//    }
    
    private void addPageArrows(Table table) {
        prevPageButon = new ImageButton(skin.newDrawable(Icons.ic_chevron_left_white_48dp), skin.newDrawable(Icons.ic_chevron_left_white_48dp, Color.GRAY));
        prevPageButon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prevPagePressed();
            }
        });
        table.add(prevPageButon).left().pad(6);

        pageLabel = new Label("page 0/0", skin);
        tableMediaList.add(pageLabel).center();
        nextPageButton = new ImageButton(skin.newDrawable(Icons.ic_chevron_right_white_48dp), skin.newDrawable(Icons.ic_chevron_right_white_48dp, Color.GRAY));
        nextPageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nextPagePressed();
            }
        });
        table.add(nextPageButton).right().pad(6);
    }

    private void onListItemClicked(int index) {
        try {
            if (currentPage != -1) {
                switch (currentState) {
                    case STATE_MUSIC_ARTIST_LIST:
                        loadSongsByArtist(artistList.get(index + ITEMS_PER_PAGE * currentPage).artistId);
                        break;
                    case STATE_MUSIC_ALBUM_LIST:
                        loadSongsByAlbum(albumList.get(index + ITEMS_PER_PAGE * currentPage).albumId);
                        break;
                    case STATE_MUSIC_SONG_LIST:
                        ((MediaPlayerGame) game).playMusic(songList.get(index + ITEMS_PER_PAGE * currentPage));
                        break;
                    case STATE_VIDEO_LIST:
                        ((MediaPlayerGame) game).playVideo(videoList.get(index + ITEMS_PER_PAGE * currentPage));
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSongsByAlbum(final long albumId) {
        songList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<SongDetails> list = MediaUtils.getSongList(context, MediaStore.Audio.Media.ALBUM_ID + " = " + albumId);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        songList.addAll(list);
                        displaySongList(0);
                    }
                });
            }
        }).start();
    }

    private void loadSongsByArtist(final long artistId) {
        songList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<SongDetails> list = MediaUtils.getSongList(context, MediaStore.Audio.Media.ARTIST_ID + " = " + artistId);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        songList.addAll(list);
                        displaySongList(0);
                    }
                });
            }
        }).start();
    }

    private void prevPagePressed() {
        try {
            if (currentPage > 0) {
                switch (currentState) {
                    case STATE_MUSIC_ARTIST_LIST:
                        displayArtistList(currentPage - 1);
                        break;
                    case STATE_MUSIC_ALBUM_LIST:
                        displayAlbumList(currentPage - 1);
                        break;
                    case STATE_MUSIC_SONG_LIST:
                        displaySongList(currentPage - 1);
                        break;
                    case STATE_VIDEO_LIST:
                        displayVideoList(currentPage - 1);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextPagePressed() {
        try {
            if (currentPage != -1 && currentPage < numPages - 1) {
                switch (currentState) {
                    case STATE_MUSIC_ARTIST_LIST:
                        displayArtistList(currentPage + 1);
                        break;
                    case STATE_MUSIC_ALBUM_LIST:
                        displayAlbumList(currentPage + 1);
                        break;
                    case STATE_MUSIC_SONG_LIST:
                        displaySongList(currentPage + 1);
                        break;
                    case STATE_VIDEO_LIST:
                        displayVideoList(currentPage + 1);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayList(int page, int state, List<? extends MediaDetails> list, @Nullable Drawable defaultDrawable) {
        for (Texture texture : thumbnailTextures) {
            texture.dispose();
        }
        thumbnailTextures.clear();
        currentState = state;
        currentPage = page;
        numPages = getTotalPages(ITEMS_PER_PAGE, list);
        pageLabel.setText("page " + (page + 1) + "/" + numPages);
        if (currentPage == 0) prevPageButon.setDisabled(true);
        else prevPageButon.setDisabled(false);
        if (currentPage >= numPages - 1) nextPageButton.setDisabled(true);
        else nextPageButton.setDisabled(false);
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            final int index = i + ITEMS_PER_PAGE * currentPage;
            if(defaultDrawable == null){
                final TextButton textButton = textButtons.get(i);
                if (index >= list.size()) {
                    textButton.setVisible(false);
                    textButton.setText("");
                } else {
                    textButton.setVisible(true);
                    textButton.setText(list.get(index).title);
                }
            }else {
                final ImageButton imageButton = imageButtons.get(i);
                final Label label = listLabels.get(i);
                if (index >= list.size()) {
                    label.setVisible(false);
                    label.setText("");
                    imageButton.setVisible(false);
                    imageButton.getImage().setDrawable(defaultDrawable);
                } else {
                    label.setVisible(true);
                    label.setText(list.get(index).title);
                    imageButton.setVisible(true);
                    final String thumbnailPath = list.get(i).thumbnailPath;
                    if (thumbnailPath != null) {
                        final Texture texture = new Texture(GdxVr.files.external(thumbnailPath));
                        thumbnailTextures.add(texture);
                        imageButton.getImage().setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
                    } else {
                        imageButton.getImage().setDrawable(defaultDrawable);
                    }
                }
            }
        }
    }

    private void displayVideoList(int page) {
        displayList(page, STATE_VIDEO_LIST, videoList, null);
    }

    private void displaySongList(int page) {
        displayList(page, STATE_MUSIC_SONG_LIST, songList, null);
    }

    private void displayArtistList(int page) {
        displayList(page, STATE_MUSIC_ARTIST_LIST, artistList, null);
    }

    private void displayAlbumList(int page) {
        displayList(page, STATE_MUSIC_ALBUM_LIST, albumList, null);
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
        GdxVr.input.setProcessor(stage);
    }

    @Override
    public void hide() {
        GdxVr.input.setProcessor(null);
        dispose();
    }

    @Override
    public void update() {
        super.update();
        stage.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        stage.draw(camera);
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        super.onDaydreamControllerUpdate(controller, connectionState);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (imageButtons != null)
            imageButtons.clear();
        if (videoList != null)
            videoList.clear();
        if (albumList != null)
            albumList.clear();
        if (artistList != null)
            artistList.clear();
        if (songList != null)
            songList.clear();
        try {
            for (Texture texture : thumbnailTextures) {
                texture.dispose();
            }
            thumbnailTextures.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
