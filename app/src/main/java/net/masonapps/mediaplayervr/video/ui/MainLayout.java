package net.masonapps.mediaplayervr.video.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import net.masonapps.mediaplayervr.Style;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.vrinterface.BaseUiLayout;

import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.ui.VirtualStage;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

import java.util.Locale;

/**
 * Created by Bob on 2/8/2017.
 */

public class MainLayout extends BaseUiLayout {


    private static final float MIN_MOVEMENT = 0.45f;
    private final Table videoTable;
    private final Table optionsTable;
    private final VideoPlayerGUI videoPlayerGUI;
    private final GestureDetector gestureDetector;
    //    private final Table settingsTable;
    protected Label timeLabel;
    protected Slider slider;
    private float downX;
    private VirtualStage videoStage;
    private VirtualStage optionsStage;
    private ImageButton playButton;
    private boolean isSwiping = false;
    private boolean isSliderDragging = false;

    public MainLayout(final VideoPlayerGUI videoPlayerGUI) {
        this.videoPlayerGUI = videoPlayerGUI;
        final VrVideoPlayer videoPlayer = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
        final Skin skin = videoPlayerGUI.getSkin();
        videoStage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 720, 100);
        optionsStage = new VirtualStage(videoPlayerGUI.getSpriteBatch(), 420, 340);
        videoStage.setPosition(0, 0.65f, -1.2f);
        optionsStage.setPosition(-1f, 0, -1.2f);
        optionsStage.lookAt(Vector3.Zero, Vector3.Y);
        videoStage.addActor(Style.newBackgroundImage(skin));
        optionsStage.addActor(Style.newBackgroundImage(skin));

        final ImageButton backButton = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.ic_arrow_back_white_48dp, true));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.backButtonClicked();
            }
        });
        optionsStage.addActor(backButton);
        backButton.setPosition(padding, optionsStage.getHeight() - padding, Align.topLeft);

//        final ImageButton closeButton = new ImageButton(Style.createImageButtonStyle(skin, Style.Drawables.ic_close_white_48dp, true));
//        closeButton.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                videoPlayerGUI.getVideoPlayerScreen().setInputVisible(false);
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

        final Drawable pauseUp = skin.newDrawable(Style.Drawables.ic_pause_circle_filled_white_48dp, Style.COLOR_UP_2);
        final Drawable pauseDown = skin.newDrawable(Style.Drawables.ic_pause_circle_filled_white_48dp, Style.COLOR_DOWN_2);
        final Drawable playUp = skin.newDrawable(Style.Drawables.ic_play_circle_filled_white_48dp, Style.COLOR_UP_2);
        final Drawable playDown = skin.newDrawable(Style.Drawables.ic_play_circle_filled_white_48dp, Style.COLOR_DOWN_2);
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
                if (videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer().useFlatRectangle())
                    videoPlayerGUI.switchToAspectRatioLayout();
                else
                    videoPlayerGUI.switchToPlaybackSettingsLayout();
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

        final TextButton colorBtn = new TextButton("Color", skin);
        colorBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.switchToColorSettingsLayout();
            }
        });
        optionsTable.add(colorBtn).padTop(padding).padBottom(padding).padRight(padding).row();

        final TextButton resetBtn = new TextButton("reset", skin);
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                videoPlayerGUI.getVideoPlayerScreen().restoreDefaultVideoOptions();
            }
        });
        optionsTable.add(resetBtn).padTop(padding).padBottom(padding).padRight(padding).row();

        gestureDetector = new GestureDetector(new GestureDetector.GestureAdapter() {

            @Override
            public boolean touchDown(float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public boolean pan(float x, float y, float deltaX, float deltaY) {
                return true;
            }

            @Override
            public boolean fling(float velocityX, float velocityY, int button) {
                return true;
            }
        });
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

    @Override
    public void update() {
        if (videoStage.isVisible()) {
            final VrVideoPlayer player = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
            if (player.isPrepared()) {
                updateSeek(player);
            }
        }
        videoStage.act();
        optionsStage.act();
    }

    private void updateSeek(VrVideoPlayer player) {
        if (!slider.isDragging()) {
            if (isSliderDragging) {
                player.seekTo(Math.round(slider.getValue()));
                isSliderDragging = false;
            } else {
                slider.setValue(player.getCurrentPosition());
            }
        } else {
            isSliderDragging = true;
        }
        timeLabel.setText(getTimeLabelString(player.getCurrentPosition(), player.getDuration()));
    }

    @Override
    public void draw(Camera camera) {
        videoStage.draw(camera);
        optionsStage.draw(camera);
    }

    @Override
    public void attach(VrUiContainer container) {
        videoStage.addActor(videoTable);
        optionsStage.addActor(optionsTable);
        container.addProcessor(videoStage);
        container.addProcessor(optionsStage);
    }

    public boolean isCursorOverSeekbar() {
        return videoStage.isCursorOver();
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

    public void onTouchPadEvent(DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                downX = event.x;
                isSwiping = true;
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                if (!isSwiping) break;
                float currentX = event.x;
                final float diff = currentX - downX;
                final float abs = Math.abs(diff);
                if (abs > MIN_MOVEMENT) {
//                        final float x = diff > 0 ? (diff - MIN_MOVEMENT) : (diff + MIN_MOVEMENT);
                    long seek = (long) MathUtils.clamp(slider.getValue() + 10000d * Math.signum(diff), slider.getMinValue(), slider.getMaxValue());
                    final VrVideoPlayer player = videoPlayerGUI.getVideoPlayerScreen().getVideoPlayer();
                    if (player.isPrepared()) {
                        player.seekTo(seek);
                        updateSeek(player);
                    }
                    isSwiping = false;
                }
                break;
            case DaydreamTouchEvent.ACTION_UP:
                break;
        }
    }
}
