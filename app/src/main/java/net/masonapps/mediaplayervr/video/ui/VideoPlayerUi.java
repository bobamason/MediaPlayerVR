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

import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 2/8/2017.
 */

public class VideoPlayerUI implements Disposable {
    public static final int PADDING = 6;
    private final MainLayout mainLayout;
    private final ModeLayout modeLayout;
    private final VideoPlayerScreen videoPlayerScreen;
    private final Skin skin;
    private float headerHeight = PADDING;
    private VirtualStage stage;

    public VideoPlayerUI(VideoPlayerScreen videoPlayerScreen, Skin skin) {
        this.videoPlayerScreen = videoPlayerScreen;
        this.skin = skin;
        mainLayout = new MainLayout(this);
        modeLayout = new ModeLayout(this);
        stage = new VirtualStage(new SpriteBatch(), 2f, 2f, 640, 640);
        stage.set3DTransform(new Vector3(0, 0, -2.5f), videoPlayerScreen.getVrCamera().position);

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
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                VideoPlayerUI.this.videoPlayerScreen.setUiVisible(false);
            }
        });
        stage.addActor(closeButton);
        closeButton.setPosition(stage.getWidth() - PADDING, stage.getHeight() - PADDING, Align.topRight);

        headerHeight = Math.max(backButton.getHeight(), closeButton.getHeight()) + PADDING;
    }

    public void setVisible(boolean visible) {
        stage.setVisible(visible);
    }

    public void backButtonClicked() {
        if (stage.isVisible()) {
            if (tableVideoType.isVisible()) {
                tableVideoType.setVisible(false);
                tableMain.setVisible(true);
            }
        }
    }

    public void draw(Camera camera) {
        stage.draw(camera);
    }

    public void update() {
        stage.act();
        if (stage.isVisible() && tableMai.isVisible() && videoPlayer.isPrepared()) {
            final long duration = videoPlayer.getDuration();
            timeLabel.setText(getTimeLabelString(videoPlayer.getCurrentPosition(), duration));
            if (duration != 0)
                slider.setValue((float) videoPlayer.getCurrentPosition() / duration);
        }
    }

    public VirtualStage getStage() {
        return stage;
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
}
