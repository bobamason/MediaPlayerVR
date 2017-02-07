package net.masonapps.mediaplayervr;

import android.annotation.SuppressLint;
import android.content.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.mediaplayervr.media.VideoDetails;
import net.masonapps.mediaplayervr.video.VideoMode;
import net.masonapps.mediaplayervr.video.VrVideoPlayer;
import net.masonapps.mediaplayervr.video.VrVideoPlayerExo;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VirtualStage;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Bob on 12/24/2016.
 */

public class VideoPlayerScreen extends MediaPlayerScreen implements DaydreamControllerInputListener, VrVideoPlayer.CompletionListener, VrVideoPlayer.ErrorListener {

    public static final Quaternion tmpQ = new Quaternion();
    public static final int PADDING = 6;
    private static final Vector3 tmpV = new Vector3();
    private static ObjectMap<String, VideoMode> nameModeMap = new ObjectMap<>();
    private static ObjectMap<VideoMode, String> modeNameMap = new ObjectMap<>();
    private static Array<String> modes = new Array<>();

    static {
        modes.add("2D");
        modes.add("3D L/R");
        modes.add("3D T/B");

        modes.add("180");
        modes.add("180 L/R");
        modes.add("180 T/B");

        modes.add("360");
        modes.add("360 L/R");
        modes.add("360 T/B");
        nameModeMap.put(modes.get(0), VideoMode.Mono);
        nameModeMap.put(modes.get(1), VideoMode.LR3D);
        nameModeMap.put(modes.get(2), VideoMode.TB3D);

        nameModeMap.put(modes.get(3), VideoMode.Mono180);
        nameModeMap.put(modes.get(4), VideoMode.LR180);
        nameModeMap.put(modes.get(5), VideoMode.TB180);

        nameModeMap.put(modes.get(6), VideoMode.Mono360);
        nameModeMap.put(modes.get(7), VideoMode.LR360);
        nameModeMap.put(modes.get(8), VideoMode.TB360);

        modeNameMap.put(VideoMode.Mono, modes.get(0));
        modeNameMap.put(VideoMode.LR3D, modes.get(1));
        modeNameMap.put(VideoMode.TB3D, modes.get(2));

        modeNameMap.put(VideoMode.Mono180, modes.get(3));
        modeNameMap.put(VideoMode.LR180, modes.get(4));
        modeNameMap.put(VideoMode.TB180, modes.get(5));

        modeNameMap.put(VideoMode.Mono360, modes.get(6));
        modeNameMap.put(VideoMode.LR360, modes.get(7));
        modeNameMap.put(VideoMode.TB360, modes.get(8));
    }

    private final VideoDetails videoDetails;
    //    private final ImageButton backButton;
    private Context context;
    private VrVideoPlayer videoPlayer;
    private boolean controllerTouched = false;
    private float currentX = 0f;
    private float lastX = 0f;
    private float deltaX = 0f;
    private ModelBuilder modelBuilder;
    private boolean isButtonClicked = false;
    private VirtualStage stage;
    private Table tableMain;
    private Table tableVideoType;
    private boolean controllerConnected;
    private Label timeLabel;
    private ArrayList<TextButton> videoTypeButtons = new ArrayList<>();
    private long startPosition;
    private ImageButton playButton;
    private Slider slider;

