package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import net.masonapps.mediaplayervr.Icons;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 2/8/2017.
 */

public class MainLayout implements Attachable {

    private final Table table;
    protected Label timeLabel;
    protected Slider slider;
    private ImageButton playButton;

    public MainLayout(final VideoPlayerGUI videoPlayerGUI) {
        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        final Skin skin = videoPlayerGUI.getSkin();
        table = new Table(skin);
        table.setBackground(Icons.WINDOW);
        table.padTop(videoPlayerGUI.getHeaderHeight());
        table.setFillParent(true);
        table.center();

        final Drawable pauseUp = skin.newDrawable(Icons.ic_pause_circle_filled_white_48dp);
        final Drawable pauseDown = skin.newDrawable(Icons.ic_pause_circle_filled_white_48dp, Color.LIGHT_GRAY);
        final Drawable playUp = skin.newDrawable(Icons.ic_play_circle_filled_white_48dp);
        final Drawable playDown = skin.newDrawable(Icons.ic_play_circle_filled_white_48dp, Color.LIGHT_GRAY);
        playButton = new ImageButton(pauseUp, pauseDown);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (videoPlayer.isPrepared()) {
                    if (videoPlayer.isPlaying()) {
                        videoPlayer.pause();
                        playButton.getStyle().imageUp = playUp;
                        playButton.getStyle().imageDown = playDown;
                    } else {
                        videoPlayer.resume();
                        playButton.getStyle().imageUp = pauseUp;
                        playButton.getStyle().imageDown = pauseDown;
                    }
                }
            }
        });
        table.add(playButton).pad(VideoPlayerGUI.PADDING);

        slider = new Slider(0f, 1f, 0.001f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue() * videoPlayer.getDuration()));
                }
            }
        });
        table.add(slider).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).colspan(4).expandX().fillX();

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        table.add(timeLabel).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).row();

        final TextButton mode = new TextButton("Mode", skin);
        mode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToModeLayout();
            }
        });
        table.add(mode).pad(VideoPlayerGUI.PADDING);

        final SelectBox<String> selectBox = new SelectBox<>(skin);
        final String[] ratioLabels = {"AUTO", "1:1", "4:3", "16:10", "16:9", "2:1"};
        final float[] ratios = new float[]{-1f, 1f, 4f / 3f, 16f / 10f, 16f / 9f, 2f / 1f};
        selectBox.setItems(ratioLabels);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                videoPlayer.setAspectRatio(ratios[selectBox.getSelectedIndex()]);
            }
        });
        table.add(selectBox).padTop(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING);
    }

    @Override
    public void attach(VirtualStage stage) {
        stage.addActor(table);
    }

    @Override
    public boolean isVisible() {
        return table.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        table.setVisible(visible);
    }
}
