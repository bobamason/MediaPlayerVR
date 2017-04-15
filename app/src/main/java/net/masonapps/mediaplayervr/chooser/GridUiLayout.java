package net.masonapps.mediaplayervr.chooser;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;
import org.masonapps.libgdxgooglevr.ui.ImageButtonVR;
import org.masonapps.libgdxgooglevr.ui.LabelVR;
import org.masonapps.libgdxgooglevr.ui.TableVR;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Created by Bob on 2/22/2017.
 */

public abstract class GridUiLayout<T> extends BaseUiLayout {

    public static final Vector3 tmp = new Vector3();
    private static final float LOADING_SPIN_SPEED = -360f;
    private static final int COLUMNS = 3;
    private static final int ROWS = 2;
    private static final int MAX_TITLE_LENGTH = 18;
    private static final float PADDING = 10f;
    private static final float MIN_MOVEMENT = 0.15f;
    private static final float SENSITIVITY = 0.125f;
    private static final int ITEMS_PER_PAGE = COLUMNS * ROWS;
    protected final Skin skin;
    private final VrUiContainer container;
    private final Batch batch;
    private final Object lock = new Object();
    private final Actor loadingSpinner;
    private final WeakReference<Context> contextRef;
    private float downX;
    private LabelVR pageLabel;
    private ImageButtonVR prevPageButton;
    private ImageButtonVR nextPageButton;
    private int currentPage = 0;
    private int numPages = 0;
    private List<T> list = new CopyOnWriteArrayList<>();
    private List<Texture> thumbnailTextures = new CopyOnWriteArrayList<>();
    private List<GridItemHolder<T>> holders = new CopyOnWriteArrayList<>();
    private boolean loading;
    private OnGridItemClickedListener<T> listener = null;
    private ExecutorService executor;

    public GridUiLayout(Context context, Skin skin, Batch batch, ExecutorService executor) {
        container = new VrUiContainer();
        contextRef = new WeakReference<>(context);
        this.skin = skin;
        this.batch = batch;
        this.executor = executor;

        prevPageButton = new ImageButtonVR(batch, Style.createImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, true));
        prevPageButton.getViewport().update((int) prevPageButton.getImageButton().getWidth() + 8, 720, false);
        prevPageButton.getImageButton().center().pad(4).setFillParent(true);
        prevPageButton.position.set(0f, 0.25f, -2f).rotate(45f, 0, 1, 0);
        prevPageButton.lookAt(tmp.set(0, 0.25f, 0), Vector3.Y);
        prevPageButton.getImageButton().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prevPagePressed();
            }
        });
//        prevPageButton.lookAt(Vector3.Zero, Vector3.Y);

        nextPageButton = new ImageButtonVR(batch, Style.createImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, true));
        nextPageButton.getViewport().update((int) nextPageButton.getImageButton().getWidth() + 8, 720, false);
        nextPageButton.getImageButton().center().pad(4).setFillParent(true);
        nextPageButton.position.set(0f, 0.25f, -2f).rotate(-45f, 0, 1, 0);
        nextPageButton.lookAt(tmp.set(0, 0.25f, 0), Vector3.Y);
        nextPageButton.getImageButton().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                nextPagePressed();
            }
        });

        pageLabel = new LabelVR("Page 0/0", batch, skin);
        pageLabel.setPosition(0, -0.65f, -2f);
        
        addListTable();
        loadingSpinner = new Image(skin.newDrawable(Style.Drawables.loading_spinner));