    public VideoPlayerScreen(VrGame game, Context context, VideoDetails videoDetails) {
        super(game);
        this.context = context;
        this.videoDetails = videoDetails;
        videoPlayer = new VrVideoPlayerExo(context, videoDetails.uri, videoDetails.width, videoDetails.height);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        setBackgroundColor(Color.BLACK);
        getVrCamera().near = 0.1f;
        getVrCamera().far = 100f;
        modelBuilder = new ModelBuilder();
        stage = new VirtualStage(new SpriteBatch(), 2f, 2f, 640, 640);
        stage.set3DTransform(new Vector3(0, 0, -2.5f), getVrCamera().position);

        final ImageButton backButton = new ImageButton(skin.newDrawable(Icons.ic_arrow_back_white_48dp, Color.WHITE), skin.newDrawable(Icons.ic_arrow_back_white_48dp, Color.LIGHT_GRAY));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                backButtonClicked();
            }
        });
        stage.addActor(backButton);
        backButton.setPosition(0, stage.getHeight(), Align.topLeft);

        final ImageButton closeButton = new ImageButton(skin.newDrawable(Icons.ic_cancel_white_48dp, Color.WHITE), skin.newDrawable(Icons.ic_cancel_white_48dp, Color.LIGHT_GRAY));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setUiVisible(false);
            }
        });
        stage.addActor(closeButton);
        closeButton.setPosition(stage.getWidth(), stage.getHeight(), Align.topRight);

        tableMain = new Table(skin);
        tableMain.setBackground(Icons.WINDOW);
        tableMain.padTop(backButton.getHeight());
        tableMain.setFillParent(true);
        tableMain.center();
        stage.addActor(tableMain);

        tableVideoType = new Table(skin);
        tableVideoType.setBackground(Icons.WINDOW);
        tableVideoType.padTop(backButton.getHeight());
        tableVideoType.setFillParent(true);
        tableVideoType.center();
        stage.addActor(tableVideoType);
        tableVideoType.setVisible(false);

        setupUI();
    }

    private void setupUI() {
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
        tableMain.add(playButton).pad(PADDING);

        slider = new Slider(0f, 1f, 0.001f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (videoPlayer.isPrepared()) {
                    videoPlayer.seekTo(Math.round(slider.getValue() * videoPlayer.getDuration()));
                }
            }
        });
        tableMain.add(slider).padTop(PADDING).padBottom(PADDING).padRight(PADDING).colspan(4).expandX().fillX();

        timeLabel = new Label("0:00:00 / 0:00:00", skin);
        tableMain.add(timeLabel).padTop(PADDING).padBottom(PADDING).padRight(PADDING).row();

        final TextButton mode = new TextButton("Mode", skin);
        mode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tableMain.setVisible(false);
                tableVideoType.setVisible(true);
