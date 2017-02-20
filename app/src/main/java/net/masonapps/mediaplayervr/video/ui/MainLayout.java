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

    private final Table videoGroup;
    private final Table optionsTable;
    private final VideoPlayerGUI videoPlayerGUI;
    //    private final Table settingsTable;
    protected Label timeLabel;
    protected Slider slider;
    private ImageButton playButton;

    public MainLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        final Skin skin = videoPlayerGUI.getSkin();
        videoGroup = new Table();
        videoGroup.center();

        optionsTable = new Table();
        optionsTable.padTop(videoPlayerGUI.getHeaderHeight());
        optionsTable.setFillParent(true);
        optionsTable.center();
        optionsTable.background(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));

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
        videoGroup.add(playButton).pad(VideoPlayerGUI.PADDING);

        slider = new Slider(0f, videoPlayerGUI.getVideoPlayerScreen().getVideoDetails().duration, 1f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue()));
                }
            }
        });
        videoGroup.add(slider).expandX().fillX().pad(VideoPlayerGUI.PADDING);

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        videoGroup.add(timeLabel).pad(VideoPlayerGUI.PADDING);

        final TextButton modeBtn = new TextButton("Mode", skin);
        modeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToModeLayout();
            }
        });
        optionsTable.add(modeBtn).pad(VideoPlayerGUI.PADDING);

        final TextButton aspectBtn = new TextButton("Aspect Ratio", skin);
        aspectBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToAspectRatioLayout();
            }
        });
        optionsTable.add(aspectBtn).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).row();

        final TextButton cameraBtn = new TextButton("Camera", skin);
        cameraBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToCameraSettingsLayout();
            }
        });
        optionsTable.add(cameraBtn).padTop(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING);

        final TextButton playbackBtn = new TextButton("Playback", skin);
        playbackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToPlaybackSettingsLayout();
            }
        });
        optionsTable.add(playbackBtn).padLeft(VideoPlayerGUI.PADDING).padBottom(VideoPlayerGUI.PADDING).padRight(VideoPlayerGUI.PADDING).row();

        optionsTable.add(videoGroup).colspan(3).fillX().expandX().pad(VideoPlayerGUI.PADDING);
    }

    @Override
    public void attach(VirtualStage stage) {
        stage.addActor(optionsTable);
    }

    @Override
    public boolean isVisible() {
        return videoGroup.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        videoGroup.setVisible(visible);
        optionsTable.setVisible(visible);
    }
}
