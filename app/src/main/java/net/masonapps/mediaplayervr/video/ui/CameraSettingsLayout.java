package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.GlobalSettings;
import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 2/8/2017.
 */

public class CameraSettingsLayout extends SingleStageUi {

    private static final float STEP = 0.01f;
    private final VideoPlayerGUI videoPlayerGUI;
    private float z = 1f;
    private float ipd = 0f;

    public CameraSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        super(new VirtualStage(videoPlayerGUI.getSpriteBatch(), 360, 480), videoPlayerGUI.getSkin());
        this.videoPlayerGUI = videoPlayerGUI;
        stage.setPosition(0, 0.5f, -2.5f);
        stage.addActor(Style.newBackgroundImage(skin));

        final ImageButton closeButton = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        stage.addActor(closeButton);

        closeButton.setPosition(stage.getWidth() - padding, stage.getHeight() - padding, Align.topRight);
        table.padTop(closeButton.getHeight());
        table.setFillParent(true);
        table.center();

        final VideoOptions videoOptions = videoPlayerGUI.getVideoOptions();

        final ImageButton.ImageButtonStyle leftButtonStyle = Style.createImageButtonStyle(skin, Style.Drawables.ic_chevron_left_white_48dp, false);
        final ImageButton.ImageButtonStyle rightButtonStyle = Style.createImageButtonStyle(skin, Style.Drawables.ic_chevron_right_white_48dp, false);

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

        final TextButton zoomButton = new TextButton("Zoom", skin);
        zoomButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.showThumbSeekbarLayout(GlobalSettings.ZOOM);
            }
        });
        table.add(zoomButton).colspan(3).padBottom(padding).row();

        final TextButton brightnessButton = new TextButton("Brightness", skin);
        brightnessButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.showThumbSeekbarLayout(GlobalSettings.BRIGHTNESS);
            }
        });
        table.add(brightnessButton).colspan(3).padBottom(padding).row();

        final TextButton contrastButton = new TextButton("Contrast", skin);
        contrastButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.showThumbSeekbarLayout(GlobalSettings.CONTRAST);
            }
        });
        table.add(contrastButton).colspan(3).padBottom(padding).row();

        final TextButton saturationButton = new TextButton("Color Temperature", skin);
        saturationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.showThumbSeekbarLayout(GlobalSettings.COLOR_TEMPERATURE);
            }
        });
        table.add(saturationButton).colspan(3).padBottom(padding).row();

        final TextButton tintButton = new TextButton("Tint", skin);
        tintButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.showThumbSeekbarLayout(GlobalSettings.TINT);
            }
        });
        table.add(tintButton).colspan(3).padBottom(padding).row();

        setVisible(false);
    }
}
