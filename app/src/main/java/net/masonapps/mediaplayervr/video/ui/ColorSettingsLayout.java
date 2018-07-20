package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

/**
 * Created by Bob on 2/8/2017.
 */

public class ColorSettingsLayout extends SingleStageUi {

    private static final float STEP = 0.01f;
    private final VideoPlayerGUI videoPlayerGUI;
    private float z = 1f;
    private float ipd = 0f;

    public ColorSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        super(videoPlayerGUI.getSpriteBatch(), videoPlayerGUI.getSkin());
        this.videoPlayerGUI = videoPlayerGUI;
        final VideoOptions videoOptions = this.videoPlayerGUI.getVideoOptions();
        setPosition(0, 0, -1.2f);

        final TextButton brightnessButton = new TextButton("Brightness", skin);
        brightnessButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_BRIGHTNESS);
                videoPlayerGUI.showSeekbar(videoOptions.brightness, VideoOptions.MIN_BRIGHTNESS, VideoOptions.MAX_BRIGHTNESS, (sliderLayout, value) -> {
                    final float brightness = MathUtils.lerp(VideoOptions.MIN_BRIGHTNESS, VideoOptions.MAX_BRIGHTNESS, value);
                    videoOptions.brightness = brightness;
                    sliderLayout.label.setText("Brightness");
                    videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer().getShader().setBrightness(brightness);
                });
            }
        });
        table.add(brightnessButton).colspan(3).padBottom(padding).row();

        final TextButton contrastButton = new TextButton("Contrast", skin);
        contrastButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_CONTRAST);
                videoPlayerGUI.showSeekbar(videoOptions.contrast, VideoOptions.MIN_CONTRAST, VideoOptions.MAX_CONTRAST, (sliderLayout, value) -> {
                    final float contrast = MathUtils.lerp(VideoOptions.MIN_CONTRAST, VideoOptions.MAX_CONTRAST, value);
                    videoOptions.contrast = contrast;
                    sliderLayout.label.setText("Contrast");
                    videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer().getShader().setContrast(contrast);
                });
            }
        });
        table.add(contrastButton).colspan(3).padBottom(padding).row();

        final TextButton saturationButton = new TextButton("Color Temperature", skin);
        saturationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_COLOR_TEMPERATURE);
                videoPlayerGUI.showSeekbar(videoOptions.colorTemp, VideoOptions.MIN_COLOR_TEMP, VideoOptions.MAX_COLOR_TEMP, (sliderLayout, value) -> {
                    final float colorTemp = MathUtils.lerp(VideoOptions.MIN_COLOR_TEMP, VideoOptions.MAX_COLOR_TEMP, value);
                    videoOptions.colorTemp = colorTemp;
                    sliderLayout.label.setText("Color Temperature");
                    videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer().getShader().setColorTemp(colorTemp);
                });
            }
        });
        table.add(saturationButton).colspan(3).padBottom(padding).row();

        final TextButton tintButton = new TextButton("Tint", skin);
        tintButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_TINT);
                videoPlayerGUI.showSeekbar(videoOptions.tint, VideoOptions.MIN_TINT, VideoOptions.MAX_TINT, (sliderLayout, value) -> {
                    final float tint = MathUtils.lerp(VideoOptions.MIN_TINT, VideoOptions.MAX_TINT, value);
                    videoOptions.tint = tint;
                    sliderLayout.label.setText("Tint");
                    videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer().getShader().setTint(tint);
                });
            }
        });
        table.add(tintButton).colspan(3).padBottom(padding).row();

        setVisible(false);
        setBackground(skin.newDrawable(Style.Drawables.window, Color.BLACK));
        resizeToFitTable();
    }
}
