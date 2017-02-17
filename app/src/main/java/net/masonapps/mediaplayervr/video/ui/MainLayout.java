package net.masonapps.mediaplayervr.video.ui;

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

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.Attachable;

import org.masonapps.libgdxgooglevr.input.VirtualStage;

/**
 * Created by Bob on 2/8/2017.
 */

public class MainLayout implements Attachable {

    private final Table videoTable;
    private final VideoPlayerGUI videoPlayerGUI;
    //    private final Table settingsTable;
    protected Label timeLabel;
    protected Slider slider;
    private ImageButton playButton;

    public MainLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        final Skin skin = videoPlayerGUI.getSkin();
        videoTable = new Table(skin);
        videoTable.padTop(videoPlayerGUI.getHeaderHeight());
        videoTable.setFillParent(true);
        videoTable.center();

        final Drawable pauseUp = skin.newDrawable(Style.Drawables.ic_pause_circle_filled_white_48dp, Style.COLOR_UP);
        final Drawable pauseDown = skin.newDrawable(Style.Drawables.ic_pause_circle_filled_white_48dp, Style.COLOR_DOWN);
        final Drawable playUp = skin.newDrawable(Style.Drawables.ic_play_circle_filled_white_48dp, Style.COLOR_UP);
        final Drawable playDown = skin.newDrawable(Style.Drawables.ic_play_circle_filled_white_48dp, Style.COLOR_DOWN);
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
        videoTable.add(playButton).pad(VideoPlayerGUI.PADDING);

        slider = new Slider(0f, 1f, 0.00001f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue() * videoPlayer.getDuration()));
                }
            }
        });
        videoTable.add(slider).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).colspan(4).expandX().fillX();

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        videoTable.add(timeLabel).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).row();

        final TextButton modeBtn = new TextButton("Mode", skin);
        modeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToModeLayout();
            }
        });
        videoTable.add(modeBtn).pad(VideoPlayerGUI.PADDING);

        final TextButton aspectBtn = new TextButton("Aspect Ratio", skin);
        aspectBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToAspectRatioLayout();
            }
        });
        videoTable.add(aspectBtn).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING);

        final TextButton cameraBtn = new TextButton("Camera", skin);
        cameraBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToCameraSettingsLayout();
            }
        });
        videoTable.add(cameraBtn).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).row();

        final TextButton playbackBtn = new TextButton("Playback", skin);
        playbackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToPlaybackSettingsLayout();
            }
        });
        videoTable.add(playbackBtn).padLeft(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING);
    }

    @Override
    public void attach(VirtualStage stage) {
        stage.addActor(videoTable);
    }

    @Override
    public boolean isVisible() {
        return videoTable.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        videoTable.setVisible(visible);
//        if (visible)
//            videoPlayerGUI.getStage().getViewport().update((int) videoTable.getWidth(), (int) videoTable.getHeight(), true);
    }
}
