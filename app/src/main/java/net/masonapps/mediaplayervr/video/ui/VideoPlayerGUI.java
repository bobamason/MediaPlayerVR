package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import net.masonapps.mediaplayervr.Icons;
import net.masonapps.mediaplayervr.VideoPlayerScreen;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;

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
    private float headerHeight = PADDING;
    private VirtualStage stage;

    public VideoPlayerGUI(VideoPlayerScreen videoPlayerScreen, Skin skin) {
        this.videoPlayerScreen = videoPlayerScreen;
        this.skin = skin;
        mainLayout = new MainLayout(this);
        modeLayout = new ModeLayout(this);
        stage = new VirtualStage(new SpriteBatch(), 2f, 2f, 640, 640);
        stage.set3DTransform(new Vector3(0, 0, -2.5f), videoPlayerScreen.getVrCamera().position);
        mainLayout.attach(stage);
        mainLayout.setVisible(true);
        modeLayout.attach(stage);
        modeLayout.setVisible(false);

        stage.getViewport().update(640, 420);

        final ImageButton backButton = new ImageButton(skin.newDrawable(Icons.ic_arrow_back_white_48dp, Color.WHITE), skin.newDrawable(Icons.ic_arrow_back_white_48dp, Color.LIGHT_GRAY));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stage.addActor(backButton);
        backButton.setPosition(PADDING, stage.getHeight() - PADDING, Align.topLeft);

        final ImageButton closeButton = new ImageButton(skin.newDrawable(Icons.ic_cancel_white_48dp, Color.WHITE), skin.newDrawable(Icons.ic_cancel_white_48dp, Color.LIGHT_GRAY));
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
        if (stage.isVisible()) {
            if (modeLayout.isVisible()) {
                modeLayout.setVisible(false);
                mainLayout.setVisible(true);
            }
        }
    }

    public void draw(Camera camera) {
        stage.draw(camera);
    }

    public void update() {
        stage.act();
        final VrVideoPlayer videoPlayer = videoPlayerScreen.getVideoPlayer();
        if (stage.isVisible() && mainLayout.isVisible() && videoPlayer.isPrepared()) {
            final long duration = videoPlayer.getDuration();
            mainLayout.timeLabel.setText(getTimeLabelString(videoPlayer.getCurrentPosition(), duration));
            if (duration != 0)
                mainLayout.slider.setValue((float) videoPlayer.getCurrentPosition() / duration);
        }
    }

    public VirtualStage getStage() {
        return stage;
    }

    public VideoPlayerScreen getVideoPlayerScreen() {
        return videoPlayerScreen;
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

    public void switchToModeLayout() {
        modeLayout.setVisible(true);
        mainLayout.setVisible(false);
    }
}
