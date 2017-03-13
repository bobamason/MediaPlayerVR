package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

/**
 * Created by Bob on 2/8/2017.
 */

public class CameraSettingsLayout extends BaseUiLayout {

    private static final float STEP = 0.01f;
    private final Table table;
    //    private final Window window;
    private final VideoPlayerGUI videoPlayerGUI;
    private VirtualStage stage;
    private Vector2 stretch = new Vector2();
    private float z = 1f;
    private float ipd = 0f;

    public CameraSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final Skin skin = videoPlayerGUI.getSkin();
        stage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 360, 360);
        stage.setPosition(0, 0, -2.5f);
        stage.addActor(Style.newBackgroundImage(skin));

        final ImageButton closeButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        stage.addActor(closeButton);

        closeButton.setPosition(stage.getWidth() - padding, stage.getHeight() - padding, Align.topRight);
        table = new Table(skin);
        table.padTop(closeButton.getHeight());
        table.setFillParent(true);
        table.center();

        final VideoOptions videoOptions = videoPlayerGUI.getVideoOptions();
        z = videoOptions.zoom;
        videoPlayerGUI.getVideoPlayerScreen().setZoom(z);
        final Label zoomLabel = new Label("Zoom " + Math.round(z * 100f) + "%", skin);

        final ImageButton.ImageButtonStyle leftButtonStyle = Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false);
        final ImageButton.ImageButtonStyle rightButtonStyle = Style.getImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false);

        final ImageButton zLeft = new ImageButton(leftButtonStyle);
        zLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                z -= STEP;
                videoPlayerGUI.getVideoPlayerScreen().setZ(z);
                videoOptions.zoom = z;
                zoomLabel.setText("Zoom " + Math.round(z * 100f) + "%");
            }
        });
        table.add(zLeft).pad(padding);

        table.add(zoomLabel).pad(padding);

        final ImageButton zRight = new ImageButton(rightButtonStyle);
        zRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                z += STEP;
                videoPlayerGUI.getVideoPlayerScreen().setZ(z);
                videoOptions.zoom = z;
                zoomLabel.setText("Zoom " + Math.round(z * 100f) + "%");
            }
        });
        table.add(zRight).pad(padding).row();

        videoPlayerGUI.getVideoPlayerScreen().setIpd(videoOptions.ipd);
        final Label ipdLabel = new Label("IPD " + Math.round(videoPlayerGUI.getVideoPlayerScreen().getIpd() * 100) + "%", skin);

        final ImageButton ipdLeft = new ImageButton(leftButtonStyle);
        ipdLeft.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ipd -= STEP * 5f;
                final VideoPlayerScreen screen = videoPlayerGUI.getVideoPlayerScreen();
                screen.setIpd(ipd);
                ipdLabel.setText("IPD " + Math.round(screen.getIpd() * 100) + "%");
                videoOptions.ipd = screen.getIpd();
            }
        });
        table.add(ipdLeft).pad(padding);
        table.add(ipdLabel).pad(padding);

        final ImageButton ipdRight = new ImageButton(rightButtonStyle);
        ipdRight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ipd += STEP * 5f;
                final VideoPlayerScreen screen = videoPlayerGUI.getVideoPlayerScreen();
                screen.setIpd(ipd);
                ipdLabel.setText("IPD " + Math.round(screen.getIpd() * 100) + "%");
                videoOptions.ipd = screen.getIpd();
            }
        });
        table.add(ipdRight).pad(padding).row();

        final Label camLabel = new Label("default camera", skin);
        final TextButton camButton = new TextButton("Use Custom Camera", skin, Style.TOGGLE);
        camButton.setChecked(videoOptions.useCustomCamera);
        camButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                videoPlayerGUI.getVideoPlayerScreen().setUseCustomCamera(camButton.isChecked());
                videoOptions.useCustomCamera = camButton.isChecked();
                camLabel.setText(camButton.isChecked() ? "custom camera" : "default camera");
            }
        });
        table.add(camButton).colspan(3).pad(padding).row();
        table.add(camLabel).colspan(3).center().pad(padding).row();
        setVisible(false);
    }

    @Override
    public void update() {
        stage.act();
    }

    @Override
    public void draw(Camera camera) {
        stage.draw(camera);
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        stage.addActor(table);
        inputMultiplexer.addProcessor(stage);
    }

    @Override
    public boolean isVisible() {
        return stage.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        stage.setVisible(visible);
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
    }
}
