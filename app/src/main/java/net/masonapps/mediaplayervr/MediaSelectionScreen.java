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
import com.badlogic.gdx.math.Vector3;
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
import com.badlogic.gdx.utils.Scaling;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

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
    private static final int MAX_TITLE_LENGTH = 18;
    private static final float PADDING = 10f;
    private final Context context;
    private final Actor loadingSpinner;
    private final Object lock = new Object();
    //    private final Label3D label3d;
    private final SpriteBatch spriteBatch;
    private Table tableStart;
    private Table tableList;
    private List<VideoDetails> videoList = new ArrayList<>();
    private VirtualStage stageList;
    private VirtualStage stageStart;
    private VirtualStage stagePages;
    private VirtualStage stageBack;
    private VrInputMultiplexer inputMultiplexer;
    private int currentPage = 0;
    private int numPages = 0;
    private volatile int currentState = STATE_NO_LIST;
    private Label pageLabel;
    private ImageButton prevPageButon;
    private ImageButton nextPageButton;
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
        inputMultiplexer = new VrInputMultiplexer(stageList, stagePages, stageStart, stageBack);
        final Texture texture = new Texture("loading.png");
        manageDisposable(texture);
        loadingSpinner = new Image(texture);
        stageList.addActor(loadingSpinner);
        loadingSpinner.setZIndex(Integer.MAX_VALUE);
        loadingSpinner.setPosition(stageList.getWidth() / 2, stageList.getHeight() / 2, center);
        if (!((MainActivity) context).isReadStoragePermissionGranted()) {
            tableStart.setVisible(false);
            tablePermissions.setVisible(true);
        }
    }

    private static int getTotalPages(int itemsPerPage, List list) {
        return list.size() / itemsPerPage;
    }

    private void initStage() {
        final SpriteBatch batch = new SpriteBatch();
        manageDisposable(batch);
        stageStart = new VirtualStage(batch, 720, 540);
        stageList = new VirtualStage(batch, 720, 420);
        stagePages = new VirtualStage(batch, 720, 100);
        stageBack = new VirtualStage(batch, 100, 100);
        manageDisposable(stageStart);
        manageDisposable(stageList);
        manageDisposable(stagePages);
        manageDisposable(stageBack);
        stageStart.setPosition(0, 0, -3f);
        stageList.setPosition(0, 0.5f, -3f);
        stagePages.setPosition(0, -1f, -3f);
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        stageStart.addActor(bg);
        stagePages.addActor(bg);
        
        addPermissionsTable();
        addStartTable();
        addListTable();

        final ImageButton backButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, true));
        stageList.addActor(backButton);
        backButton.setPosition(PADDING, stageList.getViewport().getWorldHeight() - PADDING - backButton.getHeight());
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stageBack.getViewport().update((int) backButton.getWidth(), (int) backButton.getHeight(), false);
        stageBack.setPosition(0, -1.2f, -2f);
        stageBack.setRotation(Vector3.Z, Vector3.Y);
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
                                    stageStart.setVisible(false);
                                    stagePages.setVisible(true);
                                    stageList.setVisible(true);

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
        tableList = new Table(skin);
        tableList.setFillParent(true);
        stageList.addActor(tableList);

        final Table table = new Table(skin);
        table.setTouchable(Touchable.enabled);

        final Label label = new Label("", skin);
        label.setWrap(false);
        table.add(label).center();

        final Image image = new Image(defaultVideoDrawable);
        image.setScaling(Scaling.fit);
        image.setAlign(Align.center);
        image.setSize(stageList.getWidth() / 3f - PADDING * 6f, stageList.getHeight() / 2f - label.getStyle().font.getLineHeight() * 2 - PADDING * 4f);
        table.add(image).center().row();
        table.setBackground(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));

        final VideoListItemHolder holder = new VideoListItemHolder(table, image, label);
        holder.videoDetails = null;
        holders.add(holder);

        final Table tablePages = new Table(skin);
        tablePages.setFillParent(true);
        stagePages.addActor(tablePages);
        
        prevPageButon = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, true));
        prevPageButon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prevPagePressed();
            }
        });
        tablePages.add(prevPageButon).left().pad(PADDING);

        pageLabel = new Label("page 0/0", skin);
        tablePages.add(pageLabel).center();
        
        nextPageButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, true));
        nextPageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nextPagePressed();
            }
        });
        tablePages.add(nextPageButton).right().pad(PADDING);
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

                final VideoListItemHolder holder = holders.get(i);
                if (index >= videoList.size()) {
                    holder.image.setDrawable(defaultVideoDrawable);
                    holder.label.setText("");
                    holder.videoDetails = null;
                    break;
                }

                final VideoDetails videoDetails = videoList.get(index);
                holder.videoDetails = videoDetails;
                holder.image.setDrawable(defaultVideoDrawable);
                holder.label.setText(getTruncatedTitle(videoDetails.title));

                holder.table.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        onListItemClicked(holder.videoDetails);
                    }
                });
                final Cell<Table> cell = tableList.add(holder.table).pad(PADDING).fill();
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
        GdxVr.input.setProcessor(inputMultiplexer);
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
        stageList.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        stageList.draw(camera);
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
        if (currentState == STATE_VIDEO_LIST) {
            stageStart.setVisible(true);
            stageList.setVisible(false);
            stagePages.setVisible(false);
            currentState = STATE_NO_LIST;
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
