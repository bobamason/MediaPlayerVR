package net.masonapps.mediaplayervr.chooser;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
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

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.badlogic.gdx.utils.Align.center;

/**
 * Created by Bob on 2/22/2017.
 */

public abstract class GridUiLayout<T> extends BaseUiLayout {

    private static final float LOADING_SPIN_SPEED = -360f;
    private static final int ITEMS_PER_PAGE = 6;
    private static final int MAX_TITLE_LENGTH = 18;
    private static final float PADDING = 10f;
    protected final Skin skin;
    private final Object lock = new Object();
    private final Actor loadingSpinner;
    private final WeakReference<Context> contextRef;
    private final ExecutorService executor;
    private Label pageLabel;
    private ImageButton prevPageButon;
    private ImageButton nextPageButton;
    private VirtualStage stagePages;
    private int currentPage = 0;
    private int numPages = 0;
    private VirtualStage stageList;
    private Table tableList;
    private List<T> list = new CopyOnWriteArrayList<>();
    private List<Texture> thumbnailTextures = new CopyOnWriteArrayList<>();
    private List<GridItemHolder<T>> holders = new CopyOnWriteArrayList<>();
    private boolean loading;
    private OnGridItemClickedListener<T> listener = null;

    public GridUiLayout(Context context, Skin skin, Batch batch) {
        contextRef = new WeakReference<>(context);
        executor = Executors.newCachedThreadPool();
        this.skin = skin;
        stageList = new VirtualStage(batch, 720, 420);
        stageList.setPosition(0, 0.5f, -3f);
        stagePages = new VirtualStage(batch, 720, 100);
        stagePages.setPosition(0, -0.5f, -3f);
        stagePages.addActor(Style.newBackgroundImage(skin));
        loadingSpinner = new Image(skin.newDrawable(Style.Drawables.loading_spinner));
        stageList.addActor(loadingSpinner);
        loadingSpinner.setZIndex(Integer.MAX_VALUE);
        loadingSpinner.setPosition(stageList.getWidth() / 2, stageList.getHeight() / 2, center);
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
        if (isVisible() && loading) {
            loadingSpinner.setOrigin(Align.center);
            loadingSpinner.rotateBy(GdxVr.graphics.getDeltaTime() * LOADING_SPIN_SPEED);
            loadingSpinner.setVisible(true);
        } else {
            loadingSpinner.setVisible(false);
        }
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
        try {
            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        disposeTextures();
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

            final Image image = new Image();
            image.setScaling(Scaling.fit);
            image.setAlign(Align.center);
            table.add(image).width(stageList.getWidth() / 3f - PADDING * 6f).height(stageList.getHeight() / 2f - label.getStyle().font.getLineHeight() * 2 - PADDING * 4f).center().row();
            table.setBackground(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));

            table.add(label).center();

            final GridItemHolder<T> holder = createHolder(table, image, label);
            holder.reset();
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

    protected abstract GridItemHolder<T> createHolder(Table table, Image image, Label label);

    private void prevPagePressed() {
        if (loading) return;
        try {
            if (currentPage > 0) {
                displayList(currentPage - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextPagePressed() {
        if (loading) return;
        try {
            if (currentPage != -1 && currentPage < numPages - 1) {
                displayList(currentPage + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayList(int page) {
        synchronized (lock) {
            loading = true;
            disposeTextures();

            thumbnailTextures.clear();
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

                final GridItemHolder<T> holder = holders.get(i);
                if (index >= list.size()) {
                    holder.reset();
                    break;
                }

                holder.bind(list.get(i));

                holder.table.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        onListItemClicked(index, holder.obj);
                    }
                });
                final Cell<Table> cell = tableList.add(holder.table).pad(PADDING).fill();
                if (i % 3 == 2) cell.row();
            }
        }
        loadThumbnailTextures();
    }

    protected void onListItemClicked(int index, T obj) {
        if (loading || listener == null) return;
        listener.onItemClicked(index, obj);
    }

    private void loadThumbnailTextures() {
        for (GridItemHolder<T> holder : holders) {
            executor.execute(new ThumbnailTask<>(this, holder));
        }
    }

    protected abstract Pixmap getImagePixmap(Context context, T obj);

    public boolean isLoading() {
        return loading;
    }

    public void setOnItemClickedListener(OnGridItemClickedListener<T> listener) {
        this.listener = listener;
    }

    public List<T> getList() {
        return list;
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

    public void clear() {
        list.clear();
        for (GridItemHolder<T> holder : holders) {
            holder.reset();
        }
        disposeTextures();
    }

    public interface OnGridItemClickedListener<T> {
        void onItemClicked(int index, T obj);
    }

    public static abstract class GridItemHolder<T> {
        public Table table;
        public Image image;
        public Label label;
        @Nullable
        public T obj = null;
        public Drawable defaultDrawable;

        GridItemHolder(Table table, Image image, Label label, Drawable defaultDrawable) {
            this.table = table;
            this.image = image;
            this.label = label;
            this.defaultDrawable = defaultDrawable;
        }

        @CallSuper
        public void reset() {
            obj = null;
            label.setText("");
            image.setDrawable(defaultDrawable);
        }

        @CallSuper
        public void bind(T newObj) {
            obj = newObj;
        }
    }

    private static class ThumbnailTask<T> implements Runnable {

        private GridUiLayout<T> gridUiLayout;
        private GridItemHolder<T> holder;

        private ThumbnailTask(GridUiLayout<T> gridUiLayout, GridItemHolder<T> holder) {
            this.gridUiLayout = gridUiLayout;
            this.holder = holder;
        }
        
        @Override
        public void run() {
            final Context context = gridUiLayout.contextRef.get();
            if (holder.obj == null) return;
            final Pixmap pixmap = gridUiLayout.getImagePixmap(context, holder.obj);
            if (pixmap == null) return;
            try {
                GdxVr.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        final Texture texture = new Texture(pixmap);
                        gridUiLayout.thumbnailTextures.add(texture);
                        holder.image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
