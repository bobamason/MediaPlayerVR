package net.masonapps.mediaplayervr;

import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

import java.util.List;

/**
 * Created by Bob on 12/24/2016.
 */

public class MediaSelectionScreen extends MediaPlayerScreen implements DaydreamControllerInputListener {
    private static final int STATE_NO_LIST = 0;
    private static final int STATE_VIDEO_LIST = 1;
    private final Context context;
    //    private final Label3D label3d;
    private final SpriteBatch spriteBatch;
    private Table tableStart;
    private VirtualStage stageStart;
    private VirtualStage stagePages;
    private VirtualStage stageBack;
    private VrInputMultiplexer inputMultiplexer;
    private volatile int currentState = STATE_NO_LIST;
    private Table tablePermissions;

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
        initStage();
        inputMultiplexer = new VrInputMultiplexer(stagePages, stageStart, stageBack);
        if (!((MainActivity) context).isReadStoragePermissionGranted()) {
            tableStart.setVisible(false);
            tablePermissions.setVisible(true);
        }
    }

    private static int getTotalPages(int itemsPerPage, List list) {
        return list.size() / itemsPerPage + (list.size() % itemsPerPage == 0 ? 0 : 1);
    }

    private void initStage() {
        final SpriteBatch batch = new SpriteBatch();
        manageDisposable(batch);
        stageStart = new VirtualStage(batch, 720, 540);
        stageSongList = new VirtualStage(batch, 720, 420);
        stagePages = new VirtualStage(batch, 720, 100);
        stageBack = new VirtualStage(batch, 100, 100);

        manageDisposable(stageStart);
        manageDisposable(stagePages);
        manageDisposable(stageBack);

        stageStart.setPosition(0, 0, -3f);
        stageSongList.setPosition(0, 0.5f, -3f);
        stagePages.setPosition(0, -0.5f, -3f);

        stageStart.addActor(Style.newBackgroundImage(skin));
        stagePages.addActor(Style.newBackgroundImage(skin));
        
        stageList.setVisible(false);
        stageSongList.setVisible(false);
        stagePages.setVisible(false);

        addPermissionsTable();
        addStartTable();
        addListTable();

        final ImageButton backButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, true));
        stageBack.addActor(backButton);
        backButton.setPosition(0, 0, Align.bottomLeft);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stageBack.getViewport().update((int) backButton.getWidth(), (int) backButton.getHeight(), false);
        stageBack.setPosition(0, -0.8f, -2.8f);
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
        final TextButton videosButton = new TextButton(context.getString(R.string.videos), skin);
        videosButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isLoading()) {
                    setLoading(true);
                    videoList.clear();
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
                                    setLoading(false);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        tableStart.add(videosButton).center().pad(6);
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
        if (isLoading()) {
            loadingSpinner.setOrigin(Align.center);
            loadingSpinner.rotateBy(GdxVr.graphics.getDeltaTime() * LOADING_SPIN_SPEED);
            loadingSpinner.setVisible(true);
        } else {
            loadingSpinner.setVisible(false);
        }
//        label3d.rotateY(GdxVr.graphics.getDeltaTime() * LOADING_SPIN_SPEED);
        inputMultiplexer.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        inputMultiplexer.draw(camera);
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

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