//        loadingSpinner = new Image(new Texture("loading.png"));
//        stageList.addActor(loadingSpinner);
//        loadingSpinner.setZIndex(Integer.MAX_VALUE);
//        loadingSpinner.setPosition(stageList.getWidth() / 2, stageList.getHeight() / 2, center);
//        loadingSpinner.setVisible(false);
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
    public void attach(VrUiContainer container) {
        container.addProcessor(this.container);
        container.addProcessor(pageLabel);
        container.addProcessor(prevPageButton);
        container.addProcessor(nextPageButton);
    }

    @Override
    public boolean isVisible() {
        return container.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        container.setVisible(visible);
    }

    @Override
    @CallSuper
    public void dispose() {
        try {
            container.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            prevPageButton.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            nextPageButton.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        disposeTextures();
    }

    private void addListTable() {

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                final TableVR table = new TableVR(batch, skin, 420, 360);
                final float sweep = 80f;
                final float a = sweep / COLUMNS - sweep / COLUMNS * c;
                final float y = (table.getHeightWorld() + 0.05f) / ROWS + 0.25f - (table.getHeightWorld() + 0.05f) * r;
                final float z = -2;
                table.position.set(0, y, z).rotate(a, 0, 1, 0);
                table.lookAt(tmp.set(0, y, 0), Vector3.Y);
                table.getTable().setBackground(skin.newDrawable(Style.Drawables.window));
                
                final Label label = new Label("", skin);
                label.setWrap(false);

                final Image image = new Image();
                image.setScaling(Scaling.fit);
                image.setAlign(Align.center);

                table.getTable().add(image).width(table.getWidth() - PADDING * 2f).height(table.getHeight() - label.getStyle().font.getLineHeight() * 2f - PADDING * 2f).center().row();
                table.getTable().add(label).center();

                final GridItemHolder<T> holder = createHolder(table, image, label);
                holder.reset();
                holders.add(holder);
            }
        }
    }

    protected abstract GridItemHolder<T> createHolder(TableVR table, Image image, Label label);

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
//        synchronized (lock) {
        loading = true;
        disposeTextures();

        thumbnailTextures.clear();
        container.clearProcessors();
        container.setVisible(true);

        currentPage = page;

        numPages = getTotalPages(ITEMS_PER_PAGE, list);
        pageLabel.setText("page " + (page + 1) + "/" + numPages);

        if (currentPage == 0) prevPageButton.setVisible(false);
        else prevPageButton.setVisible(true);

        if (currentPage >= numPages - 1) nextPageButton.setVisible(false);
        else nextPageButton.setVisible(true);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            final int index = i + ITEMS_PER_PAGE * currentPage;

            final GridItemHolder<T> holder = holders.get(i);
            holder.reset();

            if (index >= list.size()) {
                break;
            }

            holder.bind(list.get(index));
            holder.table.getRoot().setTouchable(Touchable.enabled);
            holder.table.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onListItemClicked(index, holder.obj);
                }
            });
            holder.table.setVisible(true);
            container.addProcessor(holder.table);
        }
//        }
        loadThumbnailTextures();
    }

    protected void onListItemClicked(int index, T obj) {
        if (loading || listener == null) return;
        listener.onItemClicked(index, obj);
    }

    private void loadThumbnailTextures() {
        for (GridItemHolder<T> holder : holders) {
            executor.submit(new ThumbnailTask<>(this, holder));
        }
    }

    protected abstract Bitmap getImageBitmap(Context context, T obj);

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

    public void onTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                downX = event.x;
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                final float diff = event.x - downX;
                final float abs = Math.abs(diff);
                if (abs > MIN_MOVEMENT) {
                    if (listener != null) {
                        final float x = diff > 0 ? (diff - MIN_MOVEMENT) : (diff + MIN_MOVEMENT);
//                        slider.setValue(MathUtils.clamp(slider.getValue() + x * GdxVr.graphics.getDeltaTime() * SENSITIVITY, 0f, 1f));
                    }
                }
                break;
            case DaydreamTouchEvent.ACTION_UP:
                break;
        }
    }

    public interface OnGridItemClickedListener<T> {
        void onItemClicked(int index, T obj);
    }

    public static class GridItemHolder<T> {
        public TableVR table;
        public Image image;
        public Label label;
        public Drawable defaultDrawable;
        @Nullable
        public T obj = null;

        GridItemHolder(TableVR table, Image image, Label label, Drawable defaultDrawable) {
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
            if (context == null || Thread.interrupted()) {
                gridUiLayout.loading = false;
                return;
            }
            gridUiLayout.loading = true;
            if (holder.obj == null) {
                gridUiLayout.loading = false;
                return;
            }
            final Bitmap bitmap = gridUiLayout.getImageBitmap(context, holder.obj);
            if (bitmap == null) {
                gridUiLayout.loading = false;
                return;
            }
            try {
                GdxVr.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        final Texture texture = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
                        Gdx.gl.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                        Gdx.gl.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                        bitmap.recycle();
                        gridUiLayout.thumbnailTextures.add(texture);
                        holder.image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
                        gridUiLayout.loading = false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                gridUiLayout.loading = false;
            }
        }
    }
}
