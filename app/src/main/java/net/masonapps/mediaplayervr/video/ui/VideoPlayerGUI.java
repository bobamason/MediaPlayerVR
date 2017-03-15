package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VrUiContainer;

/**
 * Created by Bob on 2/8/2017.
 */

public class VideoPlayerGUI extends BaseUiLayout {
    public final ThumbSeekbarLayout thumbSeekbarLayout;
    private final MainLayout mainLayout;
    private final ModeLayout modeLayout;
    private final VideoPlayerScreen videoPlayerScreen;
    private final Skin skin;
    private final AspectRatioLayout aspectRatioLayout;
    private final CameraSettingsLayout cameraSettingsLayout;
    private final PlaybackSettingsLayout playbackSettingsLayout;
    private final SpriteBatch spriteBatch;
    private VideoOptions videoOptions;

    public VideoPlayerGUI(VideoPlayerScreen videoPlayerScreen, SpriteBatch spriteBatch, Skin skin, VideoOptions videoOptions) {
        this.videoPlayerScreen = videoPlayerScreen;
        this.skin = skin;
        this.videoOptions = videoOptions;
        this.spriteBatch = spriteBatch;

        mainLayout = new MainLayout(this);
        modeLayout = new ModeLayout(this);
        aspectRatioLayout = new AspectRatioLayout(this);
        cameraSettingsLayout = new CameraSettingsLayout(this);
        playbackSettingsLayout = new PlaybackSettingsLayout(this);
        
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout = new ThumbSeekbarLayout(spriteBatch, skin);
        thumbSeekbarLayout.stage.setPosition(0, -1f, -1.5f);
        thumbSeekbarLayout.stage.recalculateTransform();
        thumbSeekbarLayout.setVisible(false);
        thumbSeekbarLayout.setListener(new ThumbSeekbarLayout.OnThumbSeekListener() {
            @Override
            public void onSeekChanged(float value) {
                final float z = MathUtils.lerp(0f, 2f, value);
                thumbSeekbarLayout.label.setText("Zoom " + Math.round(z * 100) + "%");
                VideoPlayerGUI.this.videoPlayerScreen.setZoom(z);
            }
        });
    }

    @Override
    public void attach(VrUiContainer container) {
        mainLayout.attach(container);
        modeLayout.attach(container);
        aspectRatioLayout.attach(container);
        cameraSettingsLayout.attach(container);
        playbackSettingsLayout.attach(container);
        thumbSeekbarLayout.attach(container);
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            switchToMainLayout();
        } else {
            mainLayout.setVisible(false);
            modeLayout.setVisible(false);
            aspectRatioLayout.setVisible(false);
            cameraSettingsLayout.setVisible(false);
            playbackSettingsLayout.setVisible(false);
            thumbSeekbarLayout.setVisible(false);
        }
    }

    public void backButtonClicked() {
        if (modeLayout.isVisible() ||
                aspectRatioLayout.isVisible() ||
                cameraSettingsLayout.isVisible() ||
                playbackSettingsLayout.isVisible()) {
            switchToMainLayout();
        } else {
            videoPlayerScreen.exit();
        }
    }

    public VideoPlayerScreen getVideoPlayerScreen() {
        return videoPlayerScreen;
    }

    public VideoOptions getVideoOptions() {
        return videoOptions;
    }

    public Skin getSkin() {
        return skin;
    }

    @Override
    public void dispose() {
        mainLayout.dispose();
        modeLayout.dispose();
        aspectRatioLayout.dispose();
        cameraSettingsLayout.dispose();
        playbackSettingsLayout.dispose();
        thumbSeekbarLayout.dispose();
    }

    public void switchToMainLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout.setVisible(false);
    }

    public void switchToModeLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(true);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout.setVisible(false);
    }

    public void switchToAspectRatioLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(true);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout.setVisible(false);
    }

    public void switchToCameraSettingsLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(true);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout.setVisible(false);
    }

    public void switchToPlaybackSettingsLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(true);
        thumbSeekbarLayout.setVisible(false);
    }

    public void showThumbSeekbarLayout() {
        mainLayout.setVisible(false);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout.setVisible(true);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
