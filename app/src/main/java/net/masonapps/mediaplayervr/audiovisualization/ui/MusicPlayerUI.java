package net.masonapps.mediaplayervr.audiovisualization.ui;

import android.media.MediaPlayer;

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
import net.masonapps.mediaplayervr.audiovisualization.MusicVisualizerScreen;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.Locale;

/**
 * Created by Bob on 2/13/2017.
 */

public class MusicPlayerUI implements Disposable {
    public static final int PADDING = 10;
    private final MusicMainLayout mainLayout;
    private final Skin skin;
    private MusicVisualizerScreen musicVisualizerScreen;
    private float headerHeight = PADDING;
    private VirtualStage stage;

    public MusicPlayerUI(MusicVisualizerScreen musicVisualizerScreen, Skin skin) {
        this.musicVisualizerScreen = musicVisualizerScreen;
        this.skin = skin;

        mainLayout = new MusicMainLayout(this);
        stage = new VirtualStage(new SpriteBatch(), 1080, 1080);
        stage.setPosition(0, -0.5f, -2.5f);
        stage.lookAt(musicVisualizerScreen.getVrCamera().position, Vector3.Y);
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        stage.addActor(bg);

//        mainLayout.attach(stage);
        mainLayout.setVisible(true);

        stage.getViewport().update(1080, 720);

        final ImageButton backButton = new ImageButton(skin.newDrawable(Style.Drawables.ic_arrow_back_white_48dp, Style.COLOR_UP_2), skin.newDrawable(Style.Drawables.ic_arrow_back_white_48dp, Style.COLOR_DOWN_2));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stage.addActor(backButton);
        backButton.setPosition(PADDING, stage.getHeight() - PADDING, Align.topLeft);

        final ImageButton closeButton = new ImageButton(skin.newDrawable(Style.Drawables.ic_close_white_48dp, Style.COLOR_UP_2), skin.newDrawable(Style.Drawables.ic_close_white_48dp, Style.COLOR_DOWN_2));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MusicPlayerUI.this.musicVisualizerScreen.setUiVisible(false);
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
            if (!mainLayout.isVisible()) {
                switchToMainLayout();
            }
        }
    }

    public void draw(Camera camera) {
        stage.draw(camera);
    }

    public void update() {
        stage.act(Math.min(GdxVr.graphics.getDeltaTime(), 0.0333333f));
        final MediaPlayer player = musicVisualizerScreen.getMediaPlayer();
        if (stage.isVisible() && mainLayout.isVisible() && !musicVisualizerScreen.isLoading()) {
            final long duration = player.getDuration();
            mainLayout.timeLabel.setText(getTimeLabelString(player.getCurrentPosition(), duration));
            if (duration != 0) {
                mainLayout.slider.setStepSize(1f / duration);
                mainLayout.slider.setValue((float) player.getCurrentPosition() / duration);
            }
        }
    }

    public VirtualStage getStage() {
        return stage;
    }

    public MusicVisualizerScreen getMusicVisualizerScreen() {
        return musicVisualizerScreen;
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
    }
}