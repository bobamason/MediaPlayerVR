package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.ui.VrUiContainer;


/**
 * Created by Bob on 2/8/2017.
 */

public class VideoPlayerGUI extends BaseUiLayout {
    public final SliderLayout sliderLayout;
    public final MainLayout mainLayout;
    private final ModeLayout modeLayout;
    private final VideoPlayerScreen videoPlayerScreen;
    private final Skin skin;
    private final AspectRatioLayout aspectRatioLayout;
    private final CameraSettingsLayout cameraSettingsLayout;
    private final ColorSettingsLayout colorSettingsLayout;
    private final PlaybackSettingsLayout playbackSettingsLayout;
    private final SpriteBatch spriteBatch;
    private VideoOptions videoOptions;
    private int currentSettingKey = VideoOptions.KEY_NONE;

    public VideoPlayerGUI(VideoPlayerScreen videoPlayerScreen, SpriteBatch spriteBatch, Skin skin, VideoOptions videoOptions) {
        this.videoPlayerScreen = videoPlayerScreen;
        this.skin = skin;
        this.videoOptions = videoOptions;
        this.spriteBatch = spriteBatch;

        mainLayout = new MainLayout(this);
        modeLayout = new ModeLayout(this);
        aspectRatioLayout = new AspectRatioLayout(this);
        cameraSettingsLayout = new CameraSettingsLayout(this);
        colorSettingsLayout = new ColorSettingsLayout(this);
        playbackSettingsLayout = new PlaybackSettingsLayout(this);

        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        sliderLayout = new SliderLayout(spriteBatch, skin);
        sliderLayout.setPosition(0, 0.75f, -1.2f);
        sliderLayout.recalculateTransform();
        sliderLayout.setVisible(false);
    }

    private static float unLerp(float fromValue, float toValue, float value) {
        return (value - fromValue) / (toValue - fromValue);
    }

    @Override
    public void update() {
        super.update();
        mainLayout.update();
    }

    @Override
    public void attach(VrUiContainer container) {
        mainLayout.attach(container);
        modeLayout.attach(container);
        aspectRatioLayout.attach(container);
        cameraSettingsLayout.attach(container);
        colorSettingsLayout.attach(container);
        playbackSettingsLayout.attach(container);
        sliderLayout.attach(container);
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
            currentSettingKey = VideoOptions.KEY_NONE;
            mainLayout.setVisible(false);
            modeLayout.setVisible(false);
            aspectRatioLayout.setVisible(false);
            cameraSettingsLayout.setVisible(false);
            colorSettingsLayout.setVisible(false);
            playbackSettingsLayout.setVisible(false);
            sliderLayout.setVisible(false);
        }
    }

    public void backButtonClicked() {
        if (modeLayout.isVisible() ||
                aspectRatioLayout.isVisible() ||
                cameraSettingsLayout.isVisible() ||
                colorSettingsLayout.isVisible() ||
                playbackSettingsLayout.isVisible() ||
                sliderLayout.isVisible()) {
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
        colorSettingsLayout.dispose();
        sliderLayout.dispose();
    }

    public void switchToMainLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        colorSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        sliderLayout.setVisible(false);
    }

    public void switchToModeLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        mainLayout.setVisible(true);
        modeLayout.setVisible(true);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        colorSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        sliderLayout.setVisible(false);
    }

    public void switchToAspectRatioLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(true);
        cameraSettingsLayout.setVisible(false);
        colorSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        sliderLayout.setVisible(false);
    }

    public void switchToCameraSettingsLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(true);
        colorSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        sliderLayout.setVisible(false);
    }

    public void switchToColorSettingsLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        colorSettingsLayout.setVisible(true);
        playbackSettingsLayout.setVisible(false);
        sliderLayout.setVisible(false);
    }

    public void switchToPlaybackSettingsLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        colorSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(true);
        sliderLayout.setVisible(false);
    }

    public void setCurrentSetting(int setting) {
        currentSettingKey = setting;
    }

    public void showSeekbar(float value, float min, float max, SliderLayout.OnThumbSeekListener listener) {
        sliderLayout.setListener(listener);
        sliderLayout.setSliderValue(unLerp(min, max, value));
        
        mainLayout.setVisible(false);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        colorSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        sliderLayout.setVisible(true);
    }

    public boolean isCursorOverSeekbar() {
        return mainLayout.isCursorOverSeekbar();
    }

    public int getCurrentSettingKey() {
        return currentSettingKey;
    }

    public void hideThumbSeekbarLayout() {
        currentSettingKey = VideoOptions.KEY_NONE;
        sliderLayout.setVisible(false);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