//                backButton.setVisible(true);
            }
        });
        tableMain.add(mode).pad(PADDING);

        setUpVideoModeTable();
    }

    private void setUpVideoModeTable() {
        for (int i = 0; i < modes.size; i++) {
            final TextButton textButton = new TextButton(modes.get(i), skin);
            final Cell<TextButton> cell = tableVideoType.add(textButton).expandX().fill().center().pad(PADDING);
            if (i % 3 == 2 && i < modes.size - 1) cell.row();
            videoTypeButtons.add(textButton);
        }
        for (final TextButton button : videoTypeButtons) {
            final VideoMode type = videoPlayer.getVideoMode();
            button.setChecked(type == nameModeMap.get(button.getText().toString()));
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    videoPlayer.setVideoMode(nameModeMap.get(button.getText().toString()));
                    for (TextButton b : videoTypeButtons) {
                        b.setChecked(false);
                    }
                    button.setChecked(true);
                }
            });
        }
    }

    @Override
    public void resume() {
        if (!videoPlayer.isPrepared()) {
            videoPlayer.play(videoDetails.uri);
        } else {
            videoPlayer.resume();
        }
    }

    @Override
    public void pause() {
        videoPlayer.pause();
    }

    @Override
    public void show() {
        GdxVr.app.getGvrView().setNeckModelEnabled(false);
        GdxVr.app.getGvrView().setNeckModelFactor(0f);
        GdxVr.input.getDaydreamControllerHandler().addListener(this);
        GdxVr.input.setProcessor(stage);
        controllerConnected = GdxVr.input.isControllerConnected();
    }

    @Override
    public void hide() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.dispose();
        }
        videoPlayer = null;
        GdxVr.input.getDaydreamControllerHandler().removeListener(this);
        GdxVr.input.setProcessor(null);
    }

    @Override
    public void update() {
        super.update();
        stage.act();
        if (stage.isVisible() && tableMain.isVisible() && videoPlayer.isPrepared()) {
            final long duration = videoPlayer.getDuration();
            timeLabel.setText(getTimeLabelString(videoPlayer.getCurrentPosition(), duration));
            if (duration != 0)
                slider.setValue((float) videoPlayer.getCurrentPosition() / duration);
        }
        videoPlayer.update();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void render(Camera camera, int whichEye) {
        Gdx.gl.glClearColor(getBackgroundColor().r, getBackgroundColor().g, getBackgroundColor().b, getBackgroundColor().a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        getModelBatch().begin(camera);
        getWorld().render(getModelBatch(), environment);
        getModelBatch().end();
        if (videoPlayer != null) {
            getModelBatch().begin(camera);
            videoPlayer.render(getModelBatch(), whichEye);
            getModelBatch().end();
        }
        if (isUiVisible()) {
            stage.draw(camera);
            renderCursor(camera);
        }
    }

    @Override
    public void setUiVisible(boolean uiVisible) {
        super.setUiVisible(uiVisible);
        stage.setVisible(uiVisible);
    }

    @Override
    public void onConnectionStateChange(int connectionState) {
        controllerConnected = connectionState == Controller.ConnectionStates.CONNECTED;
    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
        switch (event.button) {
            case DaydreamButtonEvent.BUTTON_TOUCHPAD:
                if (event.action == DaydreamButtonEvent.ACTION_DOWN) {
                    isButtonClicked = true;
                } else if (event.action == DaydreamButtonEvent.ACTION_UP) {
                    isButtonClicked = false;
                    if (videoPlayer.isPrepared()) {
                        if (videoPlayer.isPlaying()) {
                            setUiVisible(true);
                            videoPlayer.pause();
                        } else {
                            if (!stage.isCursorOver()) {
                                videoPlayer.resume();
                                setUiVisible(false);
                            }
                        }
                    }
                }
                break;
            case DaydreamButtonEvent.BUTTON_APP:
                if (event.action == DaydreamButtonEvent.ACTION_UP) {
                    backButtonClicked();
                }
                break;
        }
    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        if (!isButtonClicked) {
            if (event.action == DaydreamTouchEvent.ACTION_DOWN) {
                deltaX = 0;
                if (videoPlayer.isPlaying()) {
                    startPosition = videoPlayer.getCurrentPosition();
                }
                lastX = controller.touch.x;
            } else if (event.action == DaydreamTouchEvent.ACTION_MOVE) {
                currentX = controller.touch.x;
                deltaX += currentX - lastX;
//                    Log.d(VideoPlayerScreen.class.getSimpleName(), "deltaX = " + deltaX);
                if (videoPlayer.isPlaying() && !stage.isVisible()) {
//                        videoPlayer.pause();
                    final long currentPosition = videoPlayer.getCurrentPosition();
                    final long duration = videoPlayer.getDuration();
                    videoPlayer.seekTo(MathUtils.clamp(startPosition + (int) (deltaX * 20000f), 10, duration - 1000));
//                        stage.setVisible(true);
//                        timeLabel.setText(getTimeLabelString(currentPosition, duration));
//                        videoPlayer.resume();
                } else {
//                        z = MathUtils.clamp(z + deltaX * 0.02f, -5f, 5f);
                }
                lastX = currentX;
            } else if (event.action == DaydreamTouchEvent.ACTION_UP) {
                deltaX = 0;
//                stage.setVisible(false);
            }
        }
    }

    private String getTimeLabelString(long currentPosition, long duration) {
        return String.format(Locale.ENGLISH,
                "%d:%02d:%02d / %d:%02d:%02d",
                currentPosition / 1000 / (60 * 60),
                (currentPosition / 1000 / 60) % 60,
                (currentPosition / 1000) % 60,
                duration / 1000 / (60 * 60),
                (duration / 1000 / 60) % 60,
                (duration / 1000) % 60);
    }

    private void backButtonClicked() {
        if (stage.isVisible()) {
            if (tableVideoType.isVisible()) {
                tableVideoType.setVisible(false);
                tableMain.setVisible(true);
            } else if (tableMain.isVisible()) {
                mediaPlayerGame.goToSelectionScreen();
            }
        }
    }

    @Override
    public void onCompletion() {

    }

    @Override
    public void onError(String error) {

    }
}
