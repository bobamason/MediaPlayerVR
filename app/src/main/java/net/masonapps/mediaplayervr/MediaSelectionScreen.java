package net.masonapps.mediaplayervr;

import android.content.Context;
import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.utils.Align.center;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaSelectionScreen extends MediaPlayerScreen implements DaydreamControllerInputListener {

    private static final float LOADING_SPIN_SPEED = -360f;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int STATE_NO_LIST = 0;
    private static final int STATE_VIDEO_LIST = 1;
    private static final int MAX_TITLE_LENGTH = 20;
    private final Context context;
    private final Actor loadingSpinner;
    private final Object lock = new Object();
    //    private final Label3D label3d;
    private final SpriteBatch spriteBatch;
    private Table tableStart;
    private Table tableList;
    private Table tableMedia;
    private List<VideoDetails> videoList = new ArrayList<>();
    private VirtualStage stage;
    private int currentPage = 0;
    private int numPages = 0;
    private volatile int currentState = STATE_NO_LIST;
    private Label pageLabel;
    private ImageButton prevPageButon;
    private ImageButton nextPageButton;
    private ImageButton backButton;
    private volatile boolean loading = false;
    private Table tablePermissions;
    private Drawable defaultVideoDrawable;
    private ArrayList<Texture> thumbnailTextures = new ArrayList<>();
    private ArrayList<VideoListItemHolder> holders = new ArrayList<>();

    public MediaSelectionScreen(final Context context, VrGame game) {
        super(game);
        this.context = context;
        spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
//        label3d = new Label3D("Test Label", skin.get(Style.DEFAULT, Label.LabelStyle.class));
//        label3d.setFontSize(0.1f);
//        label3d.setPosition(-2f, 2f, -4f);
//        label3d.setAlignment(Align.center);
        setBackgroundColor(Color.DARK_GRAY);
        defaultVideoDrawable = skin.newDrawable(Style.Drawables.ic_movie_white_48dp);
        initStage();
        final Texture texture = new Texture("loading.png");
        manageDisposable(texture);
        loadingSpinner = new Image(texture);
        stage.addActor(loadingSpinner);
        loadingSpinner.setPosition(stage.getWidth() / 2, stage.getHeight() / 2, center);
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
        stage = new VirtualStage(batch, 720, 540);
        manageDisposable(stage);
        stage.setPosition(0, 0, -3f);
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        stage.addActor(bg);

        backButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, false));
        stage.addActor(backButton);
        backButton.setPosition(2, stage.getViewport().getWorldHeight() - 2 - backButton.getHeight());
        backButton.setVisible(false);

        addPermissionsTable();
        addStartTable();
        addListTable();
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
    }

    private void addPermissionsTable() {
        tablePermissions = new Table(skin);
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
        tableStart = new Table(skin);
        tableStart.setFillParent(true);
        stage.addActor(tableStart);
        final TextButton videosButton = new TextButton(context.getString(R.string.videos), skin);
        videosButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!loading) {
                    loading = true;
                    videoList.clear();
                    holders.clear();
                    disposeTextures();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final List<VideoDetails> list = MediaUtils.getVideoList(context);
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    tableStart.setVisible(false);
                                    tableMedia.setVisible(true);
                                    backButton.setVisible(true);

                                    videoList.addAll(list);
                                    displayList(0);
                                    loading = false;
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(videosButton).center().pad(6);
    }

    private void addListTable() {
        tableMedia = new Table(skin);
        tableMedia.setFillParent(true);
        stage.addActor(tableMedia);
        tableMedia.setVisible(false);

        tableList = new Table(skin);
        tableList.setFillParent(true);
        stage.addActor(tableList);

        tableMedia.padTop(backButton.getHeight());
        tableMedia.add(tableList).colspan(3).expand().fill().row();

        prevPageButon = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, true));
        prevPageButon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prevPagePressed();
            }
        });
        tableMedia.add(prevPageButon).left().pad(6);

        pageLabel = new Label("page 0/0", skin);
        tableMedia.add(pageLabel).center();
        nextPageButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, true));
        nextPageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nextPagePressed();
            }
        });
        tableMedia.add(nextPageButton).right().pad(6);
    }

    private void prevPagePressed() {
        if (loading) return;
        try {
            if (currentPage > 0 && currentState == STATE_VIDEO_LIST) {
                displayList(currentPage - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextPagePressed() {
        if (loading) return;
        try {
            if (currentPage != -1 && currentPage < numPages - 1 && currentState == STATE_VIDEO_LIST) {
                displayList(currentPage + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayList(int page) {
        synchronized (lock) {
            loading = true;
            for (Texture texture : thumbnailTextures) {
                texture.dispose();
            }

            thumbnailTextures.clear();
            holders.clear();
            tableList.clear();

            currentState = STATE_VIDEO_LIST;

            currentPage = page;

            numPages = getTotalPages(ITEMS_PER_PAGE, videoList);
            pageLabel.setText("page " + (page + 1) + "/" + numPages);

            if (currentPage == 0) prevPageButon.setDisabled(true);
            else prevPageButon.setDisabled(false);

            if (currentPage >= numPages - 1) nextPageButton.setDisabled(true);
            else nextPageButton.setDisabled(false);

            for (int i = 0; i < ITEMS_PER_PAGE; i++) {
                final int index = i + ITEMS_PER_PAGE * currentPage;
                if (index >= videoList.size())
                    break;

                final VideoDetails videoDetails = videoList.get(index);
                final Table table = new Table(skin);
                table.setTouchable(Touchable.enabled);

                final Image image = new Image(defaultVideoDrawable);
                table.add(image).center().row();

                final Label label = new Label(getTruncatedTitle(videoDetails.title), skin);
                label.setWrap(false);
                table.add(label).center();

                final VideoListItemHolder holder = new VideoListItemHolder(table, image, label);
                holder.videoDetails = videoDetails;
                holders.add(holder);

                table.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        onListItemClicked(holder.videoDetails);
                    }
                });
                final Cell<Table> cell = tableList.add(table).fill();
                if (i % 3 == 2) cell.row();
            }
        }
        loadThumbnailTextures();
    }

    private void onListItemClicked(VideoDetails videoDetails) {
        if (loading) return;
        try {
            ((MediaPlayerGame) game).playVideo(videoDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadThumbnailTextures() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    loading = true;
                    disposeTextures();
                    for (final VideoListItemHolder holder : holders) {
                        final Pixmap pixmap = MediaUtils.getVideoThumbnailPixmap(context, holder.videoDetails.id);
                        GdxVr.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                final Texture texture = new Texture(pixmap);
                                thumbnailTextures.add(texture);
                                holder.image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
                            }
                        });
                    }
                    loading = false;
                }
            }
        }).start();
    }

    @NonNull
    private String getTruncatedTitle(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
        }
        return title;
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
        GdxVr.input.setProcessor(stage);
    }

    @Override
    public void hide() {
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setProcessor(null);
    }

    @Override
    public void update() {
        super.update();
        if (loading) {
            loadingSpinner.setOrigin(Align.center);
            loadingSpinner.rotateBy(GdxVr.graphics.getDeltaTime() * LOADING_SPIN_SPEED);
            loadingSpinner.setVisible(true);
        } else {
            loadingSpinner.setVisible(false);
        }
//        label3d.rotateY(GdxVr.graphics.getDeltaTime() * LOADING_SPIN_SPEED);
        stage.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        stage.draw(camera);
        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(camera.combined);
//        label3d.draw(spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (holders != null)
            holders.clear();
        if (videoList != null)
            videoList.clear();
        disposeTextures();
    }

    private void disposeTextures() {
        if (thumbnailTextures != null) {
            for (Texture texture : thumbnailTextures) {
                try {
                    texture.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            thumbnailTextures.clear();
        }
    }

    private void backButtonClicked() {
        if (tableMedia.isVisible()) {
            tableStart.setVisible(true);
            tableMedia.setVisible(false);
            backButton.setVisible(false);
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

    private class VideoListItemHolder {
        Table table;
        Image image;
        Label label;
        VideoDetails videoDetails = null;

        VideoListItemHolder(Table table, Image image, Label label) {
            this.table = table;
            this.image = image;
            this.label = label;
        }
    }
}
