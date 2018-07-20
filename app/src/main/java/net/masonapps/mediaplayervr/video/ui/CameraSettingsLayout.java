package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.vrinterface.SingleStageUi;

import java.text.DecimalFormat;

/**
 * Created by Bob on 2/8/2017.
 */

public class CameraSettingsLayout extends SingleStageUi {

    private static final float STEP = 0.01f;
    private final VideoPlayerGUI videoPlayerGUI;
    private float z = 1f;
    private DecimalFormat df = new DecimalFormat("0.00");
    private float ipd = 0f;

    public CameraSettingsLayout(final VideoPlayerGUI videoPlayerGUI) {
        super(videoPlayerGUI.getSpriteBatch(), videoPlayerGUI.getSkin());
        this.videoPlayerGUI = videoPlayerGUI;
        setPosition(0, 0, -1.2f);

        final VideoOptions videoOptions = videoPlayerGUI.getVideoOptions();

        ipd = videoOptions.ipd;
        videoPlayerGUI.getVideoPlayerScreen().setIpd(ipd);

        final TextButton ipdButton = new TextButton("Eye Spacing", skin);
        ipdButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_IPD);
                videoPlayerGUI.showSeekbar(videoOptions.ipd, VideoOptions.MIN_IPD, VideoOptions.MAX_IPD, (sliderLayout, value) -> {
                    final float ipd = MathUtils.lerp(VideoOptions.MIN_IPD, VideoOptions.MAX_IPD, value);
                    videoOptions.ipd = ipd;
                    sliderLayout.label.setText("Eye Spacing " + Math.round(ipd * 100) + "%");
                    videoPlayerGUI.getVideoPlayerScreen().setIpd(ipd);
                });
            }
        });
        table.add(ipdButton).padBottom(padding).row();

        final TextButton crossButton = new TextButton("Eye Angle", skin);
        crossButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_EYE_ANGLE);
                videoPlayerGUI.showSeekbar(videoOptions.eyeAngle, VideoOptions.MIN_EYE_ANGLE, VideoOptions.MAX_EYE_ANGLE, (sliderLayout, value) -> {
                    final float a = MathUtils.lerp(VideoOptions.MIN_EYE_ANGLE, VideoOptions.MAX_EYE_ANGLE, value);
                    videoOptions.eyeAngle = a;
                    sliderLayout.label.setText("Eye Angle " + df.format(a) + "deg");
                    videoPlayerGUI.getVideoPlayerScreen().setEyeAngle(a);
                });
            }
        });
        table.add(crossButton).padBottom(padding).row();

        final TextButton zoomButton = new TextButton("Zoom", skin);
        zoomButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.setCurrentSetting(VideoOptions.KEY_ZOOM);
                videoPlayerGUI.showSeekbar(videoOptions.zoom, VideoOptions.MIN_ZOOM, VideoOptions.MAX_ZOOM, (sliderLayout, value) -> {
                    final float z = MathUtils.lerp(VideoOptions.MIN_ZOOM, VideoOptions.MAX_ZOOM, value);
                    videoOptions.zoom = z;
                    sliderLayout.label.setText("Zoom " + Math.round(z * 100) + "%");
                    videoPlayerGUI.getVideoPlayerScreen().setZoom(z);
                });
            }
        });
        table.add(zoomButton).padBottom(padding).row();

        setVisible(false);
        setBackground(skin.newDrawable(Style.Drawables.window, Color.BLACK));
        resizeToFitTable();
    }
}
