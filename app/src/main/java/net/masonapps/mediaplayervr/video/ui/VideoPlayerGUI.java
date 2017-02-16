package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.database.VideoOptions;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.Locale;

/**
 * Created by Bob on 2/8/2017.
 */

public class VideoPlayerGUI implements Disposable {
    public static final int PADDING = 10;
    private final MainLayout mainLayout;
    private final ModeLayout modeLayout;
    private final VideoPlayerScreen videoPlayerScreen;
    private final Skin skin;
    private final AspectRatioLayout aspectRatioLayout;
    private final CameraSettingsLayout cameraSettingsLayout;
    private final PlaybackSettingsLayout playbackSettingsLayout;
    private VideoOptions videoOptions;
    private float headerHeight = PADDING;
    private VirtualStage stage;

    public VideoPlayerGUI(VideoPlayerScreen videoPlayerScreen, Skin skin, VideoOptions videoOptions) {
        this.videoPlayerScreen = videoPlayerScreen;
        this.skin = skin;
        this.videoOptions = videoOptions;

        mainLayout = new MainLayout(this);
        modeLayout = new ModeLayout(this);
        aspectRatioLayout = new AspectRatioLayout(this);
        cameraSettingsLayout = new CameraSettingsLayout(this);
        playbackSettingsLayout = new PlaybackSettingsLayout(this);

        stage = new VirtualStage(new SpriteBatch(), 2f, 2f, 720, 720);
        stage.set3DTransform(new Vector3(0, -0.5f, -2.5f), videoPlayerScreen.getVrCamera().position);
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        stage.addActor(bg);
        
        mainLayout.attach(stage);
        mainLayout.setVisible(true);

        modeLayout.attach(stage);
        modeLayout.setVisible(false);

        aspectRatioLayout.attach(stage);
        aspectRatioLayout.setVisible(false);

        cameraSettingsLayout.attach(stage);
        cameraSettingsLayout.setVisible(false);

        playbackSettingsLayout.attach(stage);
        playbackSettingsLayout.setVisible(false);

        stage.getViewport().update(720, 640);

        final ImageButton backButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, false));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stage.addActor(backButton);
        backButton.setPosition(PADDING, stage.getHeight() - PADDING, Align.topLeft);

        final ImageButton closeButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                VideoPlayerGUI.this.videoPlayerScreen.setUiVisible(false);
            }
        });
        stage.addActor(closeButton);
        closeButton.setPosition(stage.getWidth() - PADDING, stage.getHeight() - PADDING, Align.topRight);

        headerHeight = Math.max(backButton.getHeight(), closeButton.getHeight()) + PADDING;
    }

    private static String getTimeLabelString(long currentPosition, long duration) {
        return String.format(Locale.ENGLISH,
                "%d:%02d:%02d / %d:%02d:%02d",
                currentPosition / 1000 / (60 * 60),
                (currentPosition / 1000 / 60) % 60,
                (currentPosition / 1000) % 60,
                duration / 1000 / (60 * 60),
                (duration / 1000 / 60) % 60,
                (duration / 1000) % 60);
    }

    public void setVisible(boolean visible) {
        stage.setVisible(visible);
    }

    public void backButtonClicked() {
        if (stage.isVisible() && !mainLayout.isVisible()) {
            switchToMainLayout();
        } else {
            videoPlayerScreen.exit();
        }
    }

    public void draw(Camera camera) {
        stage.draw(camera);
    }

    public void update() {
        stage.act(Math.min(GdxVr.graphics.getDeltaTime(), 0.0333333f));
        final VrVideoPlayer videoPlayer = videoPlayerScreen.getVideoPlayer();
        if (stage.isVisible() && mainLayout.isVisible() && videoPlayer.isPrepared()) {
            final long duration = videoPlayer.getDuration();
            mainLayout.timeLabel.setText(getTimeLabelString(videoPlayer.getCurrentPosition(), duration));
            if (duration != 0) {
                mainLayout.slider.setStepSize(1f / duration);
                mainLayout.slider.setValue((float) videoPlayer.getCurrentPosition() / duration);
            }
        }
    }

    public VirtualStage getStage() {
        return stage;
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

    public float getHeaderHeight() {
        return headerHeight;
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        stage = null;
    }

    public void switchToMainLayout() {
        mainLayout.setVisible(true);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToModeLayout() {
        mainLayout.setVisible(false);
        modeLayout.setVisible(true);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToAspectRatioLayout() {
        mainLayout.setVisible(false);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(true);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToCameraSettingsLayout() {
        mainLayout.setVisible(false);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(true);
        playbackSettingsLayout.setVisible(false);
    }

    public void switchToPlaybackSettingsLayout() {
        mainLayout.setVisible(false);
        modeLayout.setVisible(false);
        aspectRatioLayout.setVisible(false);
        cameraSettingsLayout.setVisible(false);
        playbackSettingsLayout.setVisible(true);
    }
}
