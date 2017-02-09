package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

    private final Table tableMain;
    protected Label timeLabel;
    protected Slider slider;
    private ImageButton playButton;

    public MainLayout(final VideoPlayerGUI videoPlayerGUI) {
        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        final Skin skin = videoPlayerGUI.getSkin();
        tableMain = new Table(skin);
        tableMain.setBackground(Icons.WINDOW);
        tableMain.padTop(videoPlayerGUI.getHeaderHeight());
        tableMain.setFillParent(true);
        tableMain.center();

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
        tableMain.add(playButton).pad(VideoPlayerGUI.PADDING);

        slider = new Slider(0f, 1f, 0.001f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue() * videoPlayer.getDuration()));
                }
            }
        });
        tableMain.add(slider).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).colspan(4).expandX().fillX();

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        tableMain.add(timeLabel).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).row();

        final TextButton mode = new TextButton("Mode", skin);
        mode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToModeLayout();
            }
        });
        tableMain.add(mode).pad(VideoPlayerGUI.PADDING);
    }

    @Override
    public void attach(VirtualStage stage) {
        stage.addActor(tableMain);
    }

    @Override
    public boolean isVisible() {
        return tableMain.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        tableMain.setVisible(visible);
    }
}
