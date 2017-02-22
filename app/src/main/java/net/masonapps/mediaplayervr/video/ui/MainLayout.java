package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.VirtualStage;
import org.masonapps.libgdxgooglevr.input.VrInputMultiplexer;

/**
 * Created by Bob on 2/8/2017.
 */

public class MainLayout extends BaseUiLayout {

    private final Table videoTable;
    private final Table optionsTable;
    private final VideoPlayerGUI videoPlayerGUI;
    //    private final Table settingsTable;
    protected Label timeLabel;
    protected Slider slider;
    private VirtualStage videoStage;
    private VirtualStage optionsStage;
    private ImageButton playButton;

    public MainLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        final Skin skin = videoPlayerGUI.getSkin();
        videoStage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 720, 100);
        optionsStage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 420, 300);
        videoStage.setPosition(0, -1f, -2.5f);
        optionsStage.setPosition(-1f, 0, -2.5f);
        optionsStage.lookAt(Vector3.Zero, Vector3.Y);
        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, Style.COLOR_WINDOW));
        bg.setFillParent(true);
        videoStage.addActor(bg);
        optionsStage.addActor(new Image(bg.getDrawable()));

        final ImageButton backButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, false));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.backButtonClicked();
            }
        });
        optionsStage.addActor(backButton);
        backButton.setPosition(padding, optionsStage.getHeight() - padding, Align.topLeft);

//        final ImageButton closeButton = new ImageButton(Style.getImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
//        closeButton.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                videoPlayerGUI.getVideoPlayerScreen().setUiVisible(false);
//            }
//        });
//        optionsStage.addActor(closeButton);
//        closeButton.setPosition(optionsStage.getWidth() - padding, optionsStage.getHeight() - padding, Align.topRight);

        final float headerHeight = backButton.getHeight() + padding;
        videoTable = new Table();
        videoTable.setFillParent(true);
        videoTable.center();

        optionsTable = new Table();
        optionsTable.padTop(headerHeight);
        optionsTable.setFillParent(true);
        optionsTable.center();

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
        videoTable.add(playButton).pad(padding);

        slider = new Slider(0f, videoPlayerGUI.getVideoPlayerScreen().getVideoDetails().duration, 1f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue()));
                }
            }
        });
        videoTable.add(slider).expandX().fillX().pad(padding);

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        videoTable.add(timeLabel).pad(padding);

        final TextButton modeBtn = new TextButton("Mode", skin);
        modeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToModeLayout();
            }
        });
        optionsTable.add(modeBtn).pad(padding);

        final TextButton aspectBtn = new TextButton("Aspect Ratio", skin);
        aspectBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToAspectRatioLayout();
            }
        });
        optionsTable.add(aspectBtn).padTop(padding).padBottom(padding).padRight(padding).row();

        final TextButton cameraBtn = new TextButton("Camera", skin);
        cameraBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToCameraSettingsLayout();
            }
        });
        optionsTable.add(cameraBtn).padTop(padding).padBottom(padding).padRight(padding);

        final TextButton playbackBtn = new TextButton("Playback", skin);
        playbackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToPlaybackSettingsLayout();
            }
        });
        optionsTable.add(playbackBtn).padLeft(padding).padBottom(padding).padRight(padding).row();

        optionsTable.add(videoTable).colspan(3).fillX().expandX().pad(padding);
    }

    @Override
    public void update() {
        if (videoStage.isVisible()) {

        }
        videoStage.act();
        optionsStage.act();
    }

    @Override
    public void draw(Camera camera) {
        videoStage.draw(camera);
        optionsStage.draw(camera);
    }

    @Override
    public void attach(VrInputMultiplexer inputMultiplexer) {
        videoStage.addActor(videoTable);
        optionsStage.addActor(optionsTable);
        inputMultiplexer.addProcessor(videoStage);
        inputMultiplexer.addProcessor(optionsStage);
    }

    @Override
    public boolean isVisible() {
        return videoStage.isVisible() || optionsStage.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        videoStage.setVisible(visible);
        optionsStage.setVisible(visible);
    }

    @Override
    public void dispose() {
        if (videoStage != null) {
            videoStage.dispose();
            videoStage = null;
        }
        if (optionsStage != null) {
            optionsStage.dispose();
            optionsStage = null;
        }
    }
}
