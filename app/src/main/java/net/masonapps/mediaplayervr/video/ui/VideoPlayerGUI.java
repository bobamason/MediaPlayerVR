package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

/**
 * Created by Bob on 2/8/2017.
 */

public class VideoPlayerGUI extends BaseUiLayout {
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
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        mainLayout.attach(inputMultiplexer);
        modeLayout.attach(inputMultiplexer);
        aspectRatioLayout.attach(inputMultiplexer);
        cameraSettingsLayout.attach(inputMultiplexer);
        playbackSettingsLayout.attach(inputMultiplexer);
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

    public void draw(Camera camera) {
        mainLayout.draw(camera);
        modeLayout.draw(camera);
        aspectRatioLayout.draw(camera);
        cameraSettingsLayout.draw(camera);
        playbackSettingsLayout.draw(camera);
    }

    public void update() {
        mainLayout.update();
        modeLayout.update();
        aspectRatioLayout.update();
        cameraSettingsLayout.update();
        playbackSettingsLayout.update();
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
    }

    public void switchToMainLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToModeLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(true);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToAspectRatioLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(true);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToCameraSettingsLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(true);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToPlaybackSettingsLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(true);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
