package net.masonapps.mediaplayervr.chooser;

import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.masonapps.mediaplayervr.MediaPlayerGame;
import net.masonapps.mediaplayervr.MediaSelectionScreen;
import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.media.MediaUtils;
import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.utils.Align.center;

/**
 * Created by Bob on 2/22/2017.
 */

public class GridUiLayout extends BaseUiLayout {

    private static final float LOADING_SPIN_SPEED = -360f;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int MAX_TITLE_LENGTH = 18;
    private static final float PADDING = 10f;
    private final Skin skin;
    private final Object lock = new Object();
    private final Actor loadingSpinner;
    private Label pageLabel;
    private ImageButton prevPageButon;
    private ImageButton nextPageButton;
    private int currentPage = 0;
    private int numPages = 0;
    private VirtualStage stageList;
    private Table tableList;
    private List<VideoDetails> videoList = new ArrayList<>();
    private ArrayList<Texture> thumbnailTextures = new ArrayList<>();
    private ArrayList<VideoListItemHolder> holders = new ArrayList<>();
    private Drawable defaultVideoDrawable;

    public GridUiLayout(MediaSelectionScreen mediaSelectionScreen) {
        stageList = new VirtualStage(mediaSelectionScreen.getSpriteBatch(), 720, 420);
        skin = mediaSelectionScreen.getSkin();
        defaultVideoDrawable = skin.newDrawable(Style.Drawables.ic_movie_white_48dp);
        loadingSpinner = new Image(skin.newDrawable(Style.Drawables.loading_spinner));
        stageList.addActor(loadingSpinner);
        loadingSpinner.setZIndex(Integer.MAX_VALUE);
        loadingSpinner.setPosition(stageList.getWidth() / 2, stageList.getHeight() / 2, center);
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        stageList.setPosition(0, 0.5f, -3f);
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visible) {

    }

    @Override
    public void dispose() {

    }

    private void addListTable() {
        tableList = new Table(skin);
        tableList.setFillParent(true);
        stageList.addActor(tableList);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            final Table table = new Table(skin);
//            table.setFillParent(true);
            table.setTouchable(Touchable.enabled);

            final Label label = new Label("", skin);
            label.setWrap(false);

            final Image image = new Image(defaultVideoDrawable);
            image.setScaling(Scaling.fit);
            image.setAlign(Align.center);
            table.add(image).width(stageList.getWidth() / 3f - PADDING * 6f).height(stageList.getHeight() / 2f - label.getStyle().font.getLineHeight() * 2 - PADDING * 4f).center().row();
            table.setBackground(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));

            table.add(label).center();

            final VideoListItemHolder holder = new VideoListItemHolder(table, image, label);
            holder.videoDetails = null;
            holders.add(holder);
        }

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
        tablePages.add(pageLabel).center().expandX().fillX();

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
        stageBack.setVisible(true);
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
                        if (holder.videoDetails == null) continue;
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
