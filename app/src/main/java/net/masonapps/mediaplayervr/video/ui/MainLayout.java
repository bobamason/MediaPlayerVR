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

import java.util.Locale;

/**
 * Created by Bob on 2/8/2017.
 */

public class MainLayout {

    private final Table tableMain;
    private Label timeLabel;
    private ImageButton playButton;
    private Slider slider;

    public MainLayout(VideoPlayerUI videoPlayerUI) {
        final Skin skin = videoPlayerUI.getSkin();
        tableMain = new Table(skin);
        tableMain.setBackground(Icons.WINDOW);
        tableMain.padTop(videoPlayerUI.getHeaderHeight());
        tableMain.setFillParent(true);
        tableMain.center();
        stage.addActor(tableMain);

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
        tableMain.add(playButton).pad(VideoPlayerUI.PADDING);

        slider = new Slider(0f, 1f, 0.001f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue() * videoPlayer.getDuration()));
                }
            }
        });
        tableMain.add(slider).padTop(VideoPlayerUI.PADDING).padBottom(VideoPlayerUI.PADDING).padRight(VideoPlayerUI.PADDING).colspan(4).expandX().fillX();

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        tableMain.add(timeLabel).padTop(VideoPlayerUI.PADDING).padBottom(VideoPlayerUI.PADDING).padRight(VideoPlayerUI.PADDING).row();

        final TextButton mode = new TextButton("Mode", skin);
        mode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tableMain.setVisible(false);
                tableVideoType.setVisible(true);
//                backButton.setVisible(true);
            }
        });
        tableMain.add(mode).pad(VideoPlayerUI.PADDING);
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
}
