package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.masonapps.mediaplayervr.GlobalSettings;
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
    private int currentSetting = GlobalSettings.NONE;

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
                final GlobalSettings globalSettings = GlobalSettings.getInstance();
                switch (currentSetting) {
                    case GlobalSettings.ZOOM:
                        final float z = MathUtils.lerp(0f, 2f, value);
                        VideoPlayerGUI.this.videoOptions.zoom = z;
                        thumbSeekbarLayout.label.setText("Zoom " + Math.round(z * 100) + "%");
                        VideoPlayerGUI.this.videoPlayerScreen.setZoom(z);
                        break;
                    case GlobalSettings.TINT:
                        final float tint = MathUtils.lerp(GlobalSettings.MIN_TINT, GlobalSettings.MAX_TINT, value);
                        globalSettings.tint = tint;
                        thumbSeekbarLayout.label.setText("Tint");
                        VideoPlayerGUI.this.videoPlayerScreen.getVideoPlayer().getShader().setTint(tint);
                        break;
                    case GlobalSettings.BRIGHTNESS:
                        final float brightness = MathUtils.lerp(GlobalSettings.MIN_BRIGHTNESS, GlobalSettings.MAX_BRIGHTNESS, value);
                        globalSettings.brightness = brightness;
                        thumbSeekbarLayout.label.setText("Brightness");
                        VideoPlayerGUI.this.videoPlayerScreen.getVideoPlayer().getShader().setBrightness(brightness);
                        break;
                    case GlobalSettings.CONTRAST:
                        final float contrast = MathUtils.lerp(GlobalSettings.MIN_CONTRAST, GlobalSettings.MAX_CONTRAST, value);
                        globalSettings.contrast = contrast;
                        thumbSeekbarLayout.label.setText("Contrast");
                        VideoPlayerGUI.this.videoPlayerScreen.getVideoPlayer().getShader().setContrast(contrast);
                        break;
                    case GlobalSettings.SATURATION:
                        final float saturation = MathUtils.lerp(GlobalSettings.MIN_SATURATION, GlobalSettings.MAX_SATURATION, value);
                        globalSettings.saturation = saturation;
                        thumbSeekbarLayout.label.setText("Saturation");
                        VideoPlayerGUI.this.videoPlayerScreen.getVideoPlayer().getShader().setSaturation(saturation);
                        break;
                    default:
                        thumbSeekbarLayout.setVisible(false);
                        break;
                }
            }
        });
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

    public void showThumbSeekbarLayout(int setting) {
        currentSetting = setting;
        final GlobalSettings globalSettings = GlobalSettings.getInstance();
        switch (currentSetting) {
            case GlobalSettings.ZOOM:
                final float z = videoOptions.zoom;
                thumbSeekbarLayout.label.setText("Zoom " + Math.round(z * 100) + "%");
                thumbSeekbarLayout.slider.setValue(unLerp(VideoOptions.MIN_ZOOM, VideoOptions.MAX_ZOOM, z));
                break;
            case GlobalSettings.TINT:
                final float tint = globalSettings.tint;
                thumbSeekbarLayout.label.setText("Tint");
                thumbSeekbarLayout.slider.setValue(unLerp(GlobalSettings.MIN_TINT, GlobalSettings.MAX_TINT, tint));
                break;
            case GlobalSettings.BRIGHTNESS:
                final float brightness = globalSettings.brightness;
                thumbSeekbarLayout.label.setText("Brightness");
                thumbSeekbarLayout.slider.setValue(unLerp(GlobalSettings.MIN_BRIGHTNESS, GlobalSettings.MAX_BRIGHTNESS, brightness));
                break;
            case GlobalSettings.CONTRAST:
                final float contrast = globalSettings.contrast;
                thumbSeekbarLayout.label.setText("Contrast");
                thumbSeekbarLayout.slider.setValue(unLerp(GlobalSettings.MIN_CONTRAST, GlobalSettings.MAX_CONTRAST, contrast));
                break;
            case GlobalSettings.SATURATION:
                final float saturation = globalSettings.saturation;
                thumbSeekbarLayout.label.setText("Saturation");
                thumbSeekbarLayout.slider.setValue(unLerp(GlobalSettings.MIN_SATURATION, GlobalSettings.MAX_SATURATION, saturation));
                break;
            default:
                return;
        }
        mainLayout.setVisible(false);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
        thumbSeekbarLayout.setVisible(true);
    }

    public void hideThumbSeekbarLayout() {
        currentSetting = GlobalSettings.NONE;
        thumbSeekbarLayout.setVisible(false);
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
}
