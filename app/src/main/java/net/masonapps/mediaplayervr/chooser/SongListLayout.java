package net.masonapps.mediaplayervr.chooser;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.media.SongDetails;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bob on 2/22/2017.
 */

public class SongListLayout extends BaseUiLayout {

    private static final float LOADING_SPIN_SPEED = -360f;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int MAX_TITLE_LENGTH = 40;
    private static final float PADDING = 10f;
    protected final Skin skin;
    private Label pageLabel;
    private ImageButton prevPageButon;
    private ImageButton nextPageButton;
    private VirtualStage stagePages;
    private int currentPage = 0;
    private int numPages = 0;
    private VirtualStage stageList;
    private Table tableList;
    private List<SongDetails> list = new CopyOnWriteArrayList<>();
    private List<TextButton> textButtons = new ArrayList<>();
    private OnSongItemClickedListener listener = null;

    public SongListLayout(Skin skin, Batch batch) {
        this.skin = skin;
        stageList = new VirtualStage(batch, 720, 420);
        stageList.setPosition(0, 0.5f, -3f);
        stagePages = new VirtualStage(batch, 720, 100);
        stagePages.setPosition(0, -0.5f, -3f);
        stagePages.addActor(Style.newBackgroundImage(skin));
    }

    @NonNull
    public static String getTruncatedTitle(String title) {
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
        }
        return title;
    }

    private static int getTotalPages(int itemsPerPage, List list) {
        return list.size() / itemsPerPage + (list.size() % itemsPerPage == 0 ? 0 : 1);
    }

    @Override
    public void update() {
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
    @CallSuper
    public void dispose() {
    }

    private void addListTable() {
        tableList = new Table(skin);
        tableList.setFillParent(true);
        stageList.addActor(tableList);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            textButtons.add(new TextButton("", skin));
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
        tablePages.add(pageLabel).expandX().fillX().center();

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
        try {
            if (currentPage > 0) {
                displayList(currentPage - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextPagePressed() {
        try {
            if (currentPage != -1 && currentPage < numPages - 1) {
                displayList(currentPage + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayList(int page) {
        tableList.clear();

        currentPage = page;

        numPages = getTotalPages(ITEMS_PER_PAGE, list);
        pageLabel.setText("page " + (page + 1) + "/" + numPages);

        if (currentPage == 0) prevPageButon.setDisabled(true);
        else prevPageButon.setDisabled(false);

        if (currentPage >= numPages - 1) nextPageButton.setDisabled(true);
        else nextPageButton.setDisabled(false);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            final int index = i + ITEMS_PER_PAGE * currentPage;

            final SongDetails songDetails = list.get(i);
            final TextButton textButton = textButtons.get(i);
            textButton.setText(getTruncatedTitle(songDetails.title));

            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onListItemClicked(index, songDetails);
                }
            });
            tableList.add(textButton).pad(PADDING).expandX().fillX().row();
        }
    }

    protected void onListItemClicked(int index, SongDetails obj) {
        if (listener == null) return;
        listener.onItemClicked(index, obj);
    }

    public void setOnItemClickedListener(OnSongItemClickedListener listener) {
        this.listener = listener;
    }

    public List<SongDetails> getList() {
        return list;
    }

    public void clear() {
        list.clear();
    }

    public interface OnSongItemClickedListener {
        void onItemClicked(int index, SongDetails obj);
    }
}
